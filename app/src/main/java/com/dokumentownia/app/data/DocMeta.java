package com.dokumentownia.app.data;

import org.json.JSONObject;

/**
 * Metadane jednego dokumentu. Serializacja JSON-lines (bez Rooma - mniej magii,
 * latwiejszy audyt i reczne naprawianie indeksu).
 */
public class DocMeta {

    public String id = "";
    public String title = "";
    public String fileName = "";
    public String mime = "";
    public String kind = DocKind.UNKNOWN.name();
    public String category = "";
    public long size = 0L;
    public long importedAt = 0L;
    public int pages = 0;
    public String thumbnail = null;
    public boolean starred = false;
    public String note = "";

    public String toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("title", title);
            o.put("fileName", fileName);
            o.put("mime", mime);
            o.put("kind", kind);
            o.put("category", category);
            o.put("size", size);
            o.put("importedAt", importedAt);
            o.put("pages", pages);
            o.put("thumbnail", thumbnail == null ? "" : thumbnail);
            o.put("starred", starred);
            o.put("note", note);
        } catch (Exception ignored) { }
        return o.toString();
    }

    public static DocMeta fromJson(String line) {
        try {
            JSONObject o = new JSONObject(line);
            DocMeta m = new DocMeta();
            m.id = o.optString("id");
            m.title = o.optString("title");
            m.fileName = o.optString("fileName");
            m.mime = o.optString("mime");
            m.kind = o.optString("kind", DocKind.UNKNOWN.name());
            m.category = o.optString("category");
            m.size = o.optLong("size");
            m.importedAt = o.optLong("importedAt");
            m.pages = o.optInt("pages");
            String t = o.optString("thumbnail");
            m.thumbnail = t.isEmpty() ? null : t;
            m.starred = o.optBoolean("starred");
            m.note = o.optString("note");
            return m;
        } catch (Exception e) {
            return null;
        }
    }

    public DocKind docKind() {
        try {
            return DocKind.valueOf(kind);
        } catch (Exception e) {
            return DocKind.fromMime(mime);
        }
    }

    /** Rozmiar w formacie czytacym dla czlowieka (PL: kB/MB/GB). */
    public String humanSize() {
        if (size < 1024) return size + " B";
        double v = size;
        String[] units = {"kB", "MB", "GB", "TB"};
        int i = -1;
        while (v >= 1024 && i < units.length - 1) { v /= 1024; i++; }
        return String.format(java.util.Locale.US, "%.1f %s", v, units[i]);
    }
}
