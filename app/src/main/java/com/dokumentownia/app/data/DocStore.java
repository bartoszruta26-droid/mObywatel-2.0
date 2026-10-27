package com.dokumentownia.app.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Przechowywanie dokumentow: pliki w filesDir/documents, metadane jako JSON-lines
 * w filesDir/index.jsonl. Zero baz danych, zero sieci - pelna kontrola i audytowalnosc.
 */
public class DocStore {

    private static final String TAG = "DocStore";
    private static final String DIR = "documents";
    private static final String INDEX = "index.jsonl";

    private final File docDir;
    private final File indexFile;

    public DocStore(Context ctx) {
        docDir = new File(ctx.getFilesDir(), DIR);
        if (!docDir.exists() && !docDir.mkdirs()) {
            Log.e(TAG, "Nie udalo sie utworzyc katalogu dokumentow");
        }
        indexFile = new File(ctx.getFilesDir(), INDEX);
    }

    public File dir() {
        return docDir;
    }

    /** Importuje dokument z dowolnego Uri (SAF, share, kamera) do privatu aplikacji. */
    public synchronized DocMeta importFrom(Context ctx, Uri src, String title, String category)
            throws IOException {
        String name = queryName(ctx, src);
        String mime = ctx.getContentResolver().getType(src);
        DocKind kind = DocKind.fromMime(mime != null ? mime : DocKind.fromName(name).mime());

        String id = "D" + System.currentTimeMillis();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
        File target = new File(docDir, id + ext);

        try (InputStream in = ctx.getContentResolver().openInputStream(src);
             OutputStream out = new FileOutputStream(target)) {
            if (in == null) throw new IOException("Nie mozna odczytac " + src);
            byte[] buf = new byte[16384];
            int r;
            while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
        }

        DocMeta meta = new DocMeta();
        meta.id = id;
        meta.title = (title == null || title.trim().isEmpty()) ? displayName(name) : title.trim();
        meta.fileName = target.getName();
        meta.mime = mime != null ? mime : kind.mime();
        meta.kind = kind.name();
        meta.category = category == null ? "" : category;
        meta.size = target.length();
        meta.importedAt = System.currentTimeMillis();
        meta.pages = guessPages(kind, target);
        meta.thumbnail = makeThumb(kind, target);

        append(meta);
        return meta;
    }

    public synchronized List<DocMeta> list() {
        List<DocMeta> out = new ArrayList<>();
        if (!indexFile.exists()) return out;
        for (String line : readAll()) {
            DocMeta m = DocMeta.fromJson(line);
            if (m != null && new File(docDir, m.fileName).exists()) out.add(m);
        }
        Collections.sort(out, (a, b) -> Long.compare(b.importedAt, a.importedAt));
        return out;
    }

    public synchronized void update(DocMeta meta) {
        List<DocMeta> all = list();
        StringBuilder sb = new StringBuilder();
        for (DocMeta m : all) {
            if (m.id.equals(meta.id)) sb.append(meta.toJson()).append('\n');
            else sb.append(m.toJson()).append('\n');
        }
        writeAll(sb.toString());
    }

    public synchronized boolean delete(String id) {
        boolean removed = false;
        for (DocMeta m : list()) {
            if (m.id.equals(id)) {
                File f = file(m);
                if (f.exists() && !f.delete()) Log.w(TAG, "Nie usunieto pliku " + f);
                removed = true;
            }
        }
        List<DocMeta> rest = list();
        StringBuilder sb = new StringBuilder();
        for (DocMeta m : rest) if (!m.id.equals(id)) sb.append(m.toJson()).append('\n');
        writeAll(sb.toString());
        return removed;
    }

    public File file(DocMeta meta) {
        return new File(docDir, meta.fileName);
    }

    public File thumbFile(DocMeta meta) {
        return new File(docDir, meta.id + ".thumb.jpg");
    }

    private synchronized void append(DocMeta meta) {
        try (OutputStream os = new FileOutputStream(indexFile, true)) {
            os.write((meta.toJson() + "\n").getBytes("UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "Zapis indeksu nieudany", e);
        }
    }

    private List<String> readAll() {
        List<String> lines = new ArrayList<>();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(new FileInputStreamSafe(indexFile), "UTF-8"))) {
            String l;
            while ((l = br.readLine()) != null) if (!l.trim().isEmpty()) lines.add(l);
        } catch (IOException e) {
            Log.e(TAG, "Odczyt indeksu nieudany", e);
        }
        return lines;
    }

    private void writeAll(String content) {
        try (OutputStream os = new FileOutputStream(indexFile, false)) {
            os.write(content.getBytes("UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "Nadpisanie indeksu nieudane", e);
        }
    }

    /** Prosty strumien wejscia odporny na brak pliku (indeks moze jeszcze nie istniec). */
    private static class FileInputStreamSafe extends java.io.FileInputStream {
        FileInputStreamSafe(File f) throws IOException {
            super(f.exists() ? f : empty(f));
        }

        private static File empty(File f) {
            File e = new File(f.getParentFile(), "." + f.getName() + ".empty");
            try { if (!e.exists()) e.createNewFile(); } catch (IOException ignored) { }
            return e;
        }
    }

    private int guessPages(DocKind kind, File f) {
        if (kind == DocKind.PDF) {
            // Liczba stron z naglowka PDF jest zawodna; pozostawiamy 0 do zadania Z-09.
            return 0;
        }
        if (kind == DocKind.IMAGE) return 1;
        return 0;
    }

    private String makeThumb(DocKind kind, File f) {
        if (kind != DocKind.IMAGE) return null;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), opt);
        int sample = 1;
        while (opt.outWidth / sample > 256 || opt.outHeight / sample > 256) sample *= 2;
        opt.inJustDecodeBounds = false;
        opt.inSampleSize = sample;
        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), opt);
        if (bmp == null) return null;
        File t = new File(docDir, "T.thumb.tmp");
        try (OutputStream os = new FileOutputStream(t)) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, os);
            String name = f.getName().replaceFirst("\\.[^.]+$", "") + ".thumb.jpg";
            if (!t.renameTo(new File(docDir, name))) return null;
            return name;
        } catch (IOException e) {
            return null;
        } finally {
            if (t.exists()) t.delete();
        }
    }

    private static String displayName(String name) {
        if (name == null || name.isEmpty()) return "Dokument";
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private String queryName(Context ctx, Uri uri) {
        try (android.database.Cursor c = ctx.getContentResolver()
                .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) { }
        String last = uri.getLastPathSegment();
        return last == null ? "dokument" : last;
    }
}
