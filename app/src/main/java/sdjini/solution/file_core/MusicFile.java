package sdjini.solution.file_core;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import sdjini.solution.log.Logger;

public class MusicFile extends File {
    public String Title;

    public int length;
    public int now = 0;
    public String musicFilePath;
    boolean isPrepared = false;
    private final Object syncLock = new Object();
    public MusicFile(String musicFilePath) {
        super(musicFilePath);
        this.musicFilePath = musicFilePath;

        try(MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(musicFilePath);
            Title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) == null ? new File(musicFilePath).getName() : retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        }catch (IOException ignored) {}
        new Thread(()->{
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(musicFilePath);
                mediaPlayer.prepare();
                length = mediaPlayer.getDuration();
                mediaPlayer.release();
                synchronized (syncLock){
                    isPrepared = true;
                    syncLock.notify();
                }
            } catch (IOException ignored) {}}).start();
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
