package com.dokumentownia.app;

import android.app.Application;

import com.dokumentownia.app.data.DocStore;

/**
 * Dokumentownia - prywatny, calkowicie offline portfel dokumentow.
 * Application trzyma singleton przechowywania, dzieki czemu kazda aktywnosc
 * korzysta z tego samego katalogu danych (priv-app, bez dostepu zewnetrznych procesow).
 */
public class DokumentowniaApp extends Application {

    private DocStore store;

    @Override
    public void onCreate() {
        super.onCreate();
        store = new DocStore(this);
    }

    public DocStore getStore() {
        return store;
    }
}
