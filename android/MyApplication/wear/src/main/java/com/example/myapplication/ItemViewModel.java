package com.example.myapplication;

import android.content.ClipData;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ItemViewModel extends ViewModel implements DataClient.OnDataChangedListener {

    private static final String TAG = "ItemViewModel";

    // Create a LiveData with a String
    private MutableLiveData<String> current;
    private MutableLiveData<String> next;
    private MutableLiveData<Integer> time;

    public MutableLiveData<String> getCurrent() {
        if (current == null) {
            current = new MutableLiveData<>();
            current.setValue("current");
        }
        return current;
    }
    public MutableLiveData<String> getNext() {
        if (next == null) {
            next = new MutableLiveData<>();
            next.setValue("next");
        }
        return next;
    }
    public MutableLiveData<Integer> getTime() {
        if (time == null) {
            time = new MutableLiveData<>();
            time.setValue(1);
        }
        return time;
    }

    public void setCurrent(String data) {
        current.setValue(data);
    }
    public void setNext(String data) {
        next.setValue(data);
    }
    public void setTime(Integer data) {
        time.setValue(data);
    }

    //Use data client with network connection, used to communicate with the cloud and get updated info
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged(): " + dataEvents);
        for (DataEvent event : dataEvents) {
            Log.d(TAG, "event for loop : " + event);
            //Called if data has been modified on node
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.contains(DataLayerListenerService.COUNT_PATH)) { //Inside USER action
                    Log.d(TAG, "Data Changed for COUNT_PATH");
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                    Log.d( TAG + "COUNT", Arrays.toString(dataMapItem.getDataMap().getStringArray("count_key")));
                }
                else if (path.contains(DataLayerListenerService.ACTION_PATH)) {//Inside Robot action
                    Log.d(TAG, "Data Changed for ACTION_PATH");
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Log.d(TAG + "ACTION", Arrays.toString(dataMapItem.getDataMap().getStringArray("action_key")));
                }
                else if (path.contains(DataLayerListenerService.SEQUENCE_PATH)){//Beginning or end of sequence
                    Log.d(TAG, "Data Changed for SEQUENCE_PATH");
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Log.d( TAG +"FIN", Arrays.toString(dataMapItem.getDataMap().getStringArray("sequence_key")));
                }else {
                    Log.d(TAG, "Unrecognized path: " + path);
                }
            }
            //Called if data has been deleted on node
            else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d( TAG + "DataItem Deleted", event.getDataItem().toString());
            } else {
                Log.d( TAG + "Unknown data event type\", \"Type = ", String.valueOf(event.getType()));
            }
        }
    }

}

