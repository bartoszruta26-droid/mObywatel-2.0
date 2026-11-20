/* SPDX-FileCopyrightText: 2026 bartoszruta26-droid */
/* SPDX-License-Identifier: CC0-1.0 */
import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const files = [
  "docs/identity-assets/external-repository-sources.csv",
  "docs/identity-assets/institutional-symbol-candidates.csv"
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
  const source=fs.readFileSync(path.join(root,file),"utf8");
  const csvBody=source.split(/\r?\n/).filter(line => !line.trimStart().startsWith("#")).join("\n");
  const rows=parseCsv(csvBody);
  if (rows.length < 2) throw new Error(file+": no data rows");
  const header=rows[0], idIndex=header.findIndex(x=>/^(source_id|candidate_id)$/.test(x));
  if (idIndex<0) throw new Error(file+": missing stable ID column");
  const ids=new Set();
  for (const [i,row] of rows.slice(1).entries()) {
    if (row.length!==header.length) throw new Error(file+":"+(i+2)+": expected "+header.length+" fields, got "+row.length);
    const id=row[idIndex].trim();
    if (!id) throw new Error(file+":"+(i+2)+": empty ID");
    if (ids.has(id)) throw new Error(file+":"+(i+2)+": duplicate ID "+id);
    ids.add(id);
  }
  console.log(file+": "+(rows.length-1)+" records; IDs unique; CSV shape valid");
}
