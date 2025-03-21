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
        Note note = notes.get(position);

        // Use note.getTitle() which now contains the author email
        holder.fromTextView.setText("From: " + note.getTitle()); 
        
        // Show note content (title + content)
        // We're using the note's original content field to store the original note's title
        String noteTitle = note.getContent().split("\n\n")[0]; // Get the first line as title
        String noteContent = note.getContent().substring(noteTitle.length()).trim(); // Remove title from content
        
        // Format content to show both title and content
        String formattedContent = "<b>" + noteTitle + "</b>\n\n" + noteContent;
        
        // If using HTML formatting:
        // holder.contentTextView.setText(Html.fromHtml(formattedContent, Html.FROM_HTML_MODE_COMPACT));
        // Or simpler approach:
        holder.contentTextView.setText(noteTitle + "\n\n" + noteContent);
        
        holder.categoryChip.setText(note.getCategory());

        // Format and set date
        String dateStr = dateFormat.format(new Date(note.getTimestamp()));
        holder.dateTextView.setText(dateStr);
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
