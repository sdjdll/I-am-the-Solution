package sdjini.solution.log;

import androidx.annotation.NonNull;

public final class Tags {
    public static final class MainTag{

        public static final class UpdateProgress implements Tag{
            @NonNull
            @Override
            public String toString() {
                return "UpdateProgress";
            }
        }

        public static final class Default implements Tag{
            @NonNull
            @Override
            public String toString() {
                return "Default";
            }
        }


    }
    public static final class MusicTag{
        public static final class MusicManage implements Tag{
            @NonNull
            @Override
            public String toString() {
                return "MusicTag.MusicManage";
            }
        }
        public static final class IntentTrans implements Tag{
            @NonNull
            @Override
            public String toString() {
                return "MusicTag.IntentTrans";
            }
        }

        public static final class ModeSwitch implements Tag{
            @NonNull
            @Override
            public String toString() {
                return "MusicTag.ModeSwitch";
            }
        }
    }
    public static final class FileTag{
        public static final class FileManage implements Tag{
            @NonNull
            @Override
            public String toString() {
                return "FileManage";
            }
        }
        public static final class IntentTrans implements Tag{
            @NonNull
            @Override
            public String toString() {
                return "FileTag.IntentTrans";
            }
        }
    }
}
