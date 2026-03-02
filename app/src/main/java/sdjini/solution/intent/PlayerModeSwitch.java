package sdjini.solution.intent;

import android.content.Intent;

public class PlayerModeSwitch extends Intent {
    public static final String Action = "sdjini.solution.PLAYER_MODE_SWITCH";
    public static final class Mode{
        public static final String Loop = "Loop";
        public static final String Random = "Random";
        public static final String Repeat = "Repeat";
    }
    public static final class Name{
        public static final String Mode = "Mode";
        public static final String State = "State";
    }
    public PlayerModeSwitch(String mode, boolean state) {
        setAction(Action);
        putExtra(Name.Mode, mode).putExtra(Name.State, state);
    }
}
