package sdjini.solution.file_core;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Size;

public class SpManager {
    public static final class Keys{
        public static final String ChooseDir = "ChooseDir";
        public static final String Mode = "Mode";
    }
    public static final String Setting = "Settings";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    public SpManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Setting, MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    public void write(String k,String s){
        editor.putString(k, s).apply();
    }
    public void write(String k,boolean b){
        editor.putBoolean(k, b).apply();
    }
    public void write(String k,int i){
        editor.putInt(k, i).apply();
    }

    public boolean readBoolean(String k, boolean... Default) {
        if (Default != null) return sharedPreferences.getBoolean(k, Default[0]);
        else return sharedPreferences.getBoolean(k, false);
    }
    public String readString(String k, String... Default){
        if (Default != null) return sharedPreferences.getString(k, Default[0]);
        else return sharedPreferences.getString(k, "");
    }

    public int readInt(String k, int... Default) {
        if (Default != null) return sharedPreferences.getInt(k, Default[0]);
        else return sharedPreferences.getInt(k, 0);
    }
}
