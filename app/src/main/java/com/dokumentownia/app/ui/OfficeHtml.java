package com.dokumentownia.app.ui;

import android.content.Context;

import com.dokumentownia.app.data.DocKind;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Renderowanie DOCX / XLSX do HTML bez bibliotek zewnetrznych (czysty JDK: ZipInputStream
 * + DOM). Wyjscie trafia do WebView. To szkic - pelna obsluga formatowania to zadania
 * Z-10 (DOCX) i Z-11 (XLSX) w README.
 */
final class OfficeHtml {

    private OfficeHtml() { }

    static String render(Context ctx, File f, DocKind kind) {
        try {
            String html = kind == DocKind.DOCX ? docx(f) : xlsx(f);
            return wrap(html);
        } catch (Exception e) {
            return wrap("<p><b>Nie udalo sie odczytac pliku.</b><br>" + esc(String.valueOf(e.getMessage())) + "</p>");
        }
    }

    private static String wrap(String body) {
        return "<!DOCTYPE html><html lang=\"pl\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<style>"
                + "body{font-family:sans-serif;margin:14px;line-height:1.5;color:#1b1b1b;background:#fbf7ef}"
                + "h1,h2,h3{font-family:serif;color:#7a1f2b}"
                + "table{border-collapse:collapse;width:100%;margin:8px 0}"
                + "td,th{border:1px solid #c9bda6;padding:4px 6px;font-size:13px;vertical-align:top}"
                + "th{background:#efe3cd}"
                + ".page{border-left:3px solid #7a1f2b;padding-left:10px;margin-bottom:18px}"
                + "</style></head><body>" + body + "</body></html>";
    }

    // ---------------- DOCX ----------------

    private static String docx(File f) throws Exception {
        String xml = readZipEntry(f, "word/document.xml");
        Document d = parse(xml);
        StringBuilder sb = new StringBuilder();
        NodeList paras = d.getElementsByTagName("w:p");
        for (int i = 0; i < paras.getLength(); i++) {
            Element p = (Element) paras.item(i);
            String style = paragraphStyle(p);
            StringBuilder text = new StringBuilder();
            NodeList runs = p.getElementsByTagName("w:r");
            for (int r = 0; r < runs.getLength(); r++) {
                Node run = runs.item(r);
                boolean bold = has(run, "w:b");
                boolean italic = has(run, "w:i");
                NodeList ts = ((Element) run).getElementsByTagName("w:t");
                for (int t = 0; t < ts.getLength(); t++) {
                    String chunk = ts.item(t).getTextContent();
                    if (chunk == null) continue;
                    String e = esc(chunk);
                    if (bold) e = "<b>" + e + "</b>";
                    if (italic) e = "<i>" + e + "</i>";
                    text.append(e);
                }
            }
            String cls = style.startsWith("Heading") || style.startsWith("Nag") ? "h" + digit(style) : "";
            if (!cls.isEmpty()) sb.append("<").append(cls).append(">").append(text).append("</").append(cls).append(">");
            else if (text.length() > 0) sb.append("<p>").append(text).append("</p>");
            else sb.append("<p style='height:6px'></p>");
        }
        return sb.length() == 0 ? "<p><i>Pusty dokument.</i></p>" : sb.toString();
    }

    private static String paragraphStyle(Element p) {
        NodeList ps = p.getElementsByTagName("w:pStyle");
        if (ps.getLength() == 0) return "";
        return ((Element) ps.item(0)).getAttribute("w:val");
    }

    private static int digit(String s) {
        for (char c : s.toCharArray()) if (Character.isDigit(c)) return Math.min(6, Character.getNumericValue(c));
        return 2;
    }

    private static boolean has(Node parent, String tag) {
        NodeList list = ((Element) parent).getElementsByTagName(tag);
        if (list.getLength() == 0) return false;
        String off = ((Element) list.item(0)).getAttribute("w:val");
        return !"0".equals(off) && !"false".equalsIgnoreCase(off);
    }

    // ---------------- XLSX ----------------

