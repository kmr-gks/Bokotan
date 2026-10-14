package com.gukos.bokotan;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.*;
import java.io.*;
import java.util.UUID;

/** 大きなZIPをメモリに載せず展開。画面再生成中もForeground Serviceで継続する。 */
public final class ImportService extends Service {
    static volatile boolean busy, completed;
    static volatile String status = "";
    private Thread worker;
    private volatile boolean cancelled;
    private final Handler main = new Handler(Looper.getMainLooper());
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "cancel".equals(intent.getAction())) { cancelled = true; if (!busy) stopSelf(); return START_NOT_STICKY; }
        if (busy) return START_NOT_STICKY;
        busy = true; completed = false; status = "ZIPを読み込み中…";
        startNotification();
        Uri uri = intent == null ? null : intent.getData();
        if (uri == null) { finish(false, "ZIPが選択されていません。"); return START_NOT_STICKY; }
        QuizCreator.stopCurrent();
        Dictionary.QuizData.saveQuizData(this);
        stopService(new Intent(this, PlayerService.class));
        worker = new Thread(() -> {
            File stage = new File(getNoBackupFilesDir(), "import-" + UUID.randomUUID());
            File old = PrivateData.root(this);
            boolean activated = false;
            try {
                // 前回プロセスが終了した場合に残った、一時展開データを除去。
                File[] leftovers = getNoBackupFilesDir().listFiles();
                if (leftovers != null) for (File file : leftovers)
                    if (file.getName().startsWith("import-") && !file.equals(old)) PrivatePackage.delete(file);
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    if (input == null) throw new IOException();
                    final long[] last = {0};
                    PrivatePackage.extract(input, stage, bytes -> {
                        if (cancelled || Thread.currentThread().isInterrupted()) throw new IOException();
                        long time = android.os.SystemClock.elapsedRealtime();
                        if (time - last[0] > 500) {
                            status = "展開済み: " + bytes / (1024 * 1024) + " MB";
                            last[0] = time;
                        }
                    });
                }
                if (cancelled) throw new IOException();
                PrivateData.activate(this, stage);
                activated = true;
                if (old != null && !old.equals(stage)) PrivatePackage.delete(old);
                main.post(() -> {
                    Dictionary.reload(this, () -> finish(true, "個人データを取り込みました。"));
                });
            } catch (Exception error) {
                if (!activated) PrivatePackage.delete(stage);
                main.post(() -> finish(false, cancelled ? "取り込みを中止しました。" : "取り込みに失敗しました。ZIPの形式・空き容量・接続を確認してください。"));
            } finally {
                try { getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch (SecurityException ignored) { }
            }
        }, "PrivateZipImport");
        worker.start();
        return START_NOT_STICKY;
    }
    private void finish(boolean success, String message) {
        status = message; completed = success; busy = false;
        stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
    }
    private void startNotification() {
        var manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel("import", "データ取り込み", NotificationManager.IMPORTANCE_LOW));
        var cancel = PendingIntent.getService(this, 201, new Intent(this, ImportService.class).setAction("cancel"), PendingIntent.FLAG_IMMUTABLE);
        var open = PendingIntent.getActivity(this, 202, new Intent(this, TabActivity.class), PendingIntent.FLAG_IMMUTABLE);
        var notification = new Notification.Builder(this, "import").setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("個人データを取り込み中").setContentText("アプリ内で進捗を確認できます")
                .setContentIntent(open).setOngoing(true).addAction(android.R.drawable.ic_menu_close_clear_cancel, "中止", cancel).build();
        if (Build.VERSION.SDK_INT >= 29) startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        else startForeground(2, notification);
    }
    @Override public void onTimeout(int startId, int fgsType) {
        cancelled = true;
        status = "取り込みを中断しました。もう一度ZIPを選択してください。";
        stopSelf();
    }
    @Override public void onDestroy() { cancelled = true; if (worker != null) worker.interrupt(); busy = false; super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
