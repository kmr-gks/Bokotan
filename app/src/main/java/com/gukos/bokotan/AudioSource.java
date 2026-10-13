package com.gukos.bokotan;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;

/** 同梱音声と従来の外部音声を、すべての再生画面で共通に扱う。 */
final class AudioSource {
    static final String ASSET_PREFIX = "asset:///";
    private AudioSource() {}

    static MediaPlayer create(Context context, String path) throws IOException {
        if (path == null) return null;
        if (!path.startsWith(ASSET_PREFIX)) return MediaPlayer.create(context, Uri.parse(path));
        var player = new MediaPlayer();
        try (var source = context.getAssets().openFd(path.substring(ASSET_PREFIX.length()))) {
            player.setDataSource(source.getFileDescriptor(), source.getStartOffset(), source.getLength());
            player.prepare();
            return player;
        } catch (IOException | RuntimeException error) {
            player.release();
            throw error;
        }
    }

    static int load(SoundPool pool, Context context, String path) {
        if (path == null) return 0;
        if (!path.startsWith(ASSET_PREFIX)) return pool.load(path, 1);
        try (var source = context.getAssets().openFd(path.substring(ASSET_PREFIX.length()))) {
            return pool.load(source, 1);
        } catch (IOException error) {
            Log.w("AudioSource", "同梱音声を読み込めません: " + path, error);
            return 0;
        }
    }
}
