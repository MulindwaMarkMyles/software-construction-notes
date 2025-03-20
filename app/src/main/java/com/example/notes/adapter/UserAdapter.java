package com.example.notes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notes.R;
import com.example.notes.model.UserTag;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<UserTag> users = new ArrayList<>();
    private OnUserSelectedListener listener;

    public interface OnUserSelectedListener {
        void onUserSelected(UserTag user);
    }

    public UserAdapter(OnUserSelectedListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserTag> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        UserTag user = users.get(position);
        holder.emailText.setText(user.getEmail());
        holder.itemView.setOnClickListener(v -> listener.onUserSelected(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailText;

        UserViewHolder(View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.email_text);
        }
    }
}
