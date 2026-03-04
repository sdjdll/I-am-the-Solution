package sdjini.solution.intent;

import android.content.Intent;

public class MusicPrevious extends Intent {
    public static final String Action = "sdjini.solution.PREVIOUS";

    public MusicPrevious(){
        super();
        setAction(Action);
    }
}