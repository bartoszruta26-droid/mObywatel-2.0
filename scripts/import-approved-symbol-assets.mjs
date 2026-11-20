/* SPDX-FileCopyrightText: 2026 bartoszruta26-droid */
/* SPDX-License-Identifier: CC0-1.0 */
import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";

const csvPath="docs/identity-assets/approved-symbol-assets.csv";
const root=process.cwd();
const source=fs.readFileSync(csvPath,"utf8").split(/\r?\n/).filter(x=>!x.trimStart().startsWith("#")).join("\n");
function parseCsv(s){const rows=[];let row=[],v="",q=false;for(let i=0;i<s.length;i++){const c=s[i],n=s[i+1];if(q){if(c==='"'&&n==='"'){v+='"';i++;}else if(c==='"')q=false;else v+=c;}else if(c==='"')q=true;else if(c===","){row.push(v);v="";}else if(c==="\n"){row.push(v);if(row.some(x=>x.trim()))rows.push(row);row=[];v="";}else if(c!=="\r")v+=c;}row.push(v);if(row.some(x=>x.trim()))rows.push(row);if(q)throw new Error("Unclosed CSV quote");return rows;}
const rows=parseCsv(source);
if(rows.length<2){console.log("No approved assets in allowlist; no files downloaded.");process.exit(0);}
const header=rows[0], records=rows.slice(1).map((r,i)=>{if(r.length!==header.length)throw new Error("CSV row "+(i+2)+" has incorrect column count");return Object.fromEntries(header.map((k,j)=>[k,r[j].trim()]));});
const approved=records.filter(x=>x.usage_status==="redistributable");
if(!approved.length){console.log("No records marked redistributable; protected/reference-only entries are intentionally not downloaded.");process.exit(0);}
const permitted=new Set(["CC0-1.0","CC-BY-4.0","CC-BY-SA-4.0","MIT","Apache-2.0","BSD-2-Clause","BSD-3-Clause","Public-Domain"]);
const manifest=[];
for(const item of approved){
 for(const key of ["asset_id","organization","group","source_url","source_page_url","license_id","license_url","attribution","approved_by","sha256_expected","destination_file"])if(!item[key])throw new Error(item.asset_id+": missing "+key);
 if(!permitted.has(item.license_id))throw new Error(item.asset_id+": license not on allowlist: "+item.license_id);
 if(!/^https:\/\//i.test(item.source_url))throw new Error(item.asset_id+": source_url must be HTTPS");
 if(!/^[a-f0-9]{64}$/i.test(item.sha256_expected))throw new Error(item.asset_id+": expected SHA-256 must be pinned");
 const dest=path.posix.normalize(item.destination_file);
 if(!dest.startsWith("obywatel-android/app/src/main/assets/institutions/")||dest.includes("..")||!dest.toLowerCase().endsWith(".svg"))throw new Error(item.asset_id+": destination must be a safe SVG path under app institutional assets");
 const response=await fetch(item.source_url,{redirect:"follow"});
 if(!response.ok)throw new Error(item.asset_id+": download failed with HTTP "+response.status);
 const bytes=Buffer.from(await response.arrayBuffer());
 if(bytes.length===0||bytes.length>12*1024*1024)throw new Error(item.asset_id+": invalid file size");
 const actual=crypto.createHash("sha256").update(bytes).digest("hex");
 if(actual.toLowerCase()!==item.sha256_expected.toLowerCase())throw new Error(item.asset_id+": SHA-256 mismatch; expected "+item.sha256_expected+" got "+actual);
 const svg=bytes.toString("utf8");
 if(!/<svg[\s>]/i.test(svg)||/<!DOCTYPE|<!ENTITY|<script\b|<foreignObject\b|javascript:|<iframe\b|<object\b|<embed\b/i.test(svg)||/\b(?:href|xlink:href)\s*=\s*["'](?:https?:|\/\/|data:)/i.test(svg))throw new Error(item.asset_id+": SVG failed active-content/security checks");
 const target=path.join(root,dest);
 fs.mkdirSync(path.dirname(target),{recursive:true});
 fs.writeFileSync(target,bytes);
 manifest.push({assetId:item.asset_id,organization:item.organization,group:item.group,sourceUrl:item.source_url,sourcePageUrl:item.source_page_url,license:item.license_id,licenseUrl:item.license_url,attribution:item.attribution,approvedBy:item.approved_by,sha256:actual,byteSize:bytes.length,mimeType:"image/svg+xml",usageStatus:"redistributable"});
 console.log("Imported "+item.asset_id+" -> "+dest+" ("+bytes.length+" bytes)");
}
fs.mkdirSync("docs/identity-assets",{recursive:true});
fs.writeFileSync("docs/identity-assets/imported-symbol-assets-manifest.json",JSON.stringify({schemaVersion:1,assets:manifest},null,2)+"\n");
