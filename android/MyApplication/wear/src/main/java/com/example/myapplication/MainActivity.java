package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Arrays;
import java.util.Objects;


public class MainActivity extends FragmentActivity{

    private static final String TAG = "MainActivity";

    public String fragmntName;
    private ItemViewModel viewModel;

    private BroadcastReceiver mMessageReceiver;

    DataLayerListenerService mService;
    boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DataLayerListenerService.LocalBinder binder = (DataLayerListenerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.d(TAG, "Service Connection, onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.d(TAG, "Service Connection, onServiceDisconnected");
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MAin activity", "dans on create");

        Intent intent = new Intent(this, DataLayerListenerService.class);
        startService(intent);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String[] dataUpdate = intent.getStringArrayExtra("data");
                fragmentDisplayManagement(action, dataUpdate);

                Log.d(TAG, action + Arrays.toString(dataUpdate));
            }
        };

        if (savedInstanceState == null) {
            Log.d(TAG, "savedInstanceState null");
            FragmentManager fragMgr = getSupportFragmentManager();
            FragmentTransaction fragTrans = fragMgr.beginTransaction();
            fragTrans
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, FragmentVoid.class, null, "VOID")
                    .commit();
            fragmntName = "VOID";
        }else{
            Log.d(TAG, "Dans savedInstanceState");
            fragmntName = savedInstanceState.getString("FRAGMENT_NAME");
            if(fragmntName.equals("USER")){
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.fragment_container_view, FragmentUser.class, null, "USER").commit();
            }else if (fragmntName.equals("ACTION")){
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.fragment_container_view, FragmentRobot.class, null, "ACTION").commit();
            }else{
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.fragment_container_view, FragmentVoid.class, null, "VOID").commit();
            }
        }
        setContentView(R.layout.activity_main);

        //Initiate and affecting ViewModel to this activity
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        viewModel.getCurrent().observe(this, current -> {
            Log.d(TAG, "Get Current Value from ViewModel:" + current);
        });
        viewModel.getNext().observe(this, next -> {
            Log.d(TAG, "Get Next Value from ViewModel:" + next);
        });
        viewModel.getTime().observe(this, time -> {
            Log.d(TAG, "Get Time Value from ViewModel:" + time);
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
    }



    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("RobotSequenceUpdateIntent"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("UserActionUpdateIntent"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("SequenceUpdateIntent"));

        if(mBound){
            String[] data = mService.getLastData();
            String path = mService.getLastPath();
            Log.d(TAG, "LAST PATH : " + path + "LAST DATA" + Arrays.toString(data));
        }else{
            Log.d(TAG, "Service not bounded");
        }

        Log.d(TAG, "MainActivity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        Log.d(TAG, "MainActivity onPause");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(connection);
        mBound = false;

        Log.d(TAG, "MainActivity onDestroy");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "dans onSavedInstanceState");
        outState.putString("FRAGMENT_NAME", fragmntName);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "dans onRestoreInstanceState");
    }

    public void fragmentDisplayManagement(String action, String[] data){
        //Use data client with network connection, used to communicate with the cloud and get updated info
        if (action.contains("UserActionUpdateIntent")) { //Inside USER action
            Log.d(TAG, "Data Changed for COUNT_PATH");
            FragmentUser FRAGMENT = (FragmentUser) getSupportFragmentManager().findFragmentByTag("USER");
            if (FRAGMENT != null && FRAGMENT.isVisible()) {
                Log.d(TAG, "USER FRAGMENT ALREADY VISIBLE");
                Log.d("COUNT", Arrays.toString(data));
                viewModel.setCurrent(Objects.requireNonNull(data[0]));
                viewModel.setNext(Objects.requireNonNull(data[1]));
                viewModel.setTime(Integer.valueOf(Objects.requireNonNull(data[2])));
            }else{
                Log.d(TAG, "USER FRAGMENT NOT VISIBLE");
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.fragment_container_view, FragmentUser.class, null, "USER").commit();
                fragmntName = "USER";
                Log.d("COUNT", Arrays.toString(data));
                viewModel.setCurrent(Objects.requireNonNull(data[0]));
                viewModel.setNext(Objects.requireNonNull(data[1]));
                viewModel.setTime(Integer.valueOf(Objects.requireNonNull(data[2])));
            }
        }
        else if (action.contains("RobotSequenceUpdateIntent")) {//Inside Robot action
            Log.d(TAG, "Data Changed for ACTION_PATH");
            FragmentRobot FRAGMENT = (FragmentRobot) getSupportFragmentManager().findFragmentByTag("ACTION");
            //Fragment is already visible
            if (FRAGMENT != null && FRAGMENT.isVisible()) {
                Log.d(TAG, "ROBOT FRAGMENT ALREADY VISIBLE");
                Log.d("ACTION", Arrays.toString(data));
                viewModel.setCurrent(Objects.requireNonNull(data[0]));
                viewModel.setNext(Objects.requireNonNull(data[1]));
                viewModel.setTime(Integer.valueOf(Objects.requireNonNull(data[2])));
            }else{
                Log.d(TAG, "ROBOT FRAGMENT NOT VISIBLE");
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.fragment_container_view, FragmentRobot.class, null, "ACTION").commit();
                fragmntName = "ACTION";
                Log.d("ACTION", Arrays.toString(data));
                viewModel.setCurrent(Objects.requireNonNull(data[0]));
                viewModel.setNext(Objects.requireNonNull(data[1]));
                viewModel.setTime(Integer.valueOf(data[2]));
            }
        }
        else if (action.contains("SequenceUpdateIntent")){//Beginning or end of sequence
            Log.d(TAG, "Data Changed for SEQUENCE_PATH");
            FragmentFin FRAGMENT = (FragmentFin) getSupportFragmentManager().findFragmentByTag("FIN");
            //Fragment is already visible
            if (FRAGMENT != null && FRAGMENT.isVisible()) {
                Log.d(TAG, "FIN FRAGMENT ALREADY VISIBLE");
                Log.d("FIN", Arrays.toString(data));
                viewModel.setCurrent(Objects.requireNonNull(data[0]));
                viewModel.setNext(Objects.requireNonNull(data[1]));
                viewModel.setTime(Integer.valueOf(data[2]));
            }else{
                Log.d(TAG, "FIN FRAGMENT NOT VISIBLE");
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.fragment_container_view, FragmentFin.class, null, "FIN").commit();
                fragmntName = "FIN";
                Log.d("FIN", Arrays.toString(data));
                viewModel.setCurrent(Objects.requireNonNull(data[0]));
                viewModel.setNext(Objects.requireNonNull(data[1]));
                viewModel.setTime(Integer.valueOf(data[2]));
            }
        }else {
            Log.d(TAG, "Unrecognized path: " + action);
        }
    }
}