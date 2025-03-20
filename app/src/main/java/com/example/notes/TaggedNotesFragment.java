package com.example.notes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.notes.model.SharedNote;
import java.util.ArrayList;

public class TaggedNotesFragment extends Fragment {
    private static final String TAG = "TaggedNotesFragment";
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tagged_notes, container, false);
        recyclerView = view.findViewById(R.id.tagged_notes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

                    Log.d(TAG, "Received shared notes update");
                    // Update UI with the received notes
                });
    }
}
