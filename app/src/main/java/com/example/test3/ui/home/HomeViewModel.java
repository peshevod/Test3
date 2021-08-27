package com.example.test3.ui.home;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.ProgressBar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> servernameText;
    private MutableLiveData<String> serverstringText;
    private MutableLiveData<Boolean> connected;
    private MutableLiveData<String> buttonText;
//    private MutableLiveData<ProgressBar> progressBar1;

    public HomeViewModel() {
        servernameText = new MutableLiveData<>();
        serverstringText = new MutableLiveData<>();
        buttonText = new MutableLiveData<>();
        connected = new MutableLiveData<>();
        connected.setValue(false);
//        progressBar1 = new MutableLiveData<>();
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
    public LiveData<String> getButtonText() { return buttonText; };
    public void setButtonText(String s) { buttonText.setValue(s); }
    public MutableLiveData<Boolean> getConnected() { return connected; }
    public void setConnected(Boolean b) { connected.setValue(b); }
    public void postConnected(Boolean b) { connected.postValue(b); }
 //   public void setProgressBar1(boolean sw) {
 //       if(sw) progressBar1.getValue().setVisibility(View.VISIBLE);
 //       else progressBar1.getValue().setVisibility(View.GONE);
 //   }
}