package com.example.curs.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.curs.R;
import com.example.curs.data.local.PetEntity;
import java.util.ArrayList;
import java.util.List;


public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<PetEntity> pets = new ArrayList<>();
    private OnPetClickListener listener;

    public interface OnPetClickListener {
        void onPetClick(PetEntity pet);
    }

    public void setListener(OnPetClickListener listener) {
        this.listener = listener;
    }

    public void setPets(List<PetEntity> pets) {
        this.pets = pets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        PetEntity pet = pets.get(position);
        holder.tvName.setText(pet.name);
        holder.tvInfo.setText(pet.species + ", " + pet.breed);

        if (pet.photoUrl != null && !pet.photoUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(pet.photoUrl)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPetClick(pet);
        });
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName, tvInfo;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPetPhoto);
            tvName = itemView.findViewById(R.id.tvPetName);
            tvInfo = itemView.findViewById(R.id.tvPetInfo);
        }
    }
}
