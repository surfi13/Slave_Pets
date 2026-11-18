package com.example.curs.ui.pets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curs.R;
import com.example.curs.data.local.PetEntity;
import com.example.curs.data.repository.PetRepository;
import com.example.curs.ui.AddEditPetActivity;
import com.example.curs.ui.main.PetAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PetsFragment extends Fragment {

    private PetRepository petRepository;
    private PetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        petRepository = new PetRepository(requireActivity().getApplication());

        RecyclerView rv = view.findViewById(R.id.rvPets);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PetAdapter();
        adapter.setListener(new PetAdapter.OnPetClickListener() {
            @Override
            public void onPetClick(PetEntity pet) {
                Intent intent = new Intent(requireContext(), AddEditPetActivity.class);
                intent.putExtra("PET_ID", pet.id);
                startActivity(intent);
            }

            @Override
            public void onPetDelete(PetEntity pet) {
                petRepository.deletePet(pet);
                Toast.makeText(requireContext(), "Питомец удалён", Toast.LENGTH_SHORT).show();
            }
        });
        rv.setAdapter(adapter);


        petRepository.getAllPets().observe(getViewLifecycleOwner(), pets -> {
            if (pets != null && !pets.isEmpty()) {
                adapter.setPets(pets);
            }
        });

        // подтянуть данные из Firebase (если есть сеть)
        petRepository.fetchPetsFromCloud();
    }
}