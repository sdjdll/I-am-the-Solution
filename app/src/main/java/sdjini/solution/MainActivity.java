package sdjini.solution;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import sdjini.solution.file_core.FileManager;
import sdjini.solution.file_core.MusicFile;
import sdjini.solution.file_core.SpManager;
import sdjini.solution.intent.MusicControl;
import sdjini.solution.intent.MusicNext;
import sdjini.solution.intent.MusicPrevious;
import sdjini.solution.intent.MusicSeek;
import sdjini.solution.intent.MusicSwitch;
import sdjini.solution.intent.MusicVolume;
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
            ".wav",                                                                 // PCM/WAVE
            ".m4s"                                                                  // BiliBili
    };
    public static List<MusicFile> musicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SpManager sp = new SpManager(this);
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
        ImageButton Btn_Control = findViewById(R.id.Btn_Contrl);
        Btn_Control.setOnClickListener(v -> lb.sendBroadcast(new MusicControl()));
        Btn_Control.setOnLongClickListener(v -> {
            lb.sendBroadcast(new MusicSwitch(0));
            return true;
        });

        SeekBar Sb_Left = findViewById(R.id.Sb_Left);
        SeekBar Sb_Right = findViewById(R.id.Sb_Right);
        Sb_Left.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {lb.sendBroadcast(new MusicVolume(progress / 100f, Sb_Right.getProgress() / 100f));}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {sp.write(SpManager.Keys.volumeL, seekBar.getProgress());}
        });
        Sb_Right.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {lb.sendBroadcast(new MusicVolume(Sb_Left.getProgress() / 100f, progress / 100f));}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {sp.write(SpManager.Keys.volumeR, seekBar.getProgress());}
        });
        Sb_Left.setProgress(sp.readInt(SpManager.Keys.volumeL, 50));
        Sb_Right.setProgress(sp.readInt(SpManager.Keys.volumeR, 50));

        SeekBar Sb = findViewById(R.id.Sb_Time);
        Sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                StringBuilder sb = new StringBuilder();
                int pN = progress * 1000;
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
                lb.sendBroadcast(new MusicSeek(seekBar.getProgress() * 1000));
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
            sp.write(SpManager.Keys.Mode, PlayerModeSwitch.Mode.Loop);
            logger.printAndWrite(Level.INFO, new Tags.MusicTag.IntentTrans(), "Mode Switch Loop", "" + b);
        });
        Sw_Random.setOnCheckedChangeListener((btn,b)->{
            if(b){
                Sw_Loop.setChecked(false);
                Sw_Repeat.setChecked(false);
            }
            lb.sendBroadcast(new PlayerModeSwitch(PlayerModeSwitch.Mode.Random, b));
            sp.write(SpManager.Keys.Mode, PlayerModeSwitch.Mode.Random);
            logger.printAndWrite(Level.INFO, new Tags.MusicTag.IntentTrans(), "Mode Switch Random", "" + b);
        });
        Sw_Repeat.setOnCheckedChangeListener((btn,b)->{
            if(b){
                Sw_Loop.setChecked(false);
                Sw_Random.setChecked(false);
            }
            lb.sendBroadcast(new PlayerModeSwitch(PlayerModeSwitch.Mode.Repeat, b));
            sp.write(SpManager.Keys.Mode, PlayerModeSwitch.Mode.Repeat);
            logger.printAndWrite(Level.INFO, new Tags.MusicTag.IntentTrans(), "Mode Switch Repeat", "" + b);
        });
        switch (sp.readString(SpManager.Keys.Mode,"")) {
            case PlayerModeSwitch.Mode.Loop -> Sw_Loop.setChecked(true);
            case PlayerModeSwitch.Mode.Random -> Sw_Random.setChecked(true);
            case PlayerModeSwitch.Mode.Repeat -> Sw_Repeat.setChecked(true);
        }

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
        fm.setUri(new SpManager(this).readString(SpManager.Keys.ChooseDir, (String) null));
        musicList = fm.listMusic(Types);
        logger.printAndWrite(Level.STEP, new Tags.MainTag.Default(), Arrays.toString(musicList.toArray()));
        LinearLayout Linear = findViewById(R.id.Linear_List);
        Linear.removeAllViews();
        int num = 0;
        for (MusicFile mf : musicList){
            TextView textView = new TextView(this);
            textView.setText(mf.Title);
            int temp = num;
            textView.setOnClickListener(v -> LocalBroadcastManager.getInstance(this).sendBroadcast(new MusicSwitch(temp)));
            logger.printAndWrite(Level.STEP, new Tags.MainTag.Default(), "File: " + mf.Title, "Title: " + mf.Title);
            Linear.addView(textView);
            num++;
        }
        MusicPlay.updateMusicList(musicList);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(progressReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(Update);
        super.onDestroy();
    }

    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (Objects.equals(intent.getAction(), UpdateProgress.Action)) {
                logger.printAndWrite(Level.STEP,new Tags.MainTag.UpdateProgress(),"Start Update");
                int playerNow = intent.getIntExtra(UpdateProgress.Name.NOW, 0);
                playerLength = intent.getIntExtra(UpdateProgress.Name.LENGTH, 0);
                SeekBar Sb = findViewById(R.id.Sb_Time);
                Sb.setMax(playerLength / 1000);
                StringBuilder sb = new StringBuilder();
                sb.append(MusicTool.getFormatTime(playerNow, playerNow <= 3600000 ? "mm:ss" : "H:mm:ss"))
                        .append("/")
                        .append(MusicTool.getFormatTime(playerLength, playerLength <= 3600000 ? "mm:ss" : "H:mm:ss"))
                        .append("");
                TextView tv = findViewById(R.id.Tv_Time);
                tv.setText(sb);
                tv = findViewById(R.id.Tv_NowPlayingName);
                tv.setText(intent.getStringExtra(UpdateProgress.Name.MUSIC_NAME));
                Sb.setProgress(playerNow / 1000);
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