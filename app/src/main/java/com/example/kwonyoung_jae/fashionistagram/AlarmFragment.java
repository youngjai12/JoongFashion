package com.example.kwonyoung_jae.fashionistagram;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;

import android.app.FragmentTransaction;
import android.view.ViewGroup;
public class AlarmFragment extends Fragment {
    public AlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

}