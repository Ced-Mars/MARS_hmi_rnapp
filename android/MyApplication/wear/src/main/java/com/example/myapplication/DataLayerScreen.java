package com.example.myapplication;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * Classes representing data used for each custom {@link ViewHolder} in {@link
 * CustomRecyclerAdapter}.
 */
public class DataLayerScreen {

    public static final int TYPE_IMAGE_ASSET = 0;
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
     * Represents {@link Bitmap} passed to Wear device via {@link
     * com.google.android.gms.wearable.Asset} data layer API.
     */
    public static class ImageAssetData implements DataLayerScreenData {

        private Bitmap mBitmap;

        ImageAssetData(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        @Override
        public int getType() {
            return TYPE_IMAGE_ASSET;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
        }
    }

    /**
     * Represents message event logs passed to Wear device via {@link
     * com.google.android.gms.wearable.MessageClient} data layer API.
     */
    public static class EventLoggingData implements DataLayerScreenData {

        private StringBuilder mLogBuilder;

        EventLoggingData() {
            mLogBuilder = new StringBuilder();
        }

        @Override
        public int getType() {
            return TYPE_EVENT_LOGGING;
        }

        public String getLog() {
            return mLogBuilder.toString();
        }

        public void addEventLog(String eventName, String data) {
            Log.i("data to mLogBuilder : ", data);
            mLogBuilder.setLength(0); // set length of buffer to 0
            mLogBuilder.trimToSize(); // trim the underlying buffer
            mLogBuilder.append(data);//Add data to StringBuilder
            //mLogBuilder.append("\n" + eventName + "\n" + data);
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