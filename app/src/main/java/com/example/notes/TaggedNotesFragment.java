package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.notes.model.SharedNote;
import java.util.ArrayList;
import java.util.List;

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
        adapter = new NoteAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        
        // Set item click listener for the adapter
        adapter.setOnItemClickListener(position -> {
            Note note = adapter.getNoteAtPosition(position);
            Intent intent = new Intent(getActivity(), NoteDetailActivity.class);
            intent.putExtra("noteId", note.getId());
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadTaggedNotes();
        return view;
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
                                        sharedNote.getTimestamp() != null ? 
                                            sharedNote.getTimestamp().getTime() : 
                                            System.currentTimeMillis(),
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
