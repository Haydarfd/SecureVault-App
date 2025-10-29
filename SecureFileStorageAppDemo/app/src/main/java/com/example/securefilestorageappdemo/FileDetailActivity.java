package com.example.securefilestorageappdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.securefilestorageappdemo.utils.EncryptionUtils;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class FileDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_detail);

        TextView fileNameText = findViewById(R.id.fileNameDetail);
        ImageView fileImage = findViewById(R.id.fileImagePreview);
        TextView fileContent = findViewById(R.id.fileContentDetail);

        String fileName = getIntent().getStringExtra("fileName");
        String filePath = getIntent().getStringExtra("filePath");
        boolean isImage = getIntent().getBooleanExtra("isImage", false);

        // Clean the filename if it starts with folder icon
        if (fileName != null && fileName.startsWith("📁 ")) {
            fileName = fileName.substring(2);
        }

        fileNameText.setText(fileName != null ? fileName : "Unknown File");

        // Check if the file exists
        if (filePath == null) {
            Toast.makeText(this, "File path is invalid.", Toast.LENGTH_LONG).show();
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "File does not exist.", Toast.LENGTH_LONG).show();
            return;
        }

        if (file.isDirectory()) {
            // Handle directory case
            fileContent.setVisibility(android.view.View.VISIBLE);
            fileImage.setVisibility(android.view.View.GONE);
            fileContent.setText("This is a directory.\n\nContents:\n" + getDirectoryContents(file));
            return;
        }

        if (isImage) {
            // Handle image files
            fileImage.setVisibility(android.view.View.VISIBLE);
            fileContent.setVisibility(android.view.View.GONE);

            try {
                byte[] encryptedBytes = org.apache.commons.io.FileUtils.readFileToByteArray(file);
                byte[] decrypted = EncryptionUtils.decrypt(encryptedBytes);

                if (decrypted != null && decrypted.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decrypted, 0, decrypted.length);
                    if (bitmap != null) {
                        fileImage.setImageBitmap(bitmap);
                        Toast.makeText(this, "Image loaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        fileImage.setVisibility(android.view.View.GONE);
                        fileContent.setVisibility(android.view.View.VISIBLE);
                        fileContent.setText("❌ Decrypted data is not a valid image format.\n\nFile size: "
                                + decrypted.length + " bytes\n\nThis might be a corrupted image file or the wrong file type.");
                    }
                } else {
                    fileImage.setVisibility(android.view.View.GONE);
                    fileContent.setVisibility(android.view.View.VISIBLE);
                    fileContent.setText("❌ Failed to decrypt image file.\n\nThe file might be corrupted or encrypted with a different key.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                fileImage.setVisibility(android.view.View.GONE);
                fileContent.setVisibility(android.view.View.VISIBLE);
                fileContent.setText("❌ Error loading image: " + e.getMessage() +
                        "\n\nThis could be due to:\n• File corruption\n• Encryption/decryption error\n• Insufficient memory");
                Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            // Handle non-image files
            fileImage.setVisibility(android.view.View.GONE);
            fileContent.setVisibility(android.view.View.VISIBLE);

            try {
                byte[] encryptedBytes = org.apache.commons.io.FileUtils.readFileToByteArray(file);
                byte[] decrypted = EncryptionUtils.decrypt(encryptedBytes);

                if (decrypted != null && decrypted.length > 0) {
                    // Try to display as text
                    String content = new String(decrypted);

                    // Check if it's likely text content
                    if (isPrintableText(content)) {
                        fileContent.setText("📄 File Content:\n\n" + content);
                    } else {
                        fileContent.setText("📄 Binary File\n\n" +
                                "File size: " + decrypted.length + " bytes\n" +
                                "Type: Binary/Non-text file\n\n" +
                                "This file contains binary data that cannot be displayed as text.");
                    }
                } else {
                    fileContent.setText("❌ Failed to decrypt file.\n\nThe file might be corrupted or encrypted with a different key.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                fileContent.setText("❌ Error loading file: " + e.getMessage() +
                        "\n\nThis could be due to:\n• File corruption\n• Encryption/decryption error\n• Access permissions");
                Toast.makeText(this, "Failed to load file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getDirectoryContents(File directory) {
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return "Empty directory";
        }

        StringBuilder contents = new StringBuilder();
        int fileCount = 0;
        int dirCount = 0;

        for (File file : files) {
            if (file.isDirectory()) {
                contents.append("📁 ").append(file.getName()).append("\n");
                dirCount++;
            } else {
                contents.append("📄 ").append(file.getName()).append("\n");
                fileCount++;
            }
        }

        return "Directories: " + dirCount + "\nFiles: " + fileCount + "\n\n" + contents.toString();
    }

    private boolean isPrintableText(String text) {
        if (text == null || text.length() == 0) return false;

        // Check if most characters are printable
        int printableCount = 0;
        int totalCount = Math.min(text.length(), 1000); // Check first 1000 chars

        for (int i = 0; i < totalCount; i++) {
            char c = text.charAt(i);
            if (Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t') {
                // Non-printable control character
                continue;
            }
            printableCount++;
        }

        // If more than 80% are printable, consider it text
        return (printableCount * 100.0 / totalCount) > 80;
    }
}