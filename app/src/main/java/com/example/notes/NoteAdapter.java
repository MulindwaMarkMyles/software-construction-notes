package com.example.notes;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final Context context;
    private List<Note> notesList;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;
    private boolean isDarkTheme = false;
    private DatabaseHelper dbHelper;
    private Set<Integer> notesWithTags; // Track notes that have tags

    // Interface for click listener
    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    // Interface for long click listener
    public interface OnItemLongClickListener {
        boolean onItemLongClick(Note note, View view);
    }

    public NoteAdapter(Context context, List<Note> notesList) {
        this.context = context;
        this.notesList = notesList;
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.notesWithTags = new HashSet<>();
        updateNotesWithTags();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateList(List<Note> newList) {
        this.notesList = newList;
        updateNotesWithTags();
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
        try {
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

            // Show tag icon if this note has tags - ensure this is called
            holder.tagIcon.setVisibility(notesWithTags.contains(note.getId()) ? View.VISIBLE : View.GONE);

            // Apply theme-specific styling
            applyThemeToViewHolder(holder);

            // Make sure click listener works by removing any existing ones first
            holder.itemView.setOnClickListener(null);
            holder.itemView.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && clickListener != null) {
                    Note clickedNote = notesList.get(adapterPosition);
                    clickListener.onItemClick(clickedNote);
                }
            });

            // Set up long click listener
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    return longClickListener.onItemLongClick(note, v);
                }
                return false;
            });
        } catch (Exception e) {
            Log.e("NoteAdapter", "Error binding view", e);
        }
    }

    private void applyThemeToViewHolder(NoteViewHolder holder) {
        // Get system colors based on current theme
        TypedArray ta = context.obtainStyledAttributes(new int[] {
                android.R.attr.textColorPrimary,
                android.R.attr.textColorSecondary,
                android.R.attr.colorBackground
        });

        int textColorPrimary = ta.getColor(0, 0);
        int textColorSecondary = ta.getColor(1, 0);
        int backgroundColor = ta.getColor(2, 0);
        ta.recycle();

        // Apply system colors based on theme
        holder.titleTextView.setTextColor(textColorPrimary);
        holder.contentPreview.setTextColor(textColorSecondary);
        holder.dateTextView.setTextColor(textColorSecondary);
        holder.itemView.setBackgroundColor(backgroundColor);

        // Category chip text color - use primary color from resources if available
        try {
            int primaryColor = ContextCompat.getColor(context, R.color.colorPrimary);
            holder.categoryChip.setTextColor(primaryColor);
        } catch (Exception e) {
            // If colorPrimary is not defined, use textColorPrimary as fallback
            holder.categoryChip.setTextColor(textColorPrimary);
        }
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public void updateTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        notifyDataSetChanged();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public Note getNoteAtPosition(int position) {
        return notesList.get(position);
    }

    public List<Note> getNotes() {
        return notesList;
    }

    public void updateNotes(List<Note> newNotes) {
        this.notesList = newNotes;
        updateNotesWithTags(); // Make sure to call this to refresh tag icons
        notifyDataSetChanged();
    }

    private void updateNotesWithTags() {
        // Clear existing set
        notesWithTags.clear();

        try {
            // Get all notes with tags from the database
            for (Note note : notesList) {
                if (dbHelper.noteHasTags(note.getId())) {
                    notesWithTags.add(note.getId());
                }
            }
        } catch (Exception e) {
            Log.e("NoteAdapter", "Error updating tags", e);
        }
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentPreview, dateTextView;
        Chip categoryChip;
        ImageView priorityIndicator;
        ImageView tagIcon; // New tag icon

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.note_title);
            contentPreview = itemView.findViewById(R.id.note_content_preview);
            dateTextView = itemView.findViewById(R.id.note_date);
            categoryChip = itemView.findViewById(R.id.note_category);
            priorityIndicator = itemView.findViewById(R.id.note_priority);
            tagIcon = itemView.findViewById(R.id.tag_icon); // Initialize the tag icon

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (clickListener != null && position != RecyclerView.NO_POSITION) {
                        clickListener.onItemClick(notesList.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (longClickListener != null && position != RecyclerView.NO_POSITION) {
                        return longClickListener.onItemLongClick(notesList.get(position), v);
                    }
                    return false;
                }
            });
        }
    }
}
