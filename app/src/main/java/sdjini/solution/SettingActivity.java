package sdjini.solution;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import sdjini.solution.file_core.SpManager;
import sdjini.solution.intent.Reflash;
import sdjini.solution.log.Level;
import sdjini.solution.log.Logger;
import sdjini.solution.log.Tag;

public class SettingActivity extends AppCompatActivity {
    private SpManager spManager;
    private final ActivityResultLauncher<Intent> folderPickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                Uri treeUri = data.getData();
                if (treeUri != null) {
                    spManager.write(SpManager.Keys.ChooseDir, String.valueOf(treeUri));
                    getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
            finish();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Reflash());
        }
    );

    private static class Hw implements Tag{
        @NonNull
        @Override
        public String toString() {
            return "Hello World";
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spManager = new SpManager(this);
        setContentView(R.layout.activity_setting);

        findViewById(R.id.Btn_OpenSaf).setOnClickListener(view -> openFolderPicker());
        findViewById(R.id.Btn_HW).setOnClickListener(v -> {
            Toast.makeText(this, getText(R.string.Hw), Toast.LENGTH_SHORT).show();
            Logger logger = new Logger(this);
            logger.printAndWrite(Level.INFO, new Hw(), "Hello World", new Exception("Hello World"), "Hello World");
        });

        findViewById(R.id.Btn_OpenSetting).setOnClickListener(v->{
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        });
        findViewById(R.id.Btn_OpenNotification).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        });
        findViewById(R.id.Tv_media).setOnClickListener(v -> Toast.makeText(this, getString(R.string.goto_permission), Toast.LENGTH_SHORT).show());
        findViewById(R.id.Tv_notification).setOnClickListener(v -> Toast.makeText(this, getString(R.string.goto_foreground), Toast.LENGTH_SHORT).show());
        Switch Sw_Backup = findViewById(R.id.Sw_Backup);
        Sw_Backup.setChecked(spManager.readBoolean(SpManager.Keys.NeedBackup, false));
        Sw_Backup.setOnCheckedChangeListener((v,b)-> spManager.write(SpManager.Keys.NeedBackup, b));

    }


    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerLauncher.launch(intent);
    }

    /// @deprecated 应使用Uri.getPath()
    private String getFolderPathFromUri(Uri treeUri) {
        String volumePath = getVolumePath(treeUri);
        if (volumePath == null) return null;

        String documentPath = getDocumentPath(treeUri);
        if (documentPath == null) return null;

        return volumePath + documentPath;
    }

    private String getVolumePath(Uri treeUri) {
        String docId = DocumentsContract.getTreeDocumentId(treeUri);
        String[] split = docId.split(":");
        if (split.length >= 1 && "primary".equalsIgnoreCase(split[0])) return "/storage/emulated/0";
        return null;
    }

    private String getDocumentPath(Uri treeUri) {
        String docId = DocumentsContract.getTreeDocumentId(treeUri);
        String[] split = docId.split(":");
        if (split.length >= 2) return "/" + split[1];
        return null;
    }
}
