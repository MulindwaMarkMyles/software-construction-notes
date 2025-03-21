package com.example.notes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notes.R;
import com.example.notes.model.UserTag;

public class UserSearchAdapter extends ListAdapter<UserTag, UserSearchAdapter.ViewHolder> {

    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserTag user);
    }

    public UserSearchAdapter(OnUserClickListener listener) {
        super(new UserDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserTag user = getItem(position);
        holder.bind(user, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView emailView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            emailView = itemView.findViewById(R.id.user_email);
        }

        public void bind(final UserTag user, final OnUserClickListener listener) {
            emailView.setText(user.getEmail());
            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }

    static class UserDiffCallback extends DiffUtil.ItemCallback<UserTag> {
        @Override
        public boolean areItemsTheSame(@NonNull UserTag oldItem, @NonNull UserTag newItem) {
            return oldItem.getUserId().equals(newItem.getUserId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserTag oldItem, @NonNull UserTag newItem) {
            return oldItem.getEmail().equals(newItem.getEmail());
        }
    }
}
