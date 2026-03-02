package sdjini.solution.intent;

import android.content.Intent;

public class MusicPlayInit extends Intent {
    public static final String Action = "sdjini.solution.MUSIC_PLAY_INIT";
    public MusicPlayInit() {
        setAction(Action);
    }
}
