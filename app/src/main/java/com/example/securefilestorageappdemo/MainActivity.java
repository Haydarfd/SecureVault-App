package com.example.securefilestorageappdemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securefilestorageappdemo.adapters.FileListAdapter;
import com.example.securefilestorageappdemo.models.SecureFile;
import com.example.securefilestorageappdemo.utils.EncryptionUtils;
import com.example.securefilestorageappdemo.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private LinearLayout fabMenu;
    private FloatingActionButton mainFab;
    private View fabOverlay;
    private boolean isFabOpen = false;

    // Individual FAB references
    private FloatingActionButton fabAddFile;
    private FloatingActionButton fabAddDirectory;
    private FloatingActionButton fabEdit;
    private FloatingActionButton fabLogout;

    private File currentDirectory;
    private File userRootDirectory;

    private RecyclerView fileRecyclerView;
    private FileListAdapter fileListAdapter;
    private final List<SecureFile> secureFileList = new ArrayList<>();
    private TextView currentDirectoryLabel;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) handleFileEncryption(fileUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String username = SessionManager.getUser(this);
        if (username == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        authenticateBiometric();

        TextView usernameLabel = findViewById(R.id.usernameLabel);
        usernameLabel.setText(getString(R.string.username) + ": " + username);

        // Initialize FAB components
        mainFab = findViewById(R.id.mainFab);
        fabMenu = findViewById(R.id.fabMenu);
        fabOverlay = findViewById(R.id.fabOverlay);
        fabAddFile = findViewById(R.id.fabAddFile);
        fabAddDirectory = findViewById(R.id.fabAddDirectory);
        fabEdit = findViewById(R.id.fabEdit);
        fabLogout = findViewById(R.id.fabLogout);

        fileRecyclerView = findViewById(R.id.fileRecyclerView);
        currentDirectoryLabel = findViewById(R.id.currentDirectoryLabel);

        userRootDirectory = new File(getFilesDir(), username);
        if (!userRootDirectory.exists()) userRootDirectory.mkdir();

        currentDirectory = userRootDirectory;
        currentDirectoryLabel.setText("My Secure Files");

        fileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileListAdapter = new FileListAdapter(this, secureFileList);
        fileRecyclerView.setAdapter(fileListAdapter);

        loadFilesFromDirectory(currentDirectory);

        // Initialize FAB menu state
        initializeFabMenu();

        // FAB click listeners
        mainFab.setOnClickListener(v -> toggleFabMenu());

        // Overlay click listener to close menu
        if (fabOverlay != null) {
            fabOverlay.setOnClickListener(v -> closeFabMenu());
        }

        fabAddFile.setOnClickListener(v -> {
            closeFabMenu();
            launchFilePicker();
        });
        fabAddDirectory.setOnClickListener(v -> {
            closeFabMenu();
            showDirectoryCreationDialog();
        });
        fabEdit.setOnClickListener(v -> {
            closeFabMenu();
            showDirectoryPickerDialog();
        });
        fabLogout.setOnClickListener(v -> {
            closeFabMenu();
            SessionManager.clearUser(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void initializeFabMenu() {
        // Hide all individual FAB buttons initially
        fabAddFile.setVisibility(View.GONE);
        fabAddDirectory.setVisibility(View.GONE);
        fabEdit.setVisibility(View.GONE);
        fabLogout.setVisibility(View.GONE);

        if (fabOverlay != null) {
            fabOverlay.setVisibility(View.GONE);
        }
    }

    private void authenticateBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        Toast.makeText(MainActivity.this, "Authenticated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(MainActivity.this, "Biometric auth failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        Toast.makeText(MainActivity.this, "Auth error: " + errString, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Authenticate to access secure storage")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void toggleFabMenu() {
        if (isFabOpen) {
            closeFabMenu();
        } else {
            openFabMenu();
        }
    }

    private void openFabMenu() {
        // Show overlay
        if (fabOverlay != null) {
            fabOverlay.setVisibility(View.VISIBLE);
        }
        isFabOpen = true;

        // Rotate main FAB
        mainFab.animate().rotation(45f).setDuration(200);

        // Show and animate individual FABs
        showFabWithAnimation(fabAddFile, 50);
        showFabWithAnimation(fabAddDirectory, 100);
        showFabWithAnimation(fabEdit, 150);
        showFabWithAnimation(fabLogout, 200);
    }

    private void closeFabMenu() {
        // Hide overlay
        if (fabOverlay != null) {
            fabOverlay.setVisibility(View.GONE);
        }
        isFabOpen = false;

        // Rotate main FAB back
        mainFab.animate().rotation(0f).setDuration(200);

        // Hide individual FABs with animation
        hideFabWithAnimation(fabLogout, 0);
        hideFabWithAnimation(fabEdit, 50);
        hideFabWithAnimation(fabAddDirectory, 100);
        hideFabWithAnimation(fabAddFile, 150);
    }

    private void showFabWithAnimation(FloatingActionButton fab, long delay) {
        fab.setVisibility(View.VISIBLE);
        fab.setAlpha(0f);
        fab.setScaleX(0f);
        fab.setScaleY(0f);

        fab.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .setStartDelay(delay)
                .start();
    }

    private void hideFabWithAnimation(FloatingActionButton fab, long delay) {
        fab.animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(100)
                .setStartDelay(delay)
                .withEndAction(() -> fab.setVisibility(View.GONE))
                .start();
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }

    private void handleFileEncryption(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Toast.makeText(this, "Failed to open file", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] fileBytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            byte[] encryptedBytes = EncryptionUtils.encrypt(fileBytes);

            String fileName = getFileNameFromUri(fileUri);
            if (fileName == null) fileName = "encrypted_" + System.currentTimeMillis();

            File encryptedFile = new File(currentDirectory, fileName);
            try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
                fos.write(encryptedBytes);
            }

            boolean isImage = getContentResolver().getType(fileUri) != null &&
                    getContentResolver().getType(fileUri).toLowerCase().startsWith("image/");

            Toast.makeText(this, "File encrypted and saved!", Toast.LENGTH_SHORT).show();

            // Refresh the file list
            loadFilesFromDirectory(currentDirectory);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Encryption failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        String result = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            } finally {
                cursor.close();
            }
        }
        return result != null ? result : uri.getLastPathSegment();
    }

    private void showDirectoryCreationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Directory");

        final EditText input = new EditText(this);
        input.setHint("e.g. Work, Personal");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String dirName = input.getText().toString().trim();
            if (!dirName.isEmpty()) {
                File dir = new File(currentDirectory, dirName);
                if (!dir.exists()) {
                    if (dir.mkdir()) {
                        Toast.makeText(this, "Directory created!", Toast.LENGTH_SHORT).show();
                        loadFilesFromDirectory(currentDirectory); // Refresh current directory
                    } else {
                        Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Directory already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDirectoryPickerDialog() {
        // Get all directories (including subdirectories)
        List<File> allDirectories = new ArrayList<>();
        collectDirectories(userRootDirectory, allDirectories);

        if (allDirectories.isEmpty()) {
            Toast.makeText(this, "No directories found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create display names with paths
        String[] displayNames = new String[allDirectories.size()];
        for (int i = 0; i < allDirectories.size(); i++) {
            File dir = allDirectories.get(i);
            if (dir.equals(userRootDirectory)) {
                displayNames[i] = "📁 My Secure Files (Root)";
            } else {
                String relativePath = getRelativePath(userRootDirectory, dir);
                displayNames[i] = "📁 " + relativePath;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Directory");
        builder.setItems(displayNames, (dialog, which) -> {
            currentDirectory = allDirectories.get(which);
            if (currentDirectory.equals(userRootDirectory)) {
                currentDirectoryLabel.setText("My Secure Files");
            } else {
                currentDirectoryLabel.setText(getRelativePath(userRootDirectory, currentDirectory));
            }
            loadFilesFromDirectory(currentDirectory);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void collectDirectories(File root, List<File> directories) {
        directories.add(root);
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectDirectories(file, directories);
                }
            }
        }
    }

    private String getRelativePath(File root, File target) {
        String rootPath = root.getAbsolutePath();
        String targetPath = target.getAbsolutePath();
        if (targetPath.startsWith(rootPath)) {
            String relative = targetPath.substring(rootPath.length());
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }
            return relative.isEmpty() ? "Root" : relative;
        }
        return target.getName();
    }

    private void loadFilesFromDirectory(File directory) {
        secureFileList.clear();
        File[] files = directory.listFiles();
        if (files != null) {
            // Sort files: directories first, then files
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });

            for (File file : files) {
                if (file.isDirectory()) {
                    // Add directory to list
                    secureFileList.add(new SecureFile("📁 " + file.getName(), file.getAbsolutePath(), false));
                } else {
                    // Add file to list
                    String fileName = file.getName();
                    String lower = fileName.toLowerCase();
                    boolean isImage = lower.endsWith(".png") || lower.endsWith(".jpg") ||
                            lower.endsWith(".jpeg") || lower.endsWith(".gif") ||
                            lower.endsWith(".bmp") || lower.endsWith(".webp");
                    secureFileList.add(new SecureFile(fileName, file.getAbsolutePath(), isImage));
                }
            }
        }
        fileListAdapter.notifyDataSetChanged();
    }

    public void onDirectoryClick(SecureFile directory) {
        File dirFile = new File(directory.getFilePath());
        if (dirFile.exists() && dirFile.isDirectory()) {
            currentDirectory = dirFile;
            if (currentDirectory.equals(userRootDirectory)) {
                currentDirectoryLabel.setText("My Secure Files");
            } else {
                currentDirectoryLabel.setText(getRelativePath(userRootDirectory, currentDirectory));
            }
            loadFilesFromDirectory(currentDirectory);
        }
    }

    public void showFileOptionsDialog(SecureFile file) {
        File fileObj = new File(file.getFilePath());

        if (fileObj.isDirectory()) {
            String[] options = {"Open", "Rename", "Delete"};
            new AlertDialog.Builder(this)
                    .setTitle("Directory Options")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0: // Open
                                onDirectoryClick(file);
                                break;
                            case 1: // Rename
                                showRenameDialog(file);
                                break;
                            case 2: // Delete
                                showDeleteDialog(file);
                                break;
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            String[] options = {"Rename", "Delete"};
            new AlertDialog.Builder(this)
                    .setTitle("File Options")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) showRenameDialog(file);
                        else showDeleteDialog(file);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void showRenameDialog(SecureFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename " + (new File(file.getFilePath()).isDirectory() ? "Directory" : "File"));

        final EditText input = new EditText(this);
        String currentName = file.getFileName();
        if (currentName.startsWith("📁 ")) {
            currentName = currentName.substring(2);
        }
        input.setText(currentName);
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                File oldFile = new File(file.getFilePath());
                File newFile = new File(currentDirectory, newName);
                if (oldFile.renameTo(newFile)) {
                    Toast.makeText(this, (oldFile.isDirectory() ? "Directory" : "File") + " renamed.", Toast.LENGTH_SHORT).show();
                    loadFilesFromDirectory(currentDirectory); // Refresh
                } else {
                    Toast.makeText(this, "Rename failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteDialog(SecureFile file) {
        File fileObj = new File(file.getFilePath());
        boolean isDirectory = fileObj.isDirectory();

        new AlertDialog.Builder(this)
                .setTitle("Delete " + (isDirectory ? "Directory" : "File"))
                .setMessage("Are you sure you want to delete this " + (isDirectory ? "directory and all its contents" : "file") + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (deleteRecursively(fileObj)) {
                        Toast.makeText(this, (isDirectory ? "Directory" : "File") + " deleted.", Toast.LENGTH_SHORT).show();
                        loadFilesFromDirectory(currentDirectory); // Refresh
                    } else {
                        Toast.makeText(this, "Deletion failed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursively(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    @Override
    public void onBackPressed() {
        if (isFabOpen) {
            closeFabMenu();
            return;
        }

        // Handle directory navigation
        if (!currentDirectory.equals(userRootDirectory)) {
            File parentDir = currentDirectory.getParentFile();
            if (parentDir != null && parentDir.getAbsolutePath().startsWith(userRootDirectory.getAbsolutePath())) {
                currentDirectory = parentDir;
                if (currentDirectory.equals(userRootDirectory)) {
                    currentDirectoryLabel.setText("My Secure Files");
                } else {
                    currentDirectoryLabel.setText(getRelativePath(userRootDirectory, currentDirectory));
                }
                loadFilesFromDirectory(currentDirectory);
                return;
            }
        }

        super.onBackPressed();
    }
}