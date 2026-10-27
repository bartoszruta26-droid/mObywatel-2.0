package com.dokumentownia.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dokumentownia.app.DokumentowniaApp;
import com.dokumentownia.app.R;
import com.dokumentownia.app.data.DocMeta;
import com.dokumentownia.app.data.DocStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Okno importu: dziala jako cel "Udostepnij w / Dokumentownia" oraz jako obsluga
 * ACTION_VIEW dla plikow. Dzieli DocStore z MainActivity, wiec zaimportowany dokument
 * pojawia sie na liscie natychmiast.
 */
public class ImportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        DocStore store = ((DokumentowniaApp) getApplication()).getStore();
        List<Uri> uris = incomingUris(getIntent());

        if (uris.isEmpty()) {
            findViewById(R.id.import_pick).setOnClickListener(v ->
                    new Intent(Intent.ACTION_GET_CONTENT).setType("*/*"));
            Toast.makeText(this, R.string.import_no_data, Toast.LENGTH_SHORT).show();
            return;
        }

        int ok = 0;
        List<String> failed = new ArrayList<>();
        for (Uri u : uris) {
            try {
                DocMeta m = store.importFrom(this, u, null, "");
                ViewerActivity.open(this, m);
                ok++;
            } catch (Exception e) {
                failed.add(String.valueOf(e.getMessage()));
            }
        }

        String msg = ok > 0 ? getString(R.string.import_multi_ok, ok)
                : getString(R.string.import_fail, failed.isEmpty() ? "?" : failed.get(0));
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }

    private static List<Uri> incomingUris(Intent intent) {
        List<Uri> out = new ArrayList<>();
        if (intent == null) return out;
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            Uri u = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (u != null) out.add(u);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Uri> us = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (us != null) out.addAll(us);
        } else if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null) {
            out.add(intent.getData());
        }
        return out;
    }
}
