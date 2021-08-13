package com.example.test3.ui.home;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> servernameText;
    private MutableLiveData<String> serverstringText;

    public HomeViewModel() {
        servernameText = new MutableLiveData<>();
        serverstringText = new MutableLiveData<>();
//        mText.setValue("This is home fragment");
    }

    public LiveData<String> getNameText() {
        return servernameText;
    }
    public LiveData<String> getStringText() {
        return serverstringText;
    }
    public void setNameText(String s)
    {
        servernameText.setValue(s);
    }
    public void setStringText(String s)
    {
        serverstringText.setValue(s);
    }
}