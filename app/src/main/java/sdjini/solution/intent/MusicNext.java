package sdjini.solution.intent;

import android.content.Intent;

public class MusicNext extends Intent {
    public static final String Action = "sdjini.solution.NEXT";
    public MusicNext() {
        super();
        setAction(Action);
    }
}
