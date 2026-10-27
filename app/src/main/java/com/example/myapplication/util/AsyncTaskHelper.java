package com.example.myapplication.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Exemple d'AsyncTask (deprecated depuis API 30) + alternative moderne avec Executor.
 *
 * NOTE : AsyncTask est obsolète. Utilisez l'Executor ci-dessous en production.
 */
public class AsyncTaskHelper {

    // ===== METHODE 1 : AsyncTask (deprecated) =====
    @SuppressWarnings("deprecation")
    public static class LoadItemsTask extends AsyncTask<Void, Void, String> {
        private final Callback callback;
        public interface Callback { void onResult(String result); }
        public LoadItemsTask(Callback cb) { this.callback = cb; }

        @Override protected String doInBackground(Void... v) {
            // Simuler un chargement réseau
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            return "Données chargées";
        }
        @Override protected void onPostExecute(String result) { callback.onResult(result); }
    }

    // ===== METHODE 2 : Executor (recommandée) =====
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void runInBackground(Runnable task, Runnable onComplete) {
        executor.execute(() -> {
            task.run();
            mainHandler.post(onComplete);
        });
    }
}






















