package sdjini.solution.intent;

import android.content.Intent;

public class MusicPrevious extends Intent {
    public static final String Action = "sdjini.solution.PREVIOUS";
    public static final class Name{
        public static final String Number = "NUMBER";
    }
    public MusicPrevious(int num){
        setAction(Action);
        putExtra(Name.Number, num);
    }
}