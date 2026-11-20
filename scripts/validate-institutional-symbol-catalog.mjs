/* SPDX-FileCopyrightText: 2026 bartoszruta26-droid */
/* SPDX-License-Identifier: CC0-1.0 */
import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const files = [
  { path:"docs/identity-assets/external-repository-sources.csv", id:"source_id", allowEmpty:false },
  { path:"docs/identity-assets/institutional-symbol-candidates.csv", id:"candidate_id", allowEmpty:false },
  { path:"docs/identity-assets/approved-symbol-assets.csv", id:"asset_id", allowEmpty:true }
];
function parseCsv(source) {
  const rows=[]; let row=[], field="", quoted=false;
  for (let i=0;i<source.length;i++) {
    const c=source[i], n=source[i+1];
    if (quoted) {
      if (c === '"' && n === '"') { field+='"'; i++; }
      else if (c === '"') quoted=false;
      else field+=c;
    } else if (c === '"') quoted=true;
    else if (c === ",") { row.push(field); field=""; }
    else if (c === "\n") { row.push(field); if(row.some(x=>x.trim())) rows.push(row); row=[]; field=""; }
    else if (c !== "\r") field+=c;
  }
  row.push(field); if(row.some(x=>x.trim())) rows.push(row);
  if (quoted) throw new Error("Unclosed CSV quote");
  return rows;
}
for (const file of files) {
  const source=fs.readFileSync(path.join(root,file.path),"utf8");
  const csvBody=source.split(/\r?\n/).filter(line => !line.trimStart().startsWith("#")).join("\n");
  const rows=parseCsv(csvBody);
  if (rows.length < 1 || (!file.allowEmpty && rows.length < 2)) throw new Error(file.path+": missing CSV header or data rows");
  const header=rows[0], idIndex=header.indexOf(file.id);
  if (idIndex<0) throw new Error(file.path+": missing stable ID column "+file.id);
  const ids=new Set();
  for (const [i,row] of rows.slice(1).entries()) {
    if (row.length!==header.length) throw new Error(file.path+":"+(i+2)+": expected "+header.length+" fields, got "+row.length);
    const id=row[idIndex].trim();
    if (!id) throw new Error(file.path+":"+(i+2)+": empty ID");
    if (ids.has(id)) throw new Error(file.path+":"+(i+2)+": duplicate ID "+id);
    ids.add(id);
  }
  console.log(file.path+": "+(rows.length-1)+" records; IDs unique; CSV shape valid");
}

const appCatalogPath="obywatel-android/app/src/main/assets/institutions/institutional-symbol-catalog-800.csv";
const appSource=fs.readFileSync(path.join(root,appCatalogPath),"utf8");
const appBody=appSource.split(/\r?\n/).filter(line => !line.trimStart().startsWith("#")).join("\n");
const appRows=parseCsv(appBody);
if (appRows.length!==801) throw new Error(appCatalogPath+": expected header plus exactly 800 catalog entries; got "+(appRows.length-1));
const appHeader=appRows[0];
const required=["filename","symbol_type","commons_page","svg_url","author","license","license_url","restrictions","sha256"];
for (const key of required) if (!appHeader.includes(key)) throw new Error(appCatalogPath+": missing column "+key);
const appIndex=Object.fromEntries(required.map(k=>[k,appHeader.indexOf(k)]));
const titles=new Set();
for (const [i,row] of appRows.slice(1).entries()) {
  if (row.length!==appHeader.length) throw new Error(appCatalogPath+":"+(i+2)+": malformed CSV row");
  const title=row[appIndex.filename].trim();
  const page=row[appIndex.commons_page].trim();
  const url=row[appIndex.svg_url].trim();
  const license=row[appIndex.license].trim();
  if (!title.startsWith("File:")) throw new Error(appCatalogPath+":"+(i+2)+": invalid Commons file title");
  if (titles.has(title)) throw new Error(appCatalogPath+":"+(i+2)+": duplicate title "+title);
  titles.add(title);
  if (!page.startsWith("https://commons.wikimedia.org/wiki/File%3A")) throw new Error(appCatalogPath+":"+(i+2)+": invalid Commons source page");
  const parsedUrl=new URL(url);
  if (parsedUrl.protocol!=="https:" || parsedUrl.hostname!=="upload.wikimedia.org" || !parsedUrl.pathname.toLowerCase().endsWith(".svg")) throw new Error(appCatalogPath+":"+(i+2)+": invalid SVG download URL");
  if (!license) throw new Error(appCatalogPath+":"+(i+2)+": missing per-file license metadata");
}
console.log(appCatalogPath+": 800 unique SVG candidates; source pages and per-file license names present");
