package com.example.test3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.util.HashSet;

public class TZFragment extends PreferenceFragmentCompat {

    SharedPreferences sharedPreferences;
    PreferenceScreen preferenceScreen;
    ListPreference listPreference;

    void createTZMenu() {
        preferenceScreen.removeAll();
        listPreference = new ListPreference(getActivity());
        String selectedTZ = sharedPreferences.getString("TZ", "");

        listPreference.setKey("TZ");
        CharSequence[] cs = new CharSequence[2];
        cs[0] = "Europe";
        cs[1] = "US";
        listPreference.setEntries(cs);
        listPreference.setEntryValues(cs);
        listPreference.setTitle("Select TZ");
        listPreference.setValue(selectedTZ);
        listPreference.setSummary(selectedTZ);
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                listPreference.setSummary(newValue.toString());
                listPreference.setValue(newValue.toString());
                SharedPreferences.Editor ed = sharedPreferences.edit();
                ed.putString("TZ", newValue.toString());
                ed.commit();
                return false;
            }
        });
        preferenceScreen.addPreference(listPreference);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        preferenceScreen=getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(preferenceScreen);
        sharedPreferences=getPreferenceManager().getSharedPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        createTZMenu();
    }
}