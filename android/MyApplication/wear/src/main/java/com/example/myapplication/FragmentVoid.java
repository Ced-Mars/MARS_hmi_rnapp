package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.wear.widget.CurvedTextView;

import java.util.Calendar;

public class FragmentVoid extends Fragment {
    BroadcastReceiver receiver;
    CurvedTextView curvedTextView;

    public FragmentVoid(){
        super(R.layout.void_fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.void_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        curvedTextView = view.findViewById(R.id.currentState);

        if(Calendar.getInstance().get(Calendar.MINUTE) < 10){
            curvedTextView.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + " H 0" +
                    Calendar.getInstance().get(Calendar.MINUTE));
        }else{
            curvedTextView.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + " H " +
                    Calendar.getInstance().get(Calendar.MINUTE));
        }

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
