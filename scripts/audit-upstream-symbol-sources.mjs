/* SPDX-FileCopyrightText: 2026 bartoszruta26-droid */
/* SPDX-License-Identifier: CC0-1.0 */
import fs from "node:fs";
import path from "node:path";

const token = process.env.GITHUB_TOKEN;
if (!token) throw new Error("GITHUB_TOKEN is required for authenticated GitHub API requests");
const sources = [
  { id:"collective-service-organizations", repo:"collective-service/organizations-logo", ref:"main", mode:"all-visual-assets", rights:"reference_only", group:"NGO_and_humanitarian" },
  { id:"simple-icons", repo:"simple-icons/simple-icons", ref:"develop", mode:"curated-organization-icons", rights:"per_asset_review", group:"brand_and_organization_marks" },
  { id:"thesvg", repo:"glincker/thesvg", ref:"main", mode:"inventory-only", rights:"per_asset_review", group:"brand_discovery_pool" },
  { id:"circle-flags", repo:"HatScripts/circle-flags", ref:"gh-pages", mode:"flag-assets", rights:"MIT_plus_flag_law_review", group:"flags" },
  { id:"flag-icons", repo:"lipis/flag-icons", ref:"main", mode:"flag-assets", rights:"MIT_plus_flag_law_review", group:"flags" },
  { id:"country-flags", repo:"hampusborgos/country-flags", ref:"main", mode:"flag-assets", rights:"reference_only_license_file_missing", group:"flags" },
  { id:"africons", repo:"MuriungiPatrick/africons", ref:"master", mode:"inventory-only", rights:"reference_only_license_unverified", group:"African_organizations" }
];
const curatedSlugs = new Set(["rotaryinternational","unitednations","worldhealthorganization","unesco","unicef","unhcr","oxfam","redcross","redcrescent","amnesty","greenpeace","worldwildlifefund","doctorswithoutborders","savethechildren","care"]);
const headers = { "Accept":"application/vnd.github+json", "Authorization":`Bearer ${token}`, "X-GitHub-Api-Version":"2022-11-28" };
const reports = [];
const candidates = [];
for (const source of sources) {
  const url = `https://api.github.com/repos/${source.repo}/git/trees/${source.ref}?recursive=1`;
  const response = await fetch(url,{headers});
  if (!response.ok) throw new Error(`${source.repo}: GitHub API ${response.status} ${await response.text()}`);
  const tree = await response.json();
  const entries = tree.tree || [];
  const licenseFiles = entries.filter(x => /(^|\/)(license|licence)(\.[^/]*)?$/i.test(x.path)).map(x=>x.path);
  const visual = entries.filter(x => /\.(svg|png|jpe?g|webp)$/i.test(x.path));
  const selected = source.mode === "all-visual-assets" || source.mode === "flag-assets"
    ? visual
    : source.mode === "curated-organization-icons"
      ? visual.filter(x => curatedSlugs.has(path.basename(x.path,".svg").toLowerCase()))
      : [];
  for (const item of selected) {
    candidates.push({
      source_id:source.id, repository:source.repo, ref:source.ref, path:item.path,
      group:source.group, rights_status:source.rights, import_status:"not_imported",
      source_url:`https://github.com/${source.repo}/blob/${source.ref}/${item.path}`,
      download_url:"",license_short_name:"",license_url:"",author:"",attribution_required:"",source_credit:"",commons_categories:""
    });
  }
  reports.push({
    source_id:source.id, repository:source.repo, ref:source.ref, tree_sha:tree.sha || null,
    tree_truncated:Boolean(tree.truncated), visual_file_count:visual.length,
    svg_file_count:visual.filter(x=>x.path.toLowerCase().endsWith(".svg")).length,
    license_files:licenseFiles, selected_candidate_count:selected.length,
    rights_status:source.rights, mode:source.mode
  });
}

