package sdjini.solution.intent;

import android.content.Intent;

public class MusicVolume extends Intent {
    public static final String Action = "sdjini.solution.MusicVolume";
    public static final class Name{
        public static final String Left = "LEFT";
        public static final String Right = "Right";
    }
    public MusicVolume(float left, float right) {
        setAction(Action);
        putExtra(Name.Left, left).putExtra(Name.Right, right);
    }
}
