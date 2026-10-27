package com.dokumentownia.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dokumentownia.app.DokumentowniaApp;
import com.dokumentownia.app.R;
import com.dokumentownia.app.data.DocMeta;
import com.dokumentownia.app.data.DocStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Glowny ekran: "polska skrzynka dokumentowa". Lista + wyszukiwanie + import.
 */
public class MainActivity extends AppCompatActivity {

    private DocStore store;
    private final List<DocMeta> shown = new ArrayList<>();
    private final List<DocMeta> all = new ArrayList<>();
    private DocAdapter adapter;
    private TextView emptyView;
    private String query = "";

    private final ActivityResultLauncher<String> pickLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) setSupportActionBar(tb);

        store = ((DokumentowniaApp) getApplication()).getStore();
        emptyView = findViewById(R.id.empty);

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocAdapter();
        list.setAdapter(adapter);

        findViewById(R.id.fab_import).setOnClickListener(v -> pickLauncher.launch("*/*"));

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void onPicked(Uri uri) {
        if (uri == null) return;
        try {
            DocMeta m = store.importFrom(this, uri, null, "");
            Toast.makeText(this, getString(R.string.import_ok, m.title), Toast.LENGTH_SHORT).show();
            ViewerActivity.open(this, m);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.import_fail, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
        refresh();
    }

    private void refresh() {
        all.clear();
        all.addAll(store.list());
        filter();
    }

    private void filter() {
        shown.clear();
        String q = query.toLowerCase(Locale.getDefault());
        for (DocMeta m : all) {
            if (q.isEmpty()
                    || m.title.toLowerCase(Locale.getDefault()).contains(q)
                    || m.category.toLowerCase(Locale.getDefault()).contains(q)
                    || m.docKind().label().toLowerCase(Locale.getDefault()).contains(q)) {
                shown.add(m);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        emptyView.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        setTitle(getString(R.string.app_name) + (q.isEmpty() ? "" : "  ·  \"" + query + "\""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            // Docelowe zadanie Z-14: wyszukany pasek wyszukiwania zamiast dialogu.
            new android.app.AlertDialog.Builder(this)
                    .setTitle(R.string.action_search)
                    .setView(R.layout.dialog_search)
                    .setPositiveButton(R.string.ok, (d, w) -> {
                        android.widget.EditText e = ((android.app.Dialog) d)
                                .findViewById(R.id.search_input);
                        query = e == null ? "" : e.getText().toString();
                        filter();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        }
        if (id == R.id.action_about) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.about_text)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DocAdapter extends RecyclerView.Adapter<DocHolder> {

        @NonNull
        @Override
        public DocHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_doc, parent, false);
            return new DocHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DocHolder h, int position) {
            DocMeta m = shown.get(position);
            h.title.setText(m.title);
            h.subtitle.setText(String.format(Locale.getDefault(), "%s · %s · %s",
                    m.docKind().label(), m.humanSize(), m.category.isEmpty() ? "-" : m.category));
            h.badge.setText(m.docKind().label().toUpperCase(Locale.getDefault()));
            h.star.setVisibility(m.starred ? View.VISIBLE : View.INVISIBLE);
            h.itemView.setOnClickListener(v -> ViewerActivity.open(MainActivity.this, m));
            h.itemView.setOnLongClickListener(v -> {
                new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle(m.title)
                        .setItems(new CharSequence[]{
                                getString(R.string.action_star),
                                getString(R.string.action_delete)}, (d, which) -> {
                            if (which == 0) {
                                m.starred = !m.starred;
                                store.update(m);
                            } else {
                                store.delete(m.id);
                            }
                            refresh();
                        })
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return shown.size();
        }
    }

    private static class DocHolder extends RecyclerView.ViewHolder {
        final TextView title, subtitle, badge, star;

        DocHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.doc_title);
            subtitle = itemView.findViewById(R.id.doc_subtitle);
            badge = itemView.findViewById(R.id.doc_badge);
            star = itemView.findViewById(R.id.doc_star);
        }
    }
}
