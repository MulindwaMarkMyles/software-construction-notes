package com.example.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.TrashViewHolder> {

    private Context context;
    private List<Note> trashList;
    private TrashActionListener listener;

    public interface TrashActionListener {
        void onRestore(Note note);
        void onDeletePermanently(Note note);
    }

    public TrashAdapter(Context context, List<Note> trashList, TrashActionListener listener) {
        this.context = context;
        this.trashList = trashList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trash, parent, false);
        return new TrashViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashViewHolder holder, int position) {
        Note note = trashList.get(position);

        holder.titleTextView.setText(note.getTitle());
        
        String contentPreview = note.getContent();
        if (contentPreview.length() > 80) {
            contentPreview = contentPreview.substring(0, 77) + "...";
        }
        holder.contentPreview.setText(contentPreview);
        
        holder.dateTextView.setText(formatDate(note.getTimestamp()));
        holder.categoryTextView.setText(note.getCategory());
        
        // Set up click listeners
        holder.restoreButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestore(note);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeletePermanently(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trashList.size();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class TrashViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentPreview, dateTextView, categoryTextView;
        ImageButton restoreButton, deleteButton;

        public TrashViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.trash_title);
            contentPreview = itemView.findViewById(R.id.trash_preview);
            dateTextView = itemView.findViewById(R.id.trash_date);
            categoryTextView = itemView.findViewById(R.id.trash_category);
            restoreButton = itemView.findViewById(R.id.btn_restore);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
