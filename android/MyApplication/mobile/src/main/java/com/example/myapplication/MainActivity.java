package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactFragment;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Simple list-based Activity to redirect to one of the other Activities. The code here is
 * uninteresting, {@link SignInActivity} is a good place to start if you are curious about
 * {@code GoogleSignInApi}.
 */
public class MainActivity extends ReactActivity implements AdapterView.OnItemClickListener, DefaultHardwareBackBtnHandler {
    TextView socketText;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.0.2.2:4001");
        } catch (URISyntaxException e) {}
    }

    private static final Class[] CLASSES = new Class[]{
            SignInActivity.class,
            CommunicationActivity.class
    };

    private static final int[] DESCRIPTION_IDS = new int[] {
            R.string.desc_sign_in_activity,
            R.string.start_communication_activity,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSocket.connect();
        mSocket.on("ActiveStep", onNewMessage);
        setContentView(R.layout.activity_chooser);

        // Set up ListView and Adapter
        ListView listView = findViewById(R.id.list_view);

        Button mButton = findViewById(R.id.button);
        socketText = findViewById(R.id.socketText);
        mButton.setOnClickListener(v -> {
            Fragment reactNativeFragment = new ReactFragment.Builder()
                    .setComponentName("myreactnativeapp")
                    .setLaunchOptions(getLaunchOptions("message envoyé depuis android"))
                    .build();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.reactNativeFragment, reactNativeFragment)
                    .commit();
        });

        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
        adapter.setDescriptionIds(DESCRIPTION_IDS);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    private Bundle getLaunchOptions(String message) {
        Bundle initialProperties = new Bundle();
        initialProperties.putString("message", message);
        return initialProperties;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Class clicked = CLASSES[position];
        startActivity(new Intent(this, clicked));
    }

    @Override
    public void invokeDefaultOnBackPressed() {

    }

    public static class MyArrayAdapter extends ArrayAdapter<Class> {

        private Context mContext;
        private Class[] mClasses;
        private int[] mDescriptionIds;

        public MyArrayAdapter(Context context, int resource, Class[] objects) {
            super(context, resource, objects);

            mContext = context;
            mClasses = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(mClasses[position].getSimpleName());
            ((TextView) view.findViewById(android.R.id.text2)).setText(mDescriptionIds[position]);

            return view;
        }

        public void setDescriptionIds(int[] descriptionIds) {
            mDescriptionIds = descriptionIds;
        }
    }

    private Emitter.Listener onNewMessage = args -> this.runOnUiThread((Runnable) () -> {
        Object[] data = args;
        int message;
        message = (int) args[0];
        Log.i("what is it", args.getClass().getName());

        // add the message to view
        addMessage(message);
    });

    private void addMessage(int message) {
        socketText.setText(String.valueOf(message));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected Bundle getLaunchOptions() {
                Bundle initialProperties = new Bundle();
                ArrayList<String> imageList = new ArrayList<>(Arrays.asList(
                        "http://foo.com/bar1.png",
                        "http://foo.com/bar2.png"
                ));
                initialProperties.putStringArrayList("images", imageList);
                return initialProperties;
            }
        };
    }
}