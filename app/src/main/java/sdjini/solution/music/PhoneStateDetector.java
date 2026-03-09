package sdjini.solution.music;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import sdjini.solution.intent.MusicControl;
import sdjini.solution.log.Level;
import sdjini.solution.log.Logger;
import sdjini.solution.log.Tags;

public class PhoneStateDetector {
    private AudioManager audioManager;
    private Context context;
    private Object audioFocusRequest;
    private Logger logger;
    public PhoneStateDetector(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        logger = new Logger(context);
    }
    public void startListener(OnPhoneStateChangedListener phoneListener){
        LocalBroadcastManager lb = LocalBroadcastManager.getInstance(context);
        logger.printAndWrite(Level.INFO, new Tags.ListenerTag.PhoneStateListener(), "Start Listener");
        AudioManager.OnAudioFocusChangeListener listener = focusChange -> {
            switch (focusChange){
                case AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> phoneListener.onPhoneStart();
                case AudioManager.AUDIOFOCUS_GAIN -> phoneListener.onPhoneEnd();
            }
        };

        AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(listener)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .build();
        audioFocusRequest = request;
        audioManager.requestAudioFocus(request);
        logger.printAndWrite(Level.INFO, new Tags.ListenerTag.PhoneStateListener(), "Request Audio Focus");
    }

    public interface OnPhoneStateChangedListener {
        void onPhoneStart(); // 来电/拨号
        void onPhoneEnd();   // 挂断
    }
}
