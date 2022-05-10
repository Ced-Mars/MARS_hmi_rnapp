package com.example.myapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Half;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;
import java.util.Objects;

public class FragmentFin extends Fragment {
    private static final String TAG = "MainActivity";


    public FragmentFin(){
        super(R.layout.end_fragment);
    }

    //Called before onViewCreated
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.end_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler().postDelayed(() -> {
            if(getParentFragmentManager().findFragmentByTag("FIN") != null)
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container_view, FragmentVoid.class, null, "VOID").commit();
        }, 5000);
    }
}
