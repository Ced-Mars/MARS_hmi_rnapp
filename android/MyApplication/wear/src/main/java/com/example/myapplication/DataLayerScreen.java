package com.example.myapplication;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classes representing data used for each custom {@link ViewHolder} in {@link
 * CustomRecyclerAdapter}.
 */
public class DataLayerScreen {

    public static final int TYPE_EVENT_LOGGING = 1;
    public static final int TYPE_CAPABILITY_DISCOVERY = 2;

    /**
     * All classes representing data for {@link ViewHolder} must implement this interface so {@link
     * CustomRecyclerAdapter} knows what type of {@link ViewHolder} to inflate.
     */
    public interface DataLayerScreenData {
        int getType();
    }

    /**
     * Represents message event logs passed to Wear device via {@link
     * com.google.android.gms.wearable.MessageClient} data layer API.
     */
    public static class EventLoggingData implements DataLayerScreenData {

        private JSONObject mLogBuilder;

        EventLoggingData() throws JSONException {
            mLogBuilder = new JSONObject();
            mLogBuilder.put("time", "");
            mLogBuilder.put("action", "");
        }

        @Override
        public int getType() {
            return TYPE_EVENT_LOGGING;
        }

        public JSONObject getLog() {
            return mLogBuilder;
        }

        public void addEventLog(String eventName, String data) throws JSONException {
            if (eventName.equals("Count")){
                mLogBuilder.put("time", Integer.valueOf(data));
            }else if (eventName.equals("Action")){
                mLogBuilder.put("action", data);
            }else{
                Log.i("Event", "Unknow Event");
            }
        }
    }

    /**
     * No extra data needed as the {@link ViewHolder} only contains buttons checking capabilities of
     * devices via the {@link com.google.android.gms.wearable.CapabilityClient} data layer API.
     */
    public static class CapabilityDiscoveryData implements DataLayerScreenData {

        @Override
        public int getType() {
            return TYPE_CAPABILITY_DISCOVERY;
        }
    }
}