package com.example.notes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.model.UserTag;

public class UserSearchAdapter extends ListAdapter<UserTag, UserSearchAdapter.UserViewHolder> {

    private final OnUserSelectedListener listener;

    public interface OnUserSelectedListener {
        void onUserSelected(UserTag user);
    }

    public UserSearchAdapter(OnUserSelectedListener listener) {
        super(new DiffUtil.ItemCallback<UserTag>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserTag oldItem, @NonNull UserTag newItem) {
                return oldItem.getUserId().equals(newItem.getUserId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserTag oldItem, @NonNull UserTag newItem) {
                return oldItem.getEmail().equals(newItem.getEmail());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserTag user = getItem(position);
        holder.emailText.setText(user.getEmail());
        holder.itemView.setOnClickListener(v -> listener.onUserSelected(user));
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailText;

        UserViewHolder(View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.email_text);
        }
    }
}
