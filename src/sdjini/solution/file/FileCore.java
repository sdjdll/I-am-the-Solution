package sdjini.solution.file;

import java.io.File;
import java.nio.file.FileSystemException;

public class FileCore {
    private File Dir = new File("Music");
    public FileCore(File... dir) throws FileSystemException {
        if (dir[0] != null) Dir = dir[0];
        if(Dir.exists() || Dir.mkdirs()) throw new FileSystemException("Cannot Create Dir");
    }
}
