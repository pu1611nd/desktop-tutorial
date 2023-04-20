package com.pupu.pu1611.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pupu.pu1611.databinding.ItemContainerUserBinding;
import com.pupu.pu1611.databinding.ItemContainerUserChatBinding;
import com.pupu.pu1611.listeners.UserListenerChat;
import com.pupu.pu1611.models.User;

import java.util.List;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.UserViewHolder>{
    private final List<User> users;
    private final UserListenerChat userListenerChat;

    public UserChatAdapter(List<User> users, UserListenerChat userListenerChat) {
        this.users = users;
        this.userListenerChat = userListenerChat;
    }

    ItemContainerUserChatBinding binding;

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserChatBinding itemContainerUserChatBinding = ItemContainerUserChatBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserChatBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder{
        UserViewHolder(ItemContainerUserChatBinding itemContainerUserChatBinding){
            super(itemContainerUserChatBinding.getRoot());
            binding = itemContainerUserChatBinding;
        }

        void setUserData(User user){
            binding.textName.setText(user.getFirstName()+" "+user.getLastName());
            binding.textEmail.setText(user.getEmail());
            binding.imageProfile.setImageBitmap(getUserImage(user.getImage()));
            binding.getRoot().setOnClickListener(v -> userListenerChat.onUserClicked(user));
        }
    }

    private Bitmap getUserImage (String encodeImage){
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0,bytes.length);
    }
}
