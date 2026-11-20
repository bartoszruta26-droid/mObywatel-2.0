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
      source_url:`https://github.com/${source.repo}/blob/${source.ref}/${item.path}`
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
fs.mkdirSync("build",{recursive:true});
fs.writeFileSync("build/upstream-symbol-audit.json",JSON.stringify({generatedAt:new Date().toISOString(),reports,candidateCount:candidates.length,candidates},null,2)+"\n");
const cols=["source_id","repository","ref","path","group","rights_status","import_status","source_url"];
const quote=v=>'"'+String(v).replace(/"/g,'""')+'"';
fs.writeFileSync("build/upstream-symbol-candidates.csv",cols.join(",")+"\n"+candidates.map(x=>cols.map(k=>quote(x[k])).join(",")).join("\n")+"\n");
for (const r of reports) console.log(`${r.repository}: ${r.svg_file_count} SVG, ${r.visual_file_count} visual files, ${r.selected_candidate_count} candidates; rights=${r.rights_status}`);
console.log(`Wrote ${candidates.length} source candidates to build/upstream-symbol-candidates.csv`);
