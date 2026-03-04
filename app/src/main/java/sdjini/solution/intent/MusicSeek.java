package sdjini.solution.intent;

import android.content.Intent;

public class MusicSeek extends Intent {
    public static final String Action = "sdjini.solution.MUSIC_SEEK";
    public static final class Name{
        public static final String Progress = "PROGRESS";
    }
    public MusicSeek(int progress) {
        super();
        setAction(Action);
        putExtra(Name.Progress, progress);
    }
}
