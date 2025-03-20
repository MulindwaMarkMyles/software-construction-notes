package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
    private TextView emptyView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NoteAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tagged_notes, container, false);
        recyclerView = view.findViewById(R.id.tagged_notes_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NoteAdapter(getContext(), new ArrayList<>()) {
            @Override
            public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                // Show full content instead of preview
                Note note = getNoteAtPosition(position);
                holder.contentPreview.setMaxLines(Integer.MAX_VALUE);
                holder.contentPreview.setEllipsize(null);

                // Add "Shared by" info
                holder.dateTextView.setText(String.format("Shared by %s • %s",
                        note.getTitle(),
                        formatDate(note.getTimestamp())));

                // Remove click feedback
                holder.itemView.setBackground(null);
                holder.itemView.setClickable(false);
                holder.itemView.setFocusable(false);
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

                    emptyView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(notes.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }
}
