package com.example.umbrella;

import android.app.Activity;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.umbrella.databinding.FragmentFirstBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

// 페이지 시작
public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    private double lat;
    private double lon;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart() {
        super.onStart();
        lat = ((MainActivity) getActivity()).getLat();
        lon = ((MainActivity) getActivity()).getLon();

//        int[] grid = mapToGrid(lat, lon);

//        try {
//            String result = getWeatherData(grid[0], grid[1]);
//            binding.testTextView2.setText(result);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        binding.testTextView.setText("xlat =" + grid[0] + " xlon=" + grid[1]);

        System.out.println("TEST: onStart");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}