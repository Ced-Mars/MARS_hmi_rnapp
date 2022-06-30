package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;

/** Listens to DataItems and Messages from the local node. */
public class DataLayerListenerService extends WearableListenerService implements DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener {

    private static final String TAG = "DataLayerService";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    public static final String COUNT_PATH = "/count";
    public static final String ACTION_PATH = "/action";
    public static final String SEQUENCE_PATH = "/sequence";

    private Vibrator vibrator;
    private VibrationEffect vibe;
    private final long[] vibrationPattern = {0, 500, 50, 300};

    NotificationManagerCompat notificationManager;

    private static final String CHANNEL_ID = "wear";

    private ItemViewModel viewModel;

    String[] lastData;
    String lastPath;

    boolean allowRebind; // indicates whether onRebind should be used



    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "creation of service");

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        int indexInPatternToRepeat = -1;
        vibe = VibrationEffect.createWaveform(vibrationPattern, indexInPatternToRepeat);

        createNotificationChannel();
        notificationManager = NotificationManagerCompat.from(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        if(!(lastPath == null)){
            if(lastPath.contains(COUNT_PATH)){
                Intent newIntent = new Intent("UserActionUpdateIntent");
                newIntent.putExtra("data", lastData);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }else if(lastPath.contains(ACTION_PATH)){
                Intent newIntent = new Intent("RobotSequenceUpdateIntent");
                newIntent.putExtra("data", lastData);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }else{
                Log.e(TAG, "onStartCommand, lastpath registered not recongnized");
            }
        }


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        Log.d(TAG, "A Client has unbind");
        return allowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }




    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            Log.d(TAG, "DataEvent event : " + event);
            //Called if data has been modified on node
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                if (path.contains(COUNT_PATH)) { //Inside USER action
                    lastPath = COUNT_PATH;
                    Log.d(TAG, "Data Changed for COUNT_PATH");
                    startRiging();
                    sendNotification("Action Utilisateur Requise !", "Action Requise");

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    lastData = dataMapItem.getDataMap().getStringArray("count_key");
                    Log.d(TAG, Arrays.toString(dataMapItem.getDataMap().getStringArray("count_key")));

                    if(dataMapItem.getDataMap().getStringArray("count_key")[1].isEmpty()){
                        Intent intent = new Intent("SequenceUpdateIntent");
                        intent.putExtra("data", dataMapItem.getDataMap().getStringArray("count_key"));
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }else{
                        Intent intent = new Intent("UserActionUpdateIntent");
                        intent.putExtra("data", dataMapItem.getDataMap().getStringArray("count_key"));
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                }
                else if (path.contains(ACTION_PATH)) {//Inside Robot action
                    lastPath = ACTION_PATH;
                    Log.d(TAG, "Data Changed for ACTION_PATH");

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    lastData = dataMapItem.getDataMap().getStringArray("action_key");
                    Log.d(TAG, Arrays.toString(dataMapItem.getDataMap().getStringArray("action_key")));

                    if(dataMapItem.getDataMap().getStringArray("action_key")[1].isEmpty()){
                        Intent intent = new Intent("SequenceUpdateIntent");
                        intent.putExtra("data", dataMapItem.getDataMap().getStringArray("action_key"));
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }else{
                        Intent intent = new Intent("RobotSequenceUpdateIntent");
                        intent.putExtra("data", dataMapItem.getDataMap().getStringArray("action_key"));
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                }
                else if (path.contains(SEQUENCE_PATH)){//Beginning or end of sequence
                    lastPath = SEQUENCE_PATH;
                    Log.d(TAG, "Data Changed for SEQUENCE_PATH");

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    lastData = dataMapItem.getDataMap().getStringArray("sequence_key");
                    Log.d(TAG, Arrays.toString(dataMapItem.getDataMap().getStringArray("sequence_key")));

                    Intent intent = new Intent("SequenceUpdateIntent");
                    intent.putExtra("data", dataMapItem.getDataMap().getStringArray("sequence_key"));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
        }
    }


    public class LocalBinder extends Binder {
        DataLayerListenerService getService(){
            return DataLayerListenerService.this;
        }
    }

    @Override
    public void onPeerConnected(@NonNull Node peer) {
        super.onPeerConnected(peer);
        Log.d(TAG, "onPeerConnected: " + peer.getDisplayName());
    }

    @Override
    public void onPeerDisconnected(@NonNull Node peer) {
        super.onPeerDisconnected(peer);
        Log.d(TAG, "onPeerDisconnected: " + peer.getDisplayName());
    }

    //Using Message Client with Bluetooth (reliable) and to send message from one device to another
    //Must send small data < 100 KiloByte
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
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

    private void sendNotification(String content, String title){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.alert_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /** method for clients */
    public String[] getLastData() {
        return lastData;
    }

    public String getLastPath(){
        return lastPath;
    }

}