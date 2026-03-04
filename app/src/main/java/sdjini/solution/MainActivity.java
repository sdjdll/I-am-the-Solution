package sdjini.solution;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sdjini.solution.file_core.FileManager;
import sdjini.solution.file_core.MusicFile;
import sdjini.solution.intent.MusicControl;
import sdjini.solution.intent.MusicNext;
import sdjini.solution.intent.MusicPrevious;
import sdjini.solution.intent.MusicSeek;
import sdjini.solution.intent.MusicSwitch;
import sdjini.solution.intent.PlayerModeSwitch;
import sdjini.solution.intent.Reflash;
import sdjini.solution.intent.UpdateProgress;
import sdjini.solution.log.Level;
import sdjini.solution.log.Logger;
import sdjini.solution.log.Tags;
import sdjini.solution.music.MusicPlay;
import sdjini.solution.music.MusicTool;


public class MainActivity extends AppCompatActivity {
    private Logger logger;
    private int playerLength;
    private static final String[] Types = {
            ".mp3",                                                                 // MP3
            ".acc", ".mp4", ".m4a", ".3gp",                                         // AAC LC/LTP
            ".flac",                                                                // FLAC
            ".midi", ".mid", ".xmf", ".mxmf", ".rtttl", ".rtx", ".ota", ".imy",     // MIDI
            ".ogg", ".mkv",                                                         // Vorbis
            ".wav"};                                                                // PCM/WAVE
    public static List<MusicFile> musicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        logger = new Logger(this);
        logger.printAndWrite(Level.INFO,new Tags.MainTag.Default(), "OnCreate");

        updateList();

        LocalBroadcastManager lb = LocalBroadcastManager.getInstance(this);

        findViewById(R.id.Fab_Setting).setOnClickListener(view -> startActivity(new Intent(this, SettingActivity.class)));
        findViewById(R.id.Btn_Sync).setOnClickListener(v -> updateList());

        findViewById(R.id.Btn_Next).setOnClickListener(v -> lb.sendBroadcast(new MusicNext()));
        findViewById(R.id.Btn_Previous).setOnClickListener(v -> lb.sendBroadcast(new MusicPrevious()));
        findViewById(R.id.Btn_Contrl).setOnClickListener(v -> lb.sendBroadcast(new MusicControl()));

        SeekBar Sb = findViewById(R.id.Sb_Time);
        Sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                int pN = (int) ((seekBar.getProgress() / 100f) * playerLength);
                StringBuilder sb = new StringBuilder();
                sb.append(MusicTool.getFormatTime(pN, pN <= 3600000 ? "mm:ss" : "H:mm:ss"))
                        .append("/")
                        .append(MusicTool.getFormatTime(playerLength, playerLength <= 3600000 ? "mm:ss" : "H:mm:ss"))
                        .append("");
                TextView tv = findViewById(R.id.Tv_Time);
                tv.setText(sb);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                lb.unregisterReceiver(progressReceiver);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                IntentFilter iF = new IntentFilter();
                iF.addAction(UpdateProgress.Action);
                lb.registerReceiver(progressReceiver, iF);
                lb.sendBroadcast(new MusicSeek((int) ((seekBar.getProgress() / 100f) * playerLength)));
            }
        });
        startForegroundService(new Intent(this, MusicPlay.class));

        Switch Sw_Loop = findViewById(R.id.Sw_Loop);
        Switch Sw_Random = findViewById(R.id.Sw_Random);
        Switch Sw_Repeat = findViewById(R.id.Sw_Repeat);
        Sw_Loop.setOnCheckedChangeListener((btn, b) -> {
            if (b){
                Sw_Random.setChecked(false);
                Sw_Repeat.setChecked(false);
            }
            lb.sendBroadcast(new PlayerModeSwitch(PlayerModeSwitch.Mode.Loop, b));
            logger.printAndWrite(Level.INFO, new Tags.MusicTag.IntentTrans(), "Mode Switch Loop", "" + b);
        });
        Sw_Random.setOnCheckedChangeListener((btn,b)->{
            if(b){
                Sw_Loop.setChecked(false);
                Sw_Repeat.setChecked(false);
            }
            lb.sendBroadcast(new PlayerModeSwitch(PlayerModeSwitch.Mode.Random, b));
            logger.printAndWrite(Level.INFO, new Tags.MusicTag.IntentTrans(), "Mode Switch Random", "" + b);
        });
        Sw_Repeat.setOnCheckedChangeListener((btn,b)->{
            if(b){
                Sw_Loop.setChecked(false);
                Sw_Random.setChecked(false);
            }
            lb.sendBroadcast(new PlayerModeSwitch(PlayerModeSwitch.Mode.Repeat, b));
            logger.printAndWrite(Level.INFO, new Tags.MusicTag.IntentTrans(), "Mode Switch Repeat", "" + b);
        });

        IntentFilter iF = new IntentFilter();
        iF.addAction(UpdateProgress.Action);
        lb.registerReceiver(progressReceiver, iF);
        iF = new IntentFilter();
        iF.addAction(Reflash.Action);
        lb.registerReceiver(Update, iF);
        logger.printAndWrite(Level.INFO, new Tags.MainTag.Default(), "Register Receiver");
    }

    private void updateList(){
        FileManager fm = new FileManager(this);
        musicList = FileManager.filter(fm.listDir().toArray(new File[0]), Types);
        logger.printAndWrite(Level.STEP, new Tags.MainTag.Default(), fm.listDir().toString());
        LinearLayout Linear = findViewById(R.id.Linear_List);
        Linear.removeAllViews();
        int num = 0;
        for (MusicFile mf : musicList){
            TextView textView = new TextView(this);
            textView.setText(mf.Title);
            int temp = num;
            textView.setOnClickListener(v -> LocalBroadcastManager.getInstance(this).sendBroadcast(new MusicSwitch(temp)));
            logger.printAndWrite(Level.STEP, new Tags.MainTag.Default(), "File: " + mf.getPath(), "Title: " + mf.Title);
            Linear.addView(textView);
            num++;
        }
        MusicPlay.updateMusicList(musicList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(progressReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(Update);
    }

    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (Objects.equals(intent.getAction(), UpdateProgress.Action)) {
                logger.printAndWrite(Level.STEP,new Tags.MainTag.UpdateProgress(),"Start Update");
                int playerNow = intent.getIntExtra(UpdateProgress.Name.NOW, 0);
                playerLength = intent.getIntExtra(UpdateProgress.Name.LENGTH, 0);
                StringBuilder sb = new StringBuilder();
                sb.append(MusicTool.getFormatTime(playerNow, playerNow <= 3600000 ? "mm:ss" : "H:mm:ss"))
                        .append("/")
                        .append(MusicTool.getFormatTime(playerLength, playerLength <= 3600000 ? "mm:ss" : "H:mm:ss"))
                        .append("");
                TextView tv = findViewById(R.id.Tv_Time);
                tv.setText(sb);
                tv = findViewById(R.id.Tv_NowPlayingName);
                tv.setText(intent.getStringExtra(UpdateProgress.Name.MUSIC_NAME));
                SeekBar Sb = findViewById(R.id.Sb_Time);
                Sb.setProgress(Math.round((playerNow / (float) playerLength) * 100));
                logger.printAndWrite(Level.STEP,new Tags.MainTag.Default(),"onUpdate");
            }
        }
    };
    private final BroadcastReceiver Update = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateList();
        }
    };
}