package com.example.umbrella;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.umbrella.databinding.FragmentFirstBinding;

// 페이지 시작
public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private double lat;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // page binding this FirstFragment
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        System.out.println("TEST: onCreateView");
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("TEST: onViewCreated");

        // go to SecondFragment button
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        lat = ((MainActivity) getActivity()).getLat();
        binding.testTextView.setText("lat :" + lat);
        System.out.println("TEST: onStart");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}