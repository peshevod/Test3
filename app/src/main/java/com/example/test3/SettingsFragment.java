package com.example.test3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends PreferenceFragmentCompat {

    ListPreference list;
    PreferenceCategory server;
    PreferenceScreen screen;
    SharedPreferences sharedPreferences;
    String newServer,newServerParent;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    Set<String> availableServers;

    private void createListeners(PreferenceCategory server) {
        server.findPreference("server_name").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                newServer = (String) newValue;
                String oldServer = server.getKey();
                server.setKey(newServer);
                server.setTitle(newServer);
                newServerParent = (String) preference.getParent().getKey();
                Log.i("TLS13", "New server of " + preference.getKey() + "=" + (CharSequence) newValue + " Parent=" + newServerParent);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                availableServers.remove(oldServer);
                editor.remove(oldServer + "@server_url");
                editor.remove(oldServer + "@server_port");
                availableServers.add(newServer);
                editor.putStringSet("AvailableServers", availableServers);
                editor.commit();
                Log.i("TLS13","AvailServers="+availableServers.toString());
                return true;
            }
        });
        server.findPreference("server_url").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String parent = server.getKey();
                Log.i("TLS13", "New urlr of " + preference.getKey() + "=" + (String) newValue + " Parent=" + parent);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(parent + "@server_url", (String) newValue);
                editor.commit();
                return true;
            }
        });
        server.findPreference("server_port").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String parent = server.getKey();
                Log.i("TLS13", "New port of " + preference.getKey() + "=" + (int) newValue + " Parent=" + parent);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(parent + "@server_port", (int) newValue);
                editor.commit();
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//    public void onCreate(Bundle savedInstanceState) {
//        setPreferencesFromResource(R.xml.root_preferences, rootKey);
//        super.onCreate(savedInstanceState);
//        super.onCreate(savedInstanceState);
        PreferenceManager prefManager = getPreferenceManager();
        sharedPreferences=prefManager.getSharedPreferences();
        String currentServer=sharedPreferences.getString("SelectedServer","");
        availableServers=sharedPreferences.getStringSet("AvailableServers",new HashSet<String>());
        setPreferencesFromResource(R.xml.server, rootKey);
        server=(PreferenceCategory)findPreference("NewServer");
        //        screen = (PreferenceScreen) findPreference("pref_server_settings");
        server.setKey("Add Server");
        server.setTitle("Add Server");
        createListeners(server);
        if(!availableServers.isEmpty()) {
        {
            for(String serv:availableServers)
            {

                addPreferencesFromResource(R.xml.server);
                server = (PreferenceCategory) findPreference("NewServer");
                server.setKey(serv);
                server.setTitle(serv);
            }
        }
//        PreferenceScreen prefScreen=prefManager.createPreferenceScreen (getActivity());
         addPreferencesFromResource(R.xml.empty);
//        PreferenceCategory targetCategory = (PreferenceCategory) findPreference("servers");
            list = (ListPreference) findPreference("SelectedServer");
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    addPreferencesFromResource(R.xml.server);
                    server = (PreferenceCategory) findPreference("NewServer");
                    server.setKey("Add Server");
                    server.setTitle("Add Server");
                    return true;
                }
            });
//        if (list == null) Log.i("TLS13", "list is null");

//        list=new ListPreference(getActivity());
//        CharSequence[] entries = new CharSequence[]{"server1", "server2", "Add new server"};
//        CharSequence[] values = new CharSequence[]{"1", "2", "3"};
//        list.setEntries(entries);
//        list.setDefaultValue("1");
//        list.setEntryValues(values);
            list.setTitle("Select/Add Server");
            list.setPersistent(true);
            list.setSummary("Now selected - " + list.getEntry());
            list.setDialogTitle("Dialog Message");
        }
//        targetCategory.addPreference(list);
//        prefScreen.addPreference(list);
//        prefManager.setPreferences(prefScreen);

//        prefScreen.setEnabled(true);
//        prefScreen.setVisible(true);

//        targetCategory.addPreference(list);
    }

/*    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.i("TLS13","rootKey="+rootKey);
    }*/

/*    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        list.setEnabled(true);
        list.setVisible(true);
        View ListView
    }*/
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                (listener=new SharedPreferences.OnSharedPreferenceChangeListener(){

                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

                        if(!s.equals("AvailableServers")) Log.i("TLS13","sharedPreference changed "+s+" val="+sharedPreferences.getString(s,""));
                        else Log.i("TLS13","sharedPreference changed "+s+" val="+sharedPreferences.getStringSet(s,null).toString());
                    }
                })
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

}