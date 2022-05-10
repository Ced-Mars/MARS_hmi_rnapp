package com.example.myapplication;

import static com.example.myapplication.DataLayerScreen.TYPE_CAPABILITY_DISCOVERY;
import static com.example.myapplication.DataLayerScreen.TYPE_EVENT_LOGGING;

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

import org.json.JSONException;
import org.json.JSONObject;

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

            case TYPE_EVENT_LOGGING:
                EventLoggingData eventLoggingData = (EventLoggingData) mDataSet.get(position);

                EventLoggingViewHolder eventLoggingViewHolder = (EventLoggingViewHolder) viewHolder;

                JSONObject log = eventLoggingData.getLog();

                if (log.length() > 0) {
                    try {
                        eventLoggingViewHolder.logDataLayerInformation(eventLoggingData.getLog());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    public void appendToDataEventLog(@NonNull String eventName, @NonNull String details) throws JSONException {
        int index = findItemIndex(TYPE_EVENT_LOGGING);

        if (index > -1) {
            EventLoggingData dataItemType = (EventLoggingData) mDataSet.get(index);
            dataItemType.addEventLog(eventName, details);

            notifyItemChanged(index);
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
        private final TextView prochaineAction;
        private final CurvedTextView currentState;
        private final ProgressBar progress;
        int i;
        int minutes = 0;
        int secondes = 0;
        String action;

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
            prochaineAction = view.findViewById(R.id.prochaine_action_text);
        }

        @Override
        public String toString() {
            return (String) tempsRestant.getText();
        }

        public void logDataLayerInformation(JSONObject log) throws JSONException {
            mIntroTextView.setVisibility(View.INVISIBLE);
            mDataLogTextView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            currentState.setVisibility(View.VISIBLE);
            i = (int) log.get("time");
            action = (String) log.get("action");
            prochaineAction.setText(action);
            Log.i("compteur + action", "" + "" + i + "+" + action);
            if (i > 0) {
                progress.setProgress(i*100/i);
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