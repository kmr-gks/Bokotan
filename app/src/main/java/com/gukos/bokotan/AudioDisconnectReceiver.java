package com.gukos.bokotan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import java.util.function.BooleanSupplier;

/** スピーカーへの意図しない切り替えを防ぐための、音声出力切断通知。 */
final class AudioDisconnectReceiver extends BroadcastReceiver {
    private final BooleanSupplier enabled;
    private final Runnable stop;

    AudioDisconnectReceiver(BooleanSupplier enabled, Runnable stop) {
        this.enabled = enabled;
        this.stop = stop;
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent != null && AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())
                && enabled.getAsBoolean()) stop.run();
    }
}
