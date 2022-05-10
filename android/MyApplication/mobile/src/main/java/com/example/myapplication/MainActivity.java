package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.facebook.react.ReactFragment;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity implements DefaultHardwareBackBtnHandler,
        MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener, DataClient.OnDataChangedListener {

    private static final String TAG = "MainActivity";

//    private static final String COUNT_PATH = "/count";
//    private static final String COUNT_KEY = "count_key";
//    private static final String ACTION_PATH = "/action";
//    private static final String ACTION_KEY = "action_key";

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.43.102:4001");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSocket.on("ProchaineAction", onNewMessage);
        mSocket.on("TempsAction", onNewMessage);
        mSocket.connect();

        setContentView(R.layout.activity_chooser);

        Fragment reactNativeFragment = new ReactFragment.Builder()
                .setComponentName("myreactnativeapp")
                .setLaunchOptions(getLaunchOptions())
                .build();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.reactNativeFragment, reactNativeFragment)
                .commit();

    }

    @Override
    public void onResume() {
        super.onResume();

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onCapabilityChanged(@NonNull final CapabilityInfo capabilityInfo) {
        LOGD(TAG, "onCapabilityChanged: " + capabilityInfo);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents){
        LOGD(TAG, "onDataChanged: " + dataEvents);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        LOGD(
                TAG,
                "onMessageReceived() A message from watch was received:"
                        + messageEvent.getRequestId()
                        + " "
                        + messageEvent.getPath());
    }

    private Bundle getLaunchOptions() {
        Bundle initialProperties = new Bundle();
        initialProperties.putString("message", "message envoyé depuis android");
        return initialProperties;
    }


    @Override
    public void invokeDefaultOnBackPressed() {

    }

    /** As simple wrapper around Log.d */
    private static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }

    private Emitter.Listener onNewMessage = args -> this.runOnUiThread(() -> {
            Log.i("Get value from socket", "" + Arrays.toString(args));
        try {
            execute((String) args[2], (String) args[1], (JSONObject) args[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });


    public void execute(String key, String path, JSONObject value) throws JSONException {
        String[] dataToWear = new String[3];
        String current = value.getString("current");
        String next = value.getString("next");
        Integer time = value.getInt("time");
        dataToWear[0] = current;
        dataToWear[1] = next;
        dataToWear[2] = String.valueOf(time);

        Log.i(TAG, "New String[]: " + Arrays.toString(dataToWear));

        Executors.newSingleThreadExecutor().execute(() -> {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.createWithAutoAppendedId(path);
            putDataMapRequest.getDataMap().putStringArray(key, dataToWear);
            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            request.setUrgent();
            Task<DataItem> dataItemTask =
                    Wearable.getDataClient(getApplicationContext()).putDataItem(request);
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                DataItem dataItem = Tasks.await(dataItemTask);

                Log.i(TAG, "DataItem saved: " + dataItem);

            } catch (ExecutionException exception) {
                Log.e(TAG, "Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Interrupt occurred: " + exception);
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.off("ProchaineAction", onNewMessage);
        mSocket.off("TempsAction", onNewMessage);
        mSocket.disconnect();
    }
}