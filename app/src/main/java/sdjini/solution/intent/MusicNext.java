package sdjini.solution.intent;

import android.content.Intent;

public class MusicNext extends Intent {
    public static final String Action = "sdjini.solution.NEXT";
    public static final class Name{
        public static final String Number = "NUMBER";
    }
    public MusicNext(int num) {
        setAction(Action);
        putExtra(Name.Number, num);
    }
}
