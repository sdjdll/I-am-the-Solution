package sdjini.solution.music;

import static android.app.NotificationManager.IMPORTANCE_LOW;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Switch;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sdjini.solution.MainActivity;
import sdjini.solution.R;
import sdjini.solution.file_core.MusicFile;
import sdjini.solution.intent.MusicControl;
import sdjini.solution.intent.MusicNext;
import sdjini.solution.intent.MusicPlayInit;
import sdjini.solution.intent.MusicPrevious;
import sdjini.solution.intent.MusicSwitch;
import sdjini.solution.intent.PlayerModeSwitch;
import sdjini.solution.intent.UpdateProgress;
import sdjini.solution.log.Level;
import sdjini.solution.log.Logger;
import sdjini.solution.log.Tags;

public class MusicPlay extends Service {
    public static int playN = 0;

    public enum State{
        play,
        stop,
        pause;
    }
    private static List<MusicFile> playList;
    public State state = State.stop;
    private MediaPlayer mediaPlayer;
    private MusicFile nowPlaying;
    private Logger logger;
    private NotificationCompat.Builder foregroundNotification;
    private Handler mainHandler;
    private final BroadcastReceiver ControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logger.printAndWrite(Level.INFO, new Tags.MusicTag.IntentTrans(), "MusicPlay","Action:"+intent.getAction());
            try{
                switch (intent.getAction()) {
                    case MusicControl.Action -> Control();
                    case MusicPrevious.Action -> Previous();
                    case MusicNext.Action -> Next();
                    case MusicSwitch.Action -> Switch(intent.getIntExtra(MusicSwitch.Name.ChoseNumber, -1));
                    case PlayerModeSwitch.Action -> switchMode(intent.getStringExtra(PlayerModeSwitch.Name.Mode), intent.getBooleanExtra(PlayerModeSwitch.Name.State, false));
                    case null, default -> logger.printAndWrite(Level.ERROR, new Tags.MusicTag.IntentTrans(), "Unknow Intent", new IllegalArgumentException());
                }
            }catch (IndexOutOfBoundsException e){
                logger.printAndWrite(Level.FATAL,new Tags.MusicTag.MusicManage(),"No Music", e);
            }
        }

        private void switchMode(String mode, boolean state) {
            logger.printAndWrite(Level.STEP,new Tags.MusicTag.IntentTrans(),"switchMode",mode + " " + state);
            if (!state) {
                mediaPlayer.setOnCompletionListener(mp -> nowPlaying.now = 0);
                logger.printAndWrite(Level.INFO,new Tags.MusicTag.ModeSwitch(),"Now Mode Default");
            }else switch (mode) {
                case PlayerModeSwitch.Mode.Repeat -> mediaPlayer.setOnCompletionListener(mp -> {
                    nowPlaying.now = 0;
                    restart();
                    logger.printAndWrite(Level.INFO,new Tags.MusicTag.ModeSwitch(), "Now Mode Repeat");
                });
                case PlayerModeSwitch.Mode.Loop -> mediaPlayer.setOnCompletionListener(mp -> {
                    nowPlaying.now = 0;
                    next();
                    logger.printAndWrite(Level.INFO,new Tags.MusicTag.ModeSwitch(), "Now Mode Loop");
                });
                case PlayerModeSwitch.Mode.Random -> mediaPlayer.setOnCompletionListener(mp -> {
                    nowPlaying.now = 0;
                    Random random = new Random();
                    playN = random.nextInt(playList.size());
                    nowPlaying = playList.get(playN);
                    setPlayer(nowPlaying);
                    play();
                    logger.printAndWrite(Level.INFO,new Tags.MusicTag.ModeSwitch(), "Now Mode Random");
                });
            }
        }
        private void Control(){
            logger.printAndWrite(Level.STEP, new Tags.MusicTag.MusicManage(), "Control", "playerState:" + state.name());
            if (playList.isEmpty()){
                logger.printAndWrite(Level.ERROR, new Tags.MusicTag.MusicManage(), playList.isEmpty() ? "No Music" : "Unknow IndexOutOfBounds");
                return;
            }
            switch (state){
                case stop -> {
                    setPlayer(playList.get(playN));
                    play();
                }
                case play -> pause();
                case pause -> restart();
                case null, default -> logger.printAndWrite(Level.ERROR, new Tags.MusicTag.MusicManage(), "Unknow State", new IllegalStateException());
            }
        }
        private void Switch(int num){
            stop();
            playN = num;
            setPlayer(playList.get(playN));
            play();
        }
        private void Next(){
            next();
        }
        private void Previous(){
            previous();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        logger = new Logger(this);
        playList = MainActivity.musicList;
        logger.printAndWrite(Level.INFO,new Tags.MusicTag.MusicManage(), "Sync MusicFile", Arrays.toString(playList.toArray()));
        mediaPlayer = new MediaPlayer();
        IntentFilter iF = new IntentFilter();
        iF.addAction(MusicNext.Action);
        iF.addAction(MusicControl.Action);
        iF.addAction(MusicPrevious.Action);
        iF.addAction(MusicSwitch.Action);
        iF.addAction(PlayerModeSwitch.Action);
        LocalBroadcastManager.getInstance(this).registerReceiver(ControlReceiver, iF);
        logger.printAndWrite(Level.STEP,new Tags.MusicTag.MusicManage(),"Register Receiver: ControlReceiver");
        foreground();
        mediaPlayer.setOnCompletionListener(mp -> {
            try {
                nowPlaying.now = 0;
            } catch (NullPointerException e) {
                logger.printAndWrite(Level.ERROR, new Tags.MusicTag.MusicManage(), "How can this null??", e);
            }
        });
        mainHandler = new Handler(Looper.getMainLooper());
        logger.printAndWrite(Level.INFO, new Tags.MusicTag.MusicManage(), "MusicPlay Created");
    }

    public static void updateMusicList(List<MusicFile> list){
        playList = list;
    }
    private void foreground(){
        foregroundNotification = new NotificationCompat.Builder(this, "ForegroundPlaying")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel("ForegroundPlaying","ForegroundPlaying",IMPORTANCE_LOW));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(1, foregroundNotification.build(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        logger.printAndWrite(Level.INFO,new Tags.MusicTag.MusicManage(),"Start Foreground");
    }
    private void sendNotification(String Title, String Content){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder nc = foregroundNotification;
        nc.setContentTitle(Title).setContentText(Content);
        manager.notify(1,foregroundNotification.build());
        logger.printAndWrite(Level.STEP,new Tags.MusicTag.MusicManage(),"Update Notification");
    }

    private void setPlayer(MusicFile musicFile){
        logger.printAndWrite(Level.STEP, new Tags.MusicTag.MusicManage(), "setPlayer", "state:" + state.name());
        try {
            nowPlaying = musicFile;
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicFile.getPath());
            mediaPlayer.prepare();
            logger.printAndWrite(Level.INFO,new Tags.MusicTag.MusicManage(),"GetData",musicFile.Title ," "+ musicFile.getLengthAsync());
        } catch (IOException ignored) {}
    }
    /// @deprecated 该public方法已废弃，改为Intent交互
    public void setPlayer(){
        setPlayer(playList.get(playN));
    }
    private void play(){
        if (!mediaPlayer.isPlaying())
            mediaPlayer.start();
        sendNotification(getString(R.string.app_name),this.getString(R.string.IsPlaying) + " " + nowPlaying.Title);
        state = State.play;
        mainHandler.removeCallbacks(progressRunnable);
        mainHandler.post(progressRunnable);
    }
    private void pause(){
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        state = State.pause;
        sendNotification(getString(R.string.app_name),this.getString(R.string.IsPause) + " " + nowPlaying.Title);
        mainHandler.removeCallbacks(progressRunnable);
    }
    private void restart(){
        mediaPlayer.seekTo(nowPlaying.now);
        play();
        state = State.play;
        mainHandler.post(progressRunnable);
    }

    private void stop(){
        mediaPlayer.stop();
        try{
            playList.get(playN).now = 0;
        }catch (IndexOutOfBoundsException e){
            logger.printAndWrite(Level.ERROR, new Tags.MusicTag.MusicManage(), "PlayN is null",e);
        }
        state = State.stop;
        mainHandler.removeCallbacks(progressRunnable);
    }
    private void next(){
        stop();
        playN++;
        if (playN >= playList.size()) playN = 0;
        if (playList.isEmpty()){
            logger.printAndWrite(Level.ERROR, new Tags.MusicTag.MusicManage(), playList.isEmpty() ? "No Music" : "Unknow IndexOutOfBounds");
            return;
        }
        setPlayer(playList.get(playN));
        play();
        logger.printAndWrite(Level.INFO,new Tags.MusicTag.MusicManage(),"Next", nowPlaying.Title);
    }
    private void previous(){
        stop();
        playN--;
        if (playN < 0) playN = playList.size() - 1;
        if (playList.isEmpty()){
            logger.printAndWrite(Level.ERROR, new Tags.MusicTag.MusicManage(), playList.isEmpty() ? "No Music" : "Unknow IndexOutOfBounds");
            return;
        }
        setPlayer(playList.get(playN));
        play();
        logger.printAndWrite(Level.INFO,new Tags.MusicTag.MusicManage(),"Previous", nowPlaying.Title);
    }

    private final Handler handler = new Handler();

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();

                nowPlaying.now = currentPosition;
                nowPlaying.length = duration;

                UpdateProgress updateProgress = new UpdateProgress();
                updateProgress.putExtra(UpdateProgress.Name.NOW, currentPosition)
                        .putExtra(UpdateProgress.Name.LENGTH, duration)
                        .putExtra(UpdateProgress.Name.MUSIC_NAME, nowPlaying.Title);

                LocalBroadcastManager.getInstance(MusicPlay.this).sendBroadcast(updateProgress);

                logger.printAndWrite(Level.STEP, new Tags.MusicTag.MusicManage(), "UpdateProgress");
                handler.removeCallbacks(this);
                handler.postDelayed(this, 500);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ControlReceiver);
        mediaPlayer.release();
        super.onDestroy();
    }
}