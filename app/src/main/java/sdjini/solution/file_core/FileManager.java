package sdjini.solution.file_core;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import sdjini.solution.log.Level;
import sdjini.solution.log.Logger;
import sdjini.solution.log.Tags;

public class FileManager {
    private final SpManager spManager;
    private final File dir;
    private final Logger logger;
    public FileManager(Context context) {
        logger = new Logger(context);
        logger.printAndWrite(Level.INFO, new Tags.FileTag.FileManage(), "Create", "From: " + context.getClass().getName());
        spManager = new SpManager(context);
        logger.printAndWrite(Level.INFO, new Tags.FileTag.FileManage(), "SpManagerCreate", "From: " + this.getClass().getName(), "By: " + context.getClass().getName());
        String uriString = spManager.readString(SpManager.Keys.ChooseDir, Environment.DIRECTORY_MUSIC);
        logger.printAndWrite(Level.INFO, new Tags.FileTag.FileManage(), "Source: " + uriString);
        if (uriString != null) dir = new File(Objects.requireNonNull(Uri.parse(uriString).getPath()));
        else dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (dir != null) logger.printAndWrite(Level.STEP, new Tags.FileTag.FileManage(), "GetFrom: " + dir.getAbsolutePath());
        else logger.printAndWrite(Level.FATAL, new Tags.FileTag.FileManage(), "dir is null!", new NullPointerException());
    }

    public List<File> listDir(){
        File[] f = dir.listFiles();
        List<File> list = Arrays.asList(f == null ? new File[0] : f);
        logger.printAndWrite(Level.INFO, new Tags.FileTag.FileManage(), "ListDir", "Dir: " + Arrays.toString(list.toArray()));
        return list;
    }

    @NonNull
    public static List<MusicFile> filter(@NonNull File[] files, String[] types){
        Set<String> typeSet = new HashSet<>();
        for (String t : types) {
            typeSet.add(t.toLowerCase());
        }
        List<MusicFile> temp = new ArrayList<>(files.length);

        for (File f : files){
            if (!f.isFile()) continue;
            String path = f.getPath().toLowerCase();
            int lastDot = path.lastIndexOf('.');
            if (lastDot != -1) {
                String suffix = path.substring(lastDot);
                if (typeSet.contains(suffix)) temp.add(new MusicFile(f.getPath()));
            }
        }
        return temp;
    }
}
