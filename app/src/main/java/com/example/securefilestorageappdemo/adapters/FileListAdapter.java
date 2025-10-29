package com.example.securefilestorageappdemo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securefilestorageappdemo.FileDetailActivity;
import com.example.securefilestorageappdemo.MainActivity;
import com.example.securefilestorageappdemo.R;
import com.example.securefilestorageappdemo.models.SecureFile;
import com.example.securefilestorageappdemo.utils.EncryptionUtils;

import java.io.File;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {

    private final Context context;
    private final List<SecureFile> fileList;

    public FileListAdapter(Context context, List<SecureFile> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_secure_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        SecureFile file = fileList.get(position);
        File fileObj = new File(file.getFilePath());

        holder.fileName.setText(file.getFileName());

        // Handle directory vs file display
        if (fileObj.isDirectory() || file.getFileName().startsWith("📁 ")) {
            // Directory
            holder.iconPreview.setImageResource(R.drawable.add_directory); // Use directory icon if available, or folder icon

            holder.itemView.setOnClickListener(v -> {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).onDirectoryClick(file);
                }
            });
        } else if (file.isImage()) {
            // Image file - show thumbnail
            loadImageThumbnail(holder.iconPreview, file.getFilePath());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, FileDetailActivity.class);
                intent.putExtra("filePath", file.getFilePath());
                intent.putExtra("fileName", file.getFileName());
                intent.putExtra("isImage", true);
                context.startActivity(intent);
            });
        } else {
            // Regular file
            holder.iconPreview.setImageResource(R.drawable.file);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, FileDetailActivity.class);
                intent.putExtra("filePath", file.getFilePath());
                intent.putExtra("fileName", file.getFileName());
                intent.putExtra("isImage", false);
                context.startActivity(intent);
            });
        }

        // Long click for options menu (both files and directories)
        holder.itemView.setOnLongClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).showFileOptionsDialog(file);
            }
            return true;
        });
    }

    private void loadImageThumbnail(ImageView imageView, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                imageView.setImageResource(R.drawable.image);
                return;
            }

            // Read and decrypt the image file
            byte[] encryptedBytes = org.apache.commons.io.FileUtils.readFileToByteArray(file);
            byte[] decrypted = EncryptionUtils.decrypt(encryptedBytes);

            if (decrypted != null && decrypted.length > 0) {
                // Create thumbnail bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4; // Scale down by 4x for thumbnail
                options.inPreferredConfig = Bitmap.Config.RGB_565; // Use less memory

                Bitmap bitmap = BitmapFactory.decodeByteArray(decrypted, 0, decrypted.length, options);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.image); // fallback
                }
            } else {
                imageView.setImageResource(R.drawable.image); // fallback
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.image); // fallback on error
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView iconPreview;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            iconPreview = itemView.findViewById(R.id.iconPreview);
        }
    }
}