package com.example.myapplication;

import android.icu.text.LocaleDisplayNames;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MainActivity extends FragmentActivity
        implements DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener{

    private static final String TAG = "MainActivity";

    public String fragmntName;
    private ItemViewModel viewModel;

    private Vibrator vibrator;
    private VibrationEffect vibe;

    private final long[] vibrationPattern = {0, 500, 50, 300};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        int indexInPatternToRepeat = -1;
        vibe = VibrationEffect.createWaveform(vibrationPattern, indexInPatternToRepeat);
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



        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("FRAGMENT_NAME", fragmntName);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "dans onRestoreInstanceState");
    }

    //Use data client with network connection, used to communicate with the cloud and get updated info
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        startRiging();
        Log.d(TAG, "onDataChanged(): " + dataEvents);
        for (DataEvent event : dataEvents) {
            Log.d(TAG, "event for loop : " + event);
            //Called if data has been modified on node
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.contains(DataLayerListenerService.COUNT_PATH)) { //Inside USER action
                    Log.d(TAG, "Data Changed for COUNT_PATH");
                    FragmentUser FRAGMENT = (FragmentUser) getSupportFragmentManager().findFragmentByTag("USER");
                    if (FRAGMENT != null && FRAGMENT.isVisible()) {
                        Log.d(TAG, "USER FRAGMENT ALREADY VISIBLE");
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Log.d("COUNT", Arrays.toString(dataMapItem.getDataMap().getStringArray("count_key")));
                        viewModel.setCurrent(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("count_key"))[0]);
                        viewModel.setNext(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("count_key"))[1]);
                        viewModel.setTime(Integer.valueOf(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("count_key"))[2]));
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
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Log.d("COUNT", Arrays.toString(dataMapItem.getDataMap().getStringArray("count_key")));
                        viewModel.setCurrent(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("count_key"))[0]);
                        viewModel.setNext(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("count_key"))[1]);
                        viewModel.setTime(Integer.valueOf(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("count_key"))[2]));
                    }
                }
                else if (path.contains(DataLayerListenerService.ACTION_PATH)) {//Inside Robot action
                    Log.d(TAG, "Data Changed for ACTION_PATH");
                    FragmentRobot FRAGMENT = (FragmentRobot) getSupportFragmentManager().findFragmentByTag("ACTION");
                    //Fragment is already visible
                    if (FRAGMENT != null && FRAGMENT.isVisible()) {
                        Log.d(TAG, "ROBOT FRAGMENT ALREADY VISIBLE");
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Log.d("ACTION", Arrays.toString(dataMapItem.getDataMap().getStringArray("action_key")));
                        viewModel.setCurrent(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("action_key"))[0]);
                        viewModel.setNext(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("action_key"))[1]);
                        viewModel.setTime(Integer.valueOf(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("action_key"))[2]));
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
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Log.d("ACTION", Arrays.toString(dataMapItem.getDataMap().getStringArray("action_key")));
                        viewModel.setCurrent(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("action_key"))[0]);
                        viewModel.setNext(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("action_key"))[1]);
                        viewModel.setTime(Integer.valueOf(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("action_key"))[2]));
                    }
                }
                else if (path.contains(DataLayerListenerService.SEQUENCE_PATH)){//Beginning or end of sequence
                    Log.d(TAG, "Data Changed for SEQUENCE_PATH");
                    FragmentFin FRAGMENT = (FragmentFin) getSupportFragmentManager().findFragmentByTag("FIN");
                    //Fragment is already visible
                    if (FRAGMENT != null && FRAGMENT.isVisible()) {
                        Log.d(TAG, "FIN FRAGMENT ALREADY VISIBLE");
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Log.d("FIN", Arrays.toString(dataMapItem.getDataMap().getStringArray("sequence_key")));
                        viewModel.setCurrent(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("sequence_key"))[0]);
                        viewModel.setNext(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("sequence_key"))[1]);
                        viewModel.setTime(Integer.valueOf(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("sequence_key"))[2]));
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
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Log.d("FIN", Arrays.toString(dataMapItem.getDataMap().getStringArray("sequence_key")));
                        viewModel.setCurrent(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("sequence_key"))[0]);
                        viewModel.setNext(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("sequence_key"))[1]);
                        viewModel.setTime(Integer.valueOf(Objects.requireNonNull(dataMapItem.getDataMap().getStringArray("sequence_key"))[2]));
                    }
                }else {
                    Log.d(TAG, "Unrecognized path: " + path);
                }
            }
            //Called if data has been deleted on node
            else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("DataItem Deleted", event.getDataItem().toString());
            } else {
                Log.d("Unknown data event type\", \"Type = ", String.valueOf(event.getType()));
            }
            //Possibly trying to delete node but at the end of the sequence - maybe doing it from mobile
//            Executors.newSingleThreadExecutor().execute(() -> {
//                Task<Integer> dataItemTask = Wearable.getDataClient(getApplicationContext()).deleteDataItems(event.getDataItem().getUri());
//                try {
//                    // Block on a task and get the result synchronously (because this is on a background
//                    // thread).
//                    Integer dataItem = Tasks.await(dataItemTask);
//
//                    Log.i(TAG, "DataItem removed: " + dataItem);
//
//                } catch (ExecutionException exception) {
//                    Log.e(TAG, "Task failed: " + exception);
//
//                } catch (InterruptedException exception) {
//                    Log.e(TAG, "Interrupt occurred: " + exception);
//                }
//            });
        }
    }

    private void startRiging(){

        int ringerMode = ((AudioManager) getSystemService(AUDIO_SERVICE)).getRingerMode();

        if(ringerMode == AudioManager.RINGER_MODE_SILENT){
            return;
        }

        vibrator.vibrate(vibe);

        if(ringerMode == AudioManager.RINGER_MODE_VIBRATE){
            return;
        }

        Ringtone ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE));
        ringtone.play();
    }

    //Using Message Client with Bluetooth (reliable) and to send message from one device to another
    //Must send small data < 100 KiloByte
    @Override
    public void onMessageReceived(MessageEvent event) {
        Log.d(TAG, "onMessageReceived: " + event);
        Log.d("Message : ", event.toString());
    }
}