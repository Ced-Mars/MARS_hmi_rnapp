package com.example.myapplication;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ItemViewModel extends ViewModel {

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

}

