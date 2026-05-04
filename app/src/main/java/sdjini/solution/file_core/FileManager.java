package sdjini.solution.file_core;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

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
    private final Logger logger;
    private final Context context;
    private Uri dirUri;
    public FileManager(Context context) {
        logger = new Logger(context);
        this.context = context;
        logger.printAndWrite(Level.INFO, new Tags.FileTag.FileManage(), "Create", "From: " + context.getClass().getName());
        spManager = new SpManager(context);
        logger.printAndWrite(Level.INFO, new Tags.FileTag.FileManage(), "SpManagerCreate", "From: " + this.getClass().getName(), "By: " + context.getClass().getName());
        String uriStr = spManager.readString(SpManager.Keys.ChooseDir, "Not supposed.");
        setUri(uriStr);
    }

    /// TODO：准备转用DocumentFile
    /// 不是不报，时候未到（我懒）
    public void setUri(String uri){
        if (uri != null) dirUri = Uri.parse(uri);
        else dirUri = null;
        logger.printAndWrite(Level.INFO, new Tags.FileTag.FileManage(), "Now Uri", dirUri.toString());
    }
    /// @deprecated Fuck Huawei
    public File[] listDir(File dir){
//        return dir.list();
        return new File[0];
    }
    /// @deprecated I Like this
    public List<MusicFile> listMusic(File[] MusicArray, String[] Types){
//        return MusicArray.contain(Types);
        return new ArrayList<>(0);
    }
    /// Fuck Huawei
    public List<MusicFile> listMusic(String[] supposeType) {
        logger.printAndWrite(Level.INFO, new Tags.FileTag.FileManage(), "Now Uri", dirUri.toString());
        List<MusicFile> temp = new ArrayList<>();
        if (dirUri == null || dirUri == Uri.EMPTY) return temp;
        try{
            DocumentFile dFile = DocumentFile.fromTreeUri(context, dirUri);
            if (dFile == null) return temp;

            Set<String> supTyp = new HashSet<>();
            for (String s : supposeType) {
                String low = s.toLowerCase();
                if (!low.startsWith(".")) supTyp.add("." + low);
                else supTyp.add(low);
            }

            DocumentFile[] files = dFile.listFiles();

            for (DocumentFile f : files) {
                if (!f.isFile()) continue;

                String name = f.getName();
                if (name == null) continue;
                String lowerName = name.toLowerCase();

                for (String suffix : supTyp) {
                    if (lowerName.endsWith(suffix)) {
                        temp.add(new MusicFile(context, f.getUri()));
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException e){
            logger.printAndWrite(Level.ERROR, new Tags.FileTag.FileManage(), "Uri is IllegalArgumentException", e);
        }
        return temp;
    }


}
