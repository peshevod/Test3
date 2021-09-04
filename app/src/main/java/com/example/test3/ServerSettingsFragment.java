package com.example.test3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import java.util.HashSet;
import java.util.Set;

public class ServerSettingsFragment extends PreferenceFragmentCompat {

    PreferenceCategory server;
    SharedPreferences sharedPreferences;
    Set<String> availableServers;
    String oldServer;


    private void createListeners(PreferenceCategory server) {
        server.findPreference(oldServer!=null ? oldServer+"@server_name" : "server_name").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newServer = (String) newValue;
                server.setKey(newServer+"@server_name");
                server.setTitle(newServer);
                preference.setSummary(newServer);
                Log.i("TLS13", "New server of " + preference.getKey() + "=" + (CharSequence) newValue);
                String serverURL=sharedPreferences.getString(oldServer!=null ? oldServer + "@server_url" : "server_url","");
                String serverPort=sharedPreferences.getString(oldServer!=null ? oldServer + "@server_url" : "server_url","0");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(oldServer!=null)
                {
                    availableServers.remove(oldServer);
                }
                editor.remove(oldServer!=null ? oldServer + "@server_name" : "server_name");
                editor.remove(oldServer!=null ? oldServer + "@server_url" : "server_url");
                editor.remove(oldServer!=null ? oldServer + "@server_port" : "server_port");
                availableServers.add(newServer);
                editor.putStringSet("available_servers", availableServers);
                editor.putString(newServer+"@server_name",newServer);
                editor.putString(newServer+"@server_url",serverURL);
                editor.putString(newServer+"@server_port",serverPort);
                editor.commit();
                oldServer=newServer;
                Log.i("TLS13","change AvailServers="+availableServers.toString());
                return true;
            }
        });
        server.findPreference(oldServer!=null ? oldServer+"@server_url" : "server_url").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.i("TLS13", "New urlr of " + preference.getKey() + "=" + newValue.toString());
                preference.setSummary((String) newValue);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(oldServer!=null ? oldServer + "@server_url" : "server_url", (String) newValue);
                editor.commit();
                return true;
            }
        });
        if(oldServer!=null) server.findPreference(oldServer!=null ? oldServer+"@server_port" : "server_port").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String parent = server.getKey();
                Log.i("TLS13", "New port of " + preference.getKey() + "=" + newValue.toString());
                preference.setSummary(newValue.toString());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(oldServer!=null ? oldServer + "@server_port" : "server_port", newValue.toString());
                editor.commit();
                return true;
            }
        });
        if(oldServer!=null) server.findPreference("delete_set").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.i("TLS13","check box new val="+newValue.toString());
                if((boolean)newValue)
                {
                    String selectedServer=sharedPreferences.getString("selected_server","");
                    SharedPreferences.Editor ed=sharedPreferences.edit();
                    availableServers.remove(oldServer);
                    ed.remove(oldServer+"@server_name");
                    ed.remove(oldServer+"@server_url");
                    ed.remove(oldServer+"@server_port");
                    ed.putStringSet("available_servers",availableServers);
                    if(selectedServer.equals(oldServer)) ed.putString("selected_server","");
                    ed.commit();
                }
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        setPreferencesFromResource(R.xml.server, rootKey);
        sharedPreferences=getPreferenceManager().getSharedPreferences();
        availableServers=sharedPreferences.getStringSet("available_servers", new HashSet<String>());
        Bundle bundle=getArguments();
        oldServer=bundle.getString("server_name");
        Log.i("TLS13","oldServer="+oldServer);

        PreferenceScreen preferenceScreen=getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(preferenceScreen);

        PreferenceCategory preferenceCategory=new PreferenceCategory(getActivity());
        preferenceCategory.setKey("server_settings");
        preferenceCategory.setTitle(oldServer!=null ? oldServer: "New Server");
        preferenceScreen.addPreference(preferenceCategory);

        EditTextPreference serverName=new EditTextPreference(getActivity());
        serverName.setKey(oldServer!=null ? oldServer+"@server_name" : "server_name");
        serverName.setTitle("Server Name");
//        serverName.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
//        serverName.setDefaultValue("New Server");
//        serverName.setText(oldServer!=null ? oldServer : "New Server");
        serverName.setSummary(oldServer!=null ? oldServer : "New Server");
        Log.i("TLS13","serverName="+serverName.toString());
        Log.i("TLS13","categ="+preferenceCategory.toString());
        preferenceCategory.addPreference(serverName);

        EditTextPreference serverURL=new EditTextPreference(getActivity());
        serverURL.setKey(oldServer!=null ? oldServer+"@server_url" : "server_url");
        serverURL.setTitle("Server URL");
        serverURL.setDefaultValue("");
//        serverURL.setText(oldServer!=null ? oldServer : "Server URL");
        serverURL.setSummary(oldServer!=null ? sharedPreferences.getString(oldServer+"@server_url","" ) : "Not set");
//        serverURL.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        preferenceCategory.addPreference(serverURL);

        EditTextPreference serverPort=new EditTextPreference(getActivity());
        serverPort.setKey(oldServer!=null ? oldServer+"@server_port" : "server_port");
        serverPort.setTitle("Server Port");
        serverPort.setDefaultValue("0");
//        serverURL.setText(oldServer!=null ? oldServer : "0");
        serverPort.setSummary(oldServer!=null ? sharedPreferences.getString(oldServer+"@server_port","") : "0");
//        serverPort.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        preferenceCategory.addPreference(serverPort);

        CheckBoxPreference checkBoxPreference;
        if(oldServer!=null)
        {
            checkBoxPreference=new CheckBoxPreference(getActivity());
            checkBoxPreference.setKey("delete_set");
            checkBoxPreference.setTitle("Delete server");
            preferenceCategory.addPreference(checkBoxPreference);
        }

        createListeners(preferenceCategory);
    }
}