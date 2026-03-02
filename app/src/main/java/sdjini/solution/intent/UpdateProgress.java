package sdjini.solution.intent;

import android.content.Intent;
import android.content.IntentFilter;

public class UpdateProgress extends Intent {
    public static final String Action = "sdjini.solution.UPDATE_PROGRESS";
    public static final class Name{
        public static final String NOW = "NOW";
        public static final String LENGTH = "LENGTH";
        public static final String MUSIC_NAME = "MUSIC_NAME";
    }

    public UpdateProgress(int now, int length, String name) {
        super();
        setAction(Action);
        putExtra(Name.NOW,now).putExtra(Name.LENGTH,length).putExtra(Name.MUSIC_NAME, name);
    }
    public UpdateProgress(){
        super();
        setAction(Action);
    }
}
