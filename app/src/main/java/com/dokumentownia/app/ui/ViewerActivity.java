package com.dokumentownia.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dokumentownia.app.DokumentowniaApp;
import com.dokumentownia.app.R;
import com.dokumentownia.app.data.DocKind;
import com.dokumentownia.app.data.DocMeta;
import com.dokumentownia.app.data.DocStore;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Podglad dokumentu. Dobiera widok do typu: PDF, obraz, tekst, HTML, DOCX/XLSX (offline).
 */
public class ViewerActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "doc_id";

    public static void open(Context ctx, DocMeta meta) {
        Intent i = new Intent(ctx, ViewerActivity.class);
        i.putExtra(EXTRA_ID, meta.id);
        ctx.startActivity(i);
    }

    private DocMeta meta;
    private DocStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        store = ((DokumentowniaApp) getApplication()).getStore();
        String id = getIntent().getStringExtra(EXTRA_ID);
        for (DocMeta m : store.list()) if (m.id.equals(id)) meta = m;

        if (meta == null) {
            Toast.makeText(this, R.string.doc_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(meta.title);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView info = findViewById(R.id.viewer_info);
        info.setText(getString(R.string.viewer_meta,
                meta.docKind().label(), meta.humanSize(),
                meta.pages > 0 ? String.valueOf(meta.pages) : "?",
                meta.category.isEmpty() ? "-" : meta.category));

        render(new File(store.dir(), meta.fileName), meta.docKind());
    }

    private void render(File f, DocKind kind) {
        ImageView image = findViewById(R.id.viewer_image);
        WebView web = findViewById(R.id.viewer_web);
        TextView text = findViewById(R.id.viewer_text);

        switch (kind) {
            case PDF:
                showPdf(f);
                break;
            case IMAGE:
                image.setVisibility(android.view.View.VISIBLE);
                image.setImageBitmap(decodeSampled(f, 2048));
                break;
            case TEXT:
                text.setVisibility(android.view.View.VISIBLE);
                text.setHorizontallyScrolling(false);
                text.setText(readString(f));
                break;
            case HTML:
                web.setVisibility(android.view.View.VISIBLE);
                web.getSettings().setJavaScriptEnabled(false);
                web.loadDataWithBaseURL("file:///android_asset/", readString(f),
                        "text/html", "UTF-8", null);
                break;
            case DOCX:
            case XLSX:
                web.setVisibility(android.view.View.VISIBLE);
                web.getSettings().setJavaScriptEnabled(false);
                web.loadDataWithBaseURL(null, OfficeHtml.render(this, f, kind),
                        "text/html", "UTF-8", null);
                break;
            default:
                text.setVisibility(android.view.View.VISIBLE);
                text.setText(getString(R.string.viewer_unsupported, meta.mime));
        }
    }

    /**
     * PDF bez bibliotek zewnetrznych: renderujemy pierwsza strone przez PdfRenderer
     * (Android 5+) i prezentujemy jako obraz. Wielostronicowosc - zadanie Z-09.
     */
    private void showPdf(File f) {
        try {
            android.os.ParcelFileDescriptor pfd =
                    android.os.ParcelFileDescriptor.open(f, android.os.ParcelFileDescriptor.MODE_READ_ONLY);
            android.graphics.pdf.PdfRenderer renderer = new android.graphics.pdf.PdfRenderer(pfd);
            android.graphics.pdf.PdfRenderer.Page page = renderer.openPage(0);
            int scale = Math.max(1, Math.min(3, 2000 / Math.max(1, page.getWidth())));
            Bitmap bmp = Bitmap.createBitmap(page.getWidth() * scale, page.getHeight() * scale,
                    Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bmp);
            canvas.drawColor(android.graphics.Color.WHITE);
            page.render(bmp, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            int count = renderer.getPageCount();
            renderer.close();
            pfd.close();

            ImageView image = findViewById(R.id.viewer_image);
            image.setVisibility(android.view.View.VISIBLE);
            image.setImageBitmap(bmp);
            meta.pages = count;
            store.update(meta);
            findViewById(R.id.viewer_info).setVisibility(android.view.View.VISIBLE);
        } catch (Exception e) {
            TextView text = findViewById(R.id.viewer_text);
            text.setVisibility(android.view.View.VISIBLE);
            text.setText(getString(R.string.viewer_pdf_fail, String.valueOf(e.getMessage())));
        }
    }

    private static Bitmap decodeSampled(File f, int maxDim) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), o);
        int sample = 1;
        while (Math.max(o.outWidth, o.outHeight) / sample > maxDim) sample *= 2;
        o.inJustDecodeBounds = false;
        o.inSampleSize = sample;
        Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), o);
        return b == null ? Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) : b;
    }

    private static String readString(File f) {
        try {
            return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
