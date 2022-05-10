package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.wear.widget.CurvedTextView;

import java.util.Calendar;

public class FragmentUser extends Fragment {
    private ItemViewModel viewModel;

    private static final String TAG = "FragmentUser";

    public FragmentUser(){
        super(R.layout.user_fragment);
    }

    private TextView nom_action;
    private CurvedTextView curvedTextView;

    BroadcastReceiver receiver;

    //Called before onViewCreated
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        curvedTextView = view.findViewById(R.id.currentState);
        nom_action = view.findViewById(R.id.nom_action);

        viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        viewModel.getCurrent().observe(getViewLifecycleOwner(), current -> {
            Log.d(TAG, "Get Current Value from ViewModel:" + current);
            nom_action.setText(current);
        });
        viewModel.getNext().observe(getViewLifecycleOwner(), next -> {
            Log.d(TAG, "Get Next Value from ViewModel:" + next);
        });
        viewModel.getTime().observe(getViewLifecycleOwner(), time -> {
            Log.d(TAG, "Get Time Value from ViewModel:" + time);
        });

        curvedTextView.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + " H " +
                Calendar.getInstance().get(Calendar.MINUTE));

        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                if(intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0){
                    curvedTextView.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + " H " +
                            Calendar.getInstance().get(Calendar.MINUTE));
                }
            }
        };
        requireActivity().registerReceiver(receiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(receiver != null){
            requireActivity().unregisterReceiver(receiver);
        }
    }
}
