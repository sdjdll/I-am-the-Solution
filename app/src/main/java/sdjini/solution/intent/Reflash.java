package sdjini.solution.intent;

import android.content.Intent;

public class Reflash extends Intent {
    public static final String Action = "Reflash";
    public Reflash() {
        setAction(Action);
    }
}
