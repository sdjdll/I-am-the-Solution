package sdjini.solution.file_core;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import sdjini.solution.log.Logger;
import sdjini.solution.music.MusicTool;

public class MusicFile {
    public String Title;

    public int length;
    public int now = 0;
    public Uri musicFileUri;
    boolean isPrepared = false;
    private final Object syncLock = new Object();
    public MusicFile(Context context, @NonNull Uri uri) {
        musicFileUri = uri;
        try(MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(context,uri);
            Title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) == null ? MusicTool.getFileName(context, uri) : retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        }catch (IOException ignored) {}
        new Thread(()->{
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context,uri);
                mediaPlayer.prepare();
                length = mediaPlayer.getDuration();
                mediaPlayer.release();
                synchronized (syncLock){
                    isPrepared = true;
                    syncLock.notify();
                }
            } catch (IOException ignored) {}}).start();
    }

    public Uri getUri(){
        return musicFileUri;
    }

    public String getLength() {
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

        return sdf.format(length);
    }

    public String getNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

        return sdf.format(now);
    }

    public int getLengthAsync(){
        synchronized (syncLock){
            try{
                if (!isPrepared) syncLock.wait();
            } catch (InterruptedException e) {
                return length;
            }
            return length;
        }
    }
}
