package com.example.test3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewSettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences sharedPreferences;
    PreferenceScreen preferenceScreen;
    Set<String> availableServers;
    ListPreference listPreference;

    void createServersMenu()
    {
        preferenceScreen.removeAll();
        availableServers=sharedPreferences.getStringSet("available_servers", new HashSet<String>());
        Log.i("TLS13","CreateMenu AvailableServers="+availableServers.toString());
        if(!availableServers.isEmpty())
        {
            for(String s:availableServers) {
                Preference pref = new Preference(getActivity());
                pref.setKey(s + "@server_settings_fragment");
                pref.setTitle(s);
                pref.setSummary("https://"+sharedPreferences.getString(s+"@server_url","")+":"+sharedPreferences.getString(s+"@server_port",""));
                pref.setFragment("com.example.test3.ServerSettingsFragment");
                preferenceScreen.addPreference(pref);
            }

            listPreference = new ListPreference(getActivity());
            listPreference.setKey("selected_server");
            CharSequence[] cs = new CharSequence[availableServers.size()];
            int i = 0;
            for (String s : availableServers) cs[i++] = s;
            listPreference.setEntries(cs);
            listPreference.setEntryValues(cs);
            listPreference.setTitle("Select Server");
            String selectedServer=sharedPreferences.getString("selected_server","");
            Log.i("TLS13","selected_server="+selectedServer);
            listPreference.setValue(selectedServer);
            listPreference.setSummary(selectedServer);
            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    listPreference.setSummary(newValue.toString());
                    listPreference.setValue(newValue.toString());
                    SharedPreferences.Editor ed=sharedPreferences.edit();
                    ed.putString("selected_server",newValue.toString());
                    ed.commit();
                    return false;
                }
            });
            preferenceScreen.addPreference(listPreference);
        }
        Preference pref=new Preference(getActivity());
        pref.setKey("server_settings_fragment");
        pref.setTitle("New Server");
        pref.setFragment("com.example.test3.ServerSettingsFragment");
        preferenceScreen.addPreference(pref);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        preferenceScreen=getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(preferenceScreen);
        sharedPreferences=getPreferenceManager().getSharedPreferences();
//        createServersMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("TLS13","Resume available_servers="+sharedPreferences.getStringSet("available_servers",new HashSet<String>()).toString());
        createServersMenu();
    }
}