package com.dokumentownia.app.data;

/**
 * Obslugiwane typy dokumentow. enum ordynarny, bez bibliotek - latwy do rozszerzania
 * (lista zadan Z-08..Z-12 w README dotyczy nowych formatow).
 */
public enum DocKind {
    PDF("application/pdf", "PDF"),
    IMAGE("image/*", "Obraz"),
    TEXT("text/plain", "Tekst"),
    HTML("text/html", "HTML"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Dokument"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Arkusz"),
    ZIP("application/zip", "Pakiet"),
    UNKNOWN("application/octet-stream", "Inny");

    private final String mime;
    private final String label;

    DocKind(String mime, String label) {
        this.mime = mime;
        this.label = label;
    }

    public String mime() {
        return mime;
    }

    public String label() {
        return label;
    }

    public static DocKind fromMime(String mime) {
        if (mime == null) return UNKNOWN;
        String m = mime.toLowerCase();
        for (DocKind k : values()) {
            if (k == UNKNOWN) continue;
            if (k.mime.equals(m)) return k;
            if (k.mime.endsWith("/*") && m.startsWith(k.mime.substring(0, k.mime.length() - 1))) return k;
        }
        if (m.startsWith("image/")) return IMAGE;
        if (m.startsWith("text/")) return TEXT;
        return UNKNOWN;
    }

    /** Rozszerzenie pliku -> najprawdopodobniejszy typ (gdy MIME jest niedostepny). */
    public static DocKind fromName(String name) {
        if (name == null) return UNKNOWN;
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0 ? name.substring(dot + 1).toLowerCase() : "";
        switch (ext) {
            case "pdf": return PDF;
            case "png": case "jpg": case "jpeg": case "webp": case "gif": case "bmp":
                return IMAGE;
            case "txt": case "log": case "md": return TEXT;
            case "htm": case "html": return HTML;
            case "docx": return DOCX;
            case "xlsx": case "xls": return XLSX;
            case "zip": return ZIP;
            default: return UNKNOWN;
        }
    }
}
