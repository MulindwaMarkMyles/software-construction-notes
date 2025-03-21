package com.example.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaggedNoteAdapter extends RecyclerView.Adapter<TaggedNoteAdapter.TaggedNoteViewHolder> {

    private List<Note> notes;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());

    public TaggedNoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public TaggedNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tagged_note, parent, false);
        return new TaggedNoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaggedNoteViewHolder holder, int position) {
        try {
            Note note = notes.get(position);

            // Use note.getTitle() which now contains the author email
            holder.fromTextView.setText("From: " + note.getTitle());

            // Safely parse the content
            String content = note.getContent();
            if (content != null && !content.isEmpty()) {
                // Try to extract title and content safely
                String[] parts = content.split("\n\n", 2);
                String noteTitle = parts.length > 0 ? parts[0].trim() : "";
                String noteContent = parts.length > 1 ? parts[1].trim() : content;
                
                // Set content with title and content properly formatted
                holder.contentTextView.setText(noteTitle.isEmpty() ? 
                        noteContent : 
                        noteTitle + "\n\n" + noteContent);
            } else {
                holder.contentTextView.setText("");
            }

            // Set category with null check
            holder.categoryChip.setText(note.getCategory() != null ? 
                    note.getCategory() : "Unknown");

            // Format and set date
            String dateStr = dateFormat.format(new Date(note.getTimestamp()));
            holder.dateTextView.setText(dateStr);
        } catch (Exception e) {
            Log.e("TaggedNoteAdapter", "Error binding view holder", e);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    static class TaggedNoteViewHolder extends RecyclerView.ViewHolder {
        TextView fromTextView;
        TextView contentTextView;
        Chip categoryChip;
        TextView dateTextView;

        public TaggedNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            fromTextView = itemView.findViewById(R.id.note_from);
            contentTextView = itemView.findViewById(R.id.note_content);
            categoryChip = itemView.findViewById(R.id.category_chip);
            dateTextView = itemView.findViewById(R.id.note_date);
        }
    }
}
