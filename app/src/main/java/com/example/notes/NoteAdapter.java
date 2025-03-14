package com.example.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> notesList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public NoteAdapter(Context context, List<Note> notesList) {
        this.context = context;
        this.notesList = notesList;
    }

    public void updateList(List<Note> newList) {
        this.notesList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notesList.get(position);
        SettingsManager settingsManager = SettingsManager.getInstance(context);

        // Set text with null checks to prevent crashes
        holder.titleTextView.setText(note.getTitle() != null ? note.getTitle() : "");

        // Limit content preview length to prevent UI issues
        String contentPreview = note.getContent() != null ? note.getContent() : "";
        if (contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 97) + "...";
        }
        holder.contentPreview.setText(contentPreview);

        // Show/hide date based on settings
        if (settingsManager.shouldShowDate()) {
            holder.dateTextView.setVisibility(View.VISIBLE);
            holder.dateTextView.setText(formatDate(note.getTimestamp()));
        } else {
            holder.dateTextView.setVisibility(View.GONE);
        }

        // Set category
        holder.categoryChip.setText(note.getCategory());

        // Set category color
        int categoryColor;
        switch (note.getCategory()) {
            case "Personal":
                categoryColor = R.color.category_personal;
                break;
            case "Work":
                categoryColor = R.color.category_work;
                break;
            case "Study":
                categoryColor = R.color.category_study;
                break;
            default:
                categoryColor = R.color.category_misc;
                break;
        }
        holder.categoryChip.setChipBackgroundColorResource(categoryColor);

        // Set priority indicator
        switch (note.getPriority()) {
            case 2: // High
                holder.priorityIndicator.setImageResource(R.drawable.ic_priority_high);
                holder.priorityIndicator.setColorFilter(context.getResources().getColor(R.color.priority_high));
                break;
            case 1: // Medium
                holder.priorityIndicator.setImageResource(R.drawable.ic_priority_high);
                holder.priorityIndicator.setColorFilter(context.getResources().getColor(R.color.priority_medium));
                break;
            case 0: // Low
                holder.priorityIndicator.setImageResource(R.drawable.ic_priority_high);
                holder.priorityIndicator.setColorFilter(context.getResources().getColor(R.color.priority_low));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentPreview, dateTextView;
        Chip categoryChip;
        ImageView priorityIndicator;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.note_title);
            contentPreview = itemView.findViewById(R.id.note_content_preview);
            dateTextView = itemView.findViewById(R.id.note_date);
            categoryChip = itemView.findViewById(R.id.note_category);
            priorityIndicator = itemView.findViewById(R.id.note_priority);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(notesList.get(position));
                    }
                }
            });
        }
    }
}
