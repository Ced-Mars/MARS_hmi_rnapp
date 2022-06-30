package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.wear.widget.CurvedTextView;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.Calendar;

public class FragmentRobot extends Fragment {
    private ItemViewModel viewModel;

    private static final String TAG = "FragmentRobot";

    public FragmentRobot() {
        super(R.layout.robot_fragment);
    }

    private CurvedTextView curvedTextView;
    private TextView tempsRestant;
    private TextView prochaineAction;
    private ProgressBar progress;
    int i;
    int j;
    int minutes = 0;
    int secondes = 0;
    Handler mHandler;
    BroadcastReceiver receiver;
    Runnable runnable;

    String next1;

    //Called before onViewCreated
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.robot_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null){
            //Restore Fragment State here
            prochaineAction.setText(savedInstanceState.getString("NEXT_ACTION"));
            i = savedInstanceState.getInt("COUNTER_I");
            j= savedInstanceState.getInt("COUNTER_J");
        }else{
            viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
            viewModel.getCurrent().observe(getViewLifecycleOwner(), (current) -> {
                Log.d(TAG, "Get Current Value from ViewModel:" + current);
            });
            viewModel.getNext().observe(getViewLifecycleOwner(), (next) -> {
                Log.d(TAG, "Get Next Value from ViewModel:" + next);
                next1 = next;
                prochaineAction.setText(next);
            });
            viewModel.getTime().observe(getViewLifecycleOwner(), (time) -> {
                Log.d(TAG, "Get Time Value from ViewModel:" + time);
                i = time;
                j= time;
            });
        }

        curvedTextView = view.findViewById(R.id.currentState);
        tempsRestant = view.findViewById(R.id.tempsRestant);
        prochaineAction = view.findViewById(R.id.prochaine_action);
        progress = view.findViewById(R.id.progress);

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

        runnable = () -> {
            try {
                updateProgress();
            } catch (Exception ignored) {

            } finally {
                mHandler.postDelayed(runnable, 1000);
            }
        };
        progress.setProgress(100);
        mHandler = new Handler();
        runnable.run();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(receiver != null){
            requireActivity().unregisterReceiver(receiver);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here
        outState.putString("NEXT_ACTION", next1);
        outState.putInt("COUNTER_I", i);
        outState.putInt("COUNTER_J", j);
    }

    private void updateProgress() {
        if(j > 0) {
            j -= 1;
            minutes = (j) / 60;
            secondes = (j) % 60;
        }else{
            minutes = 0;
            secondes = 0;
        }
        tempsRestant.setText(minutes + " min " + secondes + " s");
        if (j < 1) {
            mHandler.removeCallbacks(runnable);
        } else {
            progress.setProgress(j*100/i);
        }
    }

}
