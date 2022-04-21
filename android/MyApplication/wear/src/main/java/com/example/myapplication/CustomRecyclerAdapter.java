package com.example.myapplication;

import static com.example.myapplication.DataLayerScreen.TYPE_CAPABILITY_DISCOVERY;
import static com.example.myapplication.DataLayerScreen.TYPE_EVENT_LOGGING;
import static com.example.myapplication.DataLayerScreen.TYPE_IMAGE_ASSET;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.wear.widget.CurvedTextView;
import androidx.wear.widget.WearableRecyclerView;

import com.example.myapplication.DataLayerScreen.DataLayerScreenData;
import com.example.myapplication.DataLayerScreen.EventLoggingData;
import com.example.myapplication.DataLayerScreen.ImageAssetData;

import java.util.ArrayList;

/**
 * Populates a {@link WearableRecyclerView}.
 *
 * <p>Provides a custom {@link ViewHolder} for each unique row associated with a feature from the
 * Data Layer APIs (one for transferring images, one for event logging, and one for checking
 * capabilities). Data for each {@link ViewHolder} populated by {@link DataLayerScreen}.
 */
public class CustomRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "CustomRecyclerAdapter";

    private ArrayList<DataLayerScreenData> mDataSet;

    public CustomRecyclerAdapter(ArrayList<DataLayerScreenData> dataSet) {
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.d(TAG, "onCreateViewHolder(): viewType: " + viewType);

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case TYPE_IMAGE_ASSET:
                viewHolder =
                        new ImageAssetViewHolder(
                                LayoutInflater.from(viewGroup.getContext())
                                        .inflate(
                                                R.layout.recycler_row_image_asset,
                                                viewGroup,
                                                false));
                break;

            case TYPE_EVENT_LOGGING:
                viewHolder =
                        new EventLoggingViewHolder(
                                LayoutInflater.from(viewGroup.getContext())
                                        .inflate(
                                                R.layout.recycler_row_event_logging,
                                                viewGroup,
                                                false));
                break;

            case TYPE_CAPABILITY_DISCOVERY:
                viewHolder =
                        new CapabilityDiscoveryViewHolder(
                                LayoutInflater.from(viewGroup.getContext())
                                        .inflate(
                                                R.layout.recycler_row_capability_discovery,
                                                viewGroup,
                                                false));
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");

        switch (viewHolder.getItemViewType()) {
            case TYPE_IMAGE_ASSET:
                ImageAssetData imageAssetData = (ImageAssetData) mDataSet.get(position);

                ImageAssetViewHolder imageAssetViewHolder = (ImageAssetViewHolder) viewHolder;
                imageAssetViewHolder.setBackgroundImage(imageAssetData.getBitmap());
                break;

            case TYPE_EVENT_LOGGING:
                EventLoggingData eventLoggingData = (EventLoggingData) mDataSet.get(position);

                EventLoggingViewHolder eventLoggingViewHolder = (EventLoggingViewHolder) viewHolder;

                String log = eventLoggingData.getLog();

                if (log.length() > 0) {
                    eventLoggingViewHolder.logDataLayerInformation(eventLoggingData.getLog());
                }
                break;

            case TYPE_CAPABILITY_DISCOVERY:
                // This view never changes, as it contains just two buttons that trigger
                // capabilities requests to other devices.
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        DataLayerScreenData dataLayerScreenData = mDataSet.get(position);
        return dataLayerScreenData.getType();
    }

    private int findItemIndex(@NonNull int assetType) {

        for (int index = 0; index < mDataSet.size(); index++) {
            if (mDataSet.get(index).getType() == assetType) {
                return index;
            }
        }
        return -1;
    }

    public void appendToDataEventLog(@NonNull String eventName, @NonNull String details) {
        int index = findItemIndex(TYPE_EVENT_LOGGING);

        if (index > -1) {
            EventLoggingData dataItemType = (EventLoggingData) mDataSet.get(index);
            dataItemType.addEventLog(eventName, details);

            notifyItemChanged(index);
        }
    }

    public int setImageAsset(Bitmap bitmap) {

        int index = findItemIndex(TYPE_IMAGE_ASSET);

        if ((index > -1) && (bitmap != null)) {
            ImageAssetData imageAssetData = (ImageAssetData) mDataSet.get(index);
            imageAssetData.setBitmap(bitmap);
            notifyItemChanged(index);

            return index;

        } else {
            return -1;
        }
    }

    /** ***** Classes representing custom {@link ViewHolder}. ****** */

    /**
     * Displays {@link Bitmap} passed from other devices via the {@link
     * com.google.android.gms.wearable.Asset} API.
     */
    public static class ImageAssetViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mImageView;

        public ImageAssetViewHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.image);
        }

        public void setBackgroundImage(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Displays text log of data passed from other devices via the {@link
     * com.google.android.gms.wearable.MessageClient} API.
     */
    public static class EventLoggingViewHolder extends RecyclerView.ViewHolder {

        private final TextView mIntroTextView;
        private final LinearLayout mDataLogTextView;
        private final TextView tempsRestant;
        private final CurvedTextView currentState;
        private final ProgressBar progress;
        int i;
        int minutes = 0;
        int secondes = 0;

        public EventLoggingViewHolder(View view) {
            super(view);
            mIntroTextView = view.findViewById(R.id.intro);
            mDataLogTextView = view.findViewById(R.id.event_logging);
            mDataLogTextView.setVisibility(View.INVISIBLE);
            tempsRestant = view.findViewById(R.id.tempsRestant);
            currentState = view.findViewById(R.id.currentState);
            currentState.setVisibility(View.INVISIBLE);
            progress = view.findViewById(R.id.progress);
            progress.setVisibility(View.INVISIBLE);
        }

        @Override
        public String toString() {
            return (String) tempsRestant.getText();
        }

        public void logDataLayerInformation(String log) {
            mIntroTextView.setVisibility(View.INVISIBLE);
            mDataLogTextView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            currentState.setVisibility(View.VISIBLE);
            Log.i("compteur", log);
            i = Integer.parseInt(log);
            if (i > 0) {
                progress.setProgress(i*100/60);
                minutes = (i) / 60;
                secondes = (i) % 60;
                tempsRestant.setText(minutes + " min " + secondes + " s");

            } else {
                Log.i("Counter Info", "Counter value : " + i);
            }

        }
    }

    /**
     * Displays two buttons for querying device capabilities via {@link
     * com.google.android.gms.wearable.CapabilityClient}.
     */
    public static class CapabilityDiscoveryViewHolder extends RecyclerView.ViewHolder {

        public CapabilityDiscoveryViewHolder(View view) {
            super(view);
        }
    }
}