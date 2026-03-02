package sdjini.solution.intent;

import android.content.Intent;

public class MusicControl extends Intent {
    public static final String Action = "sdjini.solution.CONTROL";
    public MusicControl(){
        setAction(Action);
    }
}