package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.notes.model.SharedNote;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaggedNotesFragment extends Fragment {
    private static final String TAG = "TaggedNotesFragment";
    private RecyclerView recyclerView;
    private View emptyState;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NoteAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tagged_notes, container, false);
        recyclerView = view.findViewById(R.id.tagged_notes_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NoteAdapter(getContext(), new ArrayList<>()) {
            @Override
            public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                Note note = getNoteAtPosition(position);

                // Style the note item
                holder.titleTextView.setText("From: " + note.getTitle());
                holder.contentPreview.setMaxLines(4); // Show more content but not everything
                holder.contentPreview.setTextSize(14); // Slightly smaller text

                // Show date and owner info
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
                String dateStr = sdf.format(new Date(note.getTimestamp()));
                holder.dateTextView.setText(dateStr);
                holder.dateTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));

                // Make category chip more prominent
                holder.categoryChip.setTextSize(12);
                holder.categoryChip.setChipStrokeWidth(1);

                // Remove clickable background but keep visual separation
                holder.itemView.setBackgroundResource(R.drawable.tagged_note_background);
                holder.itemView.setClickable(false);
                holder.itemView.setFocusable(false);

                // Add some padding to the item
                int padding = getResources().getDimensionPixelSize(R.dimen.tagged_note_padding);
                holder.itemView.setPadding(padding, padding, padding, padding);
            }
        };
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadTaggedNotes();
        return view;
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void loadTaggedNotes() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("shared_notes")
                .whereEqualTo("taggedUserId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    List<Note> notes = new ArrayList<>();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            SharedNote sharedNote = doc.toObject(SharedNote.class);
                            if (sharedNote != null) {
                                Note note = new Note(
                                        Integer.parseInt(sharedNote.getNoteId()),
                                        sharedNote.getTitle(),
                                        sharedNote.getContent(),
                                        sharedNote.getCategory(),
                                        sharedNote.getTimestamp() != null ? sharedNote.getTimestamp().getTime()
                                                : System.currentTimeMillis(),
                                        0);
                                notes.add(note);
                            }
                        }
                    }

                    adapter.updateNotes(notes);

                    emptyState.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(notes.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }
}