    private static String xlsx(File f) throws Exception {
        List<String> shared = sharedStrings(f);
        StringBuilder sb = new StringBuilder();
        List<String> sheets = sheetPaths(f);
        int n = 1;
        for (String path : sheets) {
            String xml = readZipEntry(f, path);
            sb.append("<div class='page'><h3>Arkusz ").append(n++).append("</h3>");
            sb.append(table(parse(xml), shared));
            sb.append("</div>");
        }
        return sb.length() == 0 ? "<p><i>Brak arkuszy.</i></p>" : sb.toString();
    }

    private static List<String> sheetPaths(File f) throws Exception {
        List<String> out = new ArrayList<>();
        try (ZipInputStream zin = new ZipInputStream(new java.io.FileInputStream(f))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                String n = e.getName();
                if (n.startsWith("xl/worksheets/sheet") && n.endsWith(".xml")) out.add(n);
            }
        }
        out.sort(String::compareTo);
        return out;
    }

    private static String table(Document d, List<String> shared) {
        StringBuilder sb = new StringBuilder("<table>");
        NodeList rows = d.getElementsByTagName("row");
        int limit = Math.min(rows.getLength(), 500);
        for (int i = 0; i < limit; i++) {
            Element row = (Element) rows.item(i);
            sb.append("<tr>");
            NodeList cells = row.getElementsByTagName("c");
            int col = 0;
            for (int c = 0; c < cells.getLength(); c++) {
                Element cell = (Element) cells.item(c);
                int target = colIndex(cell.getAttribute("r"));
                while (col < target && col < 64) { sb.append("<td></td>"); col++; }
                String type = cell.getAttribute("t");
                String v = childText(cell, "v");
                String display;
                if ("s".equals(type)) {
                    int idx = parseInt(v, -1);
                    display = idx >= 0 && idx < shared.size() ? shared.get(idx) : "";
                } else if ("inlineStr".equals(type)) {
                    display = childText(cell, "is");
                } else {
                    display = v;
                    if (display.matches("-?\\d+\\.0+")) display = display.replaceAll("\\.0+$", "");
                }
                sb.append(i == 0 ? "<th>" : "<td>").append(esc(display)).append(i == 0 ? "</th>" : "</td>");
                col++;
            }
            sb.append("</tr>");
        }
        return sb.append("</table>").toString();
    }

    private static int colIndex(String ref) {
        int idx = 0;
        for (char ch : ref.toUpperCase().toCharArray()) {
            if (ch < 'A' || ch > 'Z') break;
            idx = idx * 26 + (ch - 'A' + 1);
        }
        return idx - 1;
    }

    private static List<String> sharedStrings(File f) throws Exception {
        List<String> out = new ArrayList<>();
        String xml = readZipEntry(f, "xl/sharedStrings.xml");
        if (xml == null) return out;
        NodeList sis = parse(xml).getElementsByTagName("si");
        for (int i = 0; i < sis.getLength(); i++) {
            StringBuilder sb = new StringBuilder();
            NodeList ts = ((Element) sis.item(i)).getElementsByTagName("t");
            for (int t = 0; t < ts.getLength(); t++) {
                String s = ts.item(t).getTextContent();
                if (s != null) sb.append(s);
            }
            out.add(sb.toString());
        }
        return out;
    }

    // ---------------- narzedzia ZIP / XML ----------------

    private static String readZipEntry(File f, String wanted) throws Exception {
        try (ZipInputStream zin = new ZipInputStream(new java.io.FileInputStream(f))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                if (e.getName().equals(wanted)) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = zin.read(buf)) > 0) bos.write(buf, 0, r);
                    return new String(bos.toByteArray(), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
        b.setNamespaceAware(true);
        b.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder db = b.newDocumentBuilder();
        return db.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String childText(Element e, String tag) {
        NodeList l = e.getElementsByTagName(tag);
        if (l.getLength() == 0) return "";
        if ("is".equals(tag)) {
            StringBuilder sb = new StringBuilder();
            NodeList ts = ((Element) l.item(0)).getElementsByTagName("t");
            for (int i = 0; i < ts.getLength(); i++) sb.append(ts.item(i).getTextContent());
            return sb.toString();
        }
        String t = l.item(0).getTextContent();
        return t == null ? "" : t;
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
