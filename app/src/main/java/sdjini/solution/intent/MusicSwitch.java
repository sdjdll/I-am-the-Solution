package sdjini.solution.intent;

import android.content.Intent;

public class MusicSwitch extends Intent {
    public static final String Action = "sdjini.solution.MusicSwitch";
    public static final class Name {
        public static final String ChoseNumber = "CHOSE_NUMBER";
    }

    public MusicSwitch(int num) {
        super();
        setAction(Action);
        putExtra(Name.ChoseNumber, num);
    }
}