const commonsCategories = [
  { id:"commons-nonprofit-svg", title:"Category:SVG logos of non-profit organizations", group:"international_ngo", limit:200 },
  { id:"commons-ngo-logos", title:"Category:Logos of non-governmental organizations", group:"ngo_and_social_organizations", limit:200 },
  { id:"commons-international-flags", title:"Category:SVG flags of international organizations", group:"international_organization_flags", limit:150 },
  { id:"commons-government-svg", title:"Category:SVG logos of governments and government agencies", group:"government_and_public_institutions", limit:150 },
  { id:"commons-charity-logos", title:"Category:Logos of charitable organizations", group:"charitable_organizations", limit:150 },
  { id:"commons-polish-city-arms", title:"Category:SVG coats of arms of cities of Poland", group:"polish_city_arms", limit:200 },
  { id:"commons-polish-gmina-arms", title:"Category:SVG coats of arms of gminas of Poland", group:"polish_gmina_arms", limit:200 },
  { id:"commons-polish-powiat-arms", title:"Category:SVG coats of arms of powiats of Poland", group:"polish_powiats_arms", limit:100 }
];
const commonsHeaders = { "User-Agent":"mObywatel-2.0-symbol-catalog/1.0 (https://github.com/bartoszruta26-droid/mObywatel-2.0)", "Accept":"application/json" };
const commonsByPage = new Map();
const commonsReports = [];
const meta = (info,key) => String(info?.[key]?.value || "").replace(/<[^>]*>/g," ").replace(/\\s+/g," ").trim();
for (const category of commonsCategories) {
  let continuation = null, collected = 0, pagesFetched = 0;
  while (collected < category.limit && pagesFetched < 5) {
    const params = new URLSearchParams({
      action:"query", generator:"categorymembers", gcmtitle:category.title,
      gcmtype:"file", gcmnamespace:"6", gcmlimit:String(Math.min(200,category.limit-collected)),
      prop:"imageinfo", iiprop:"url|extmetadata", format:"json", formatversion:"2"
    });
    if (continuation) params.set("gcmcontinue",continuation);
    const response = await fetch("https://commons.wikimedia.org/w/api.php?"+params.toString(),{headers:commonsHeaders});
    if (!response.ok) throw new Error("Wikimedia Commons "+category.title+": HTTP "+response.status);
    const payload = await response.json();
    const pages = payload.query?.pages || [];
    for (const page of pages) {
      const info = page.imageinfo?.[0] || {};
      const pageUrl = "https://commons.wikimedia.org/wiki/"+encodeURIComponent(String(page.title||"").replace(/ /g,"_"));
      const record = {
        source_id:"wikimedia-commons", repository:"Wikimedia Commons", ref:"live-category-snapshot",
        path:page.title, group:category.group, rights_status:"per_file_metadata_review_required",
        import_status:"not_imported", source_url:pageUrl, download_url:info.url || "",
        license_short_name:meta(info.extmetadata,"LicenseShortName"),
        license_url:meta(info.extmetadata,"LicenseUrl"),
        author:meta(info.extmetadata,"Artist"),
        attribution_required:meta(info.extmetadata,"AttributionRequired"),
        source_credit:meta(info.extmetadata,"Credit"),
        commons_categories:category.title
      };
      const key=pageUrl;
      if (commonsByPage.has(key)) {
        const old=commonsByPage.get(key);
        old.commons_categories += " | "+category.title;
      } else commonsByPage.set(key,record);
      collected++;
    }
    pagesFetched++;
    continuation = payload.continue?.gcmcontinue || null;
    if (!continuation || pages.length===0) break;
  }
  commonsReports.push({category:category.title,group:category.group,items_seen:collected,pages_fetched:pagesFetched});
}
candidates.push(...commonsByPage.values());
reports.push(...commonsReports.map(x=>({source_id:"wikimedia-commons",repository:"Wikimedia Commons",ref:"live-category-snapshot",visual_file_count:x.items_seen,svg_file_count:null,license_files:[],selected_candidate_count:x.items_seen,rights_status:"per_file_metadata_review_required",mode:x.category,group:x.group})));

fs.mkdirSync("build",{recursive:true});
fs.writeFileSync("build/upstream-symbol-audit.json",JSON.stringify({generatedAt:new Date().toISOString(),reports,candidateCount:candidates.length,candidates},null,2)+"\n");
const cols=["source_id","repository","ref","path","group","rights_status","import_status","source_url","download_url","license_short_name","license_url","author","attribution_required","source_credit","commons_categories"];
const quote=v=>'"'+String(v).replace(/"/g,'""')+'"';
fs.writeFileSync("build/upstream-symbol-candidates.csv",cols.join(",")+"\n"+candidates.map(x=>cols.map(k=>quote(x[k])).join(",")).join("\n")+"\n");
for (const r of reports) console.log(`${r.repository}: ${r.svg_file_count} SVG, ${r.visual_file_count} visual files, ${r.selected_candidate_count} candidates; rights=${r.rights_status}`);
console.log(`Wrote ${candidates.length} source candidates to build/upstream-symbol-candidates.csv`);
