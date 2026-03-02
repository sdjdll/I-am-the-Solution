package sdjini.solution;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import sdjini.solution.file_core.SpManager;
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
                    String folderPath = getFolderPathFromUri(treeUri);
                    if (folderPath != null) {
                        spManager.write(SpManager.Keys.ChooseDir, folderPath);
                        getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                }
            }
        }
    );

    private class Hw implements Tag{
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
    }


    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerLauncher.launch(intent);
    }

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
