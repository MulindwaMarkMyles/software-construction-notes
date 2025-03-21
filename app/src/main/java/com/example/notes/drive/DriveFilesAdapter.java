package com.example.notes.drive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notes.R;
import com.google.api.services.drive.model.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DriveFilesAdapter extends RecyclerView.Adapter<DriveFilesAdapter.ViewHolder> {

    private final Context context;
    private final List<File> fileList;
    private final SimpleDateFormat dateFormat;

    public DriveFilesAdapter(Context context, List<File> files) {
        this.context = context;
        this.fileList = files;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_drive_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = fileList.get(position);

        holder.fileName.setText(file.getName());

        // Set file type
        String mimeType = file.getMimeType();
        if (mimeType != null) {
            if (mimeType.equals("text/plain")) {
                holder.fileType.setText(R.string.text_document);
            } else if (mimeType.contains("folder")) {
                holder.fileType.setText(R.string.folder);
            } else {
                holder.fileType.setText(mimeType);
            }
        }

        // Format and set modified date
        try {
            if (file.getModifiedTime() != null) {
                Date modifiedDate = new Date(file.getModifiedTime().getValue());
                holder.modifiedDate.setText(dateFormat.format(modifiedDate));
            } else {
                holder.modifiedDate.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.modifiedDate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        TextView fileType;
        TextView modifiedDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            fileType = itemView.findViewById(R.id.file_type);
            modifiedDate = itemView.findViewById(R.id.modified_date);
        }
    }
}
