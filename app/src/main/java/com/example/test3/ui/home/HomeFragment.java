package com.example.test3.ui.home;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.test3.R;
import com.example.test3.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView textNameView = binding.serverName;
        final TextView textStringView = binding.serverString;

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity() /* Activity context */);

        String s="https://"+ sharedPreferences.getString("server_url", "mm304.asuscomm.com")+":"+
                sharedPreferences.getString("server_port", "51443");
        String my_server=sharedPreferences.getString("server_name", "YOUR SERVER");
        homeViewModel.setStringText(s);
        homeViewModel.setNameText(my_server);

        String nc= " - not connected";
        Spannable myServer = new SpannableString(my_server);
        Spannable conn = new SpannableString(nc);
        myServer.setSpan(new ForegroundColorSpan(Color.WHITE), 0, myServer.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        conn.setSpan(new ForegroundColorSpan(Color.RED), 0, conn.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//        Spannable ss= (Spannable) android.text.TextUtils.concat(myServer,conn);
//        getActivity().getActionBar().setTitle(android.text.TextUtils.concat(myServer,conn));
        getActivity().setTitle(myServer);
//        getActivity().setTitle(ss);

/*        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener()
        {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        if (key.equals("server_name")) {
    //                        homeViewModel.setText(sharedPreferences.getString("server_name", "YOUR SERVER"));
                            homeViewModel.setText("My Text");
                        }
                    }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);*/

//        mtext.setValue(sharedPreferences.getString("server_name", "YOUR SERVER"));

        homeViewModel.getNameText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textNameView.setText(s);
            }
        });
        homeViewModel.getStringText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textStringView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity() /* Activity context */);

        String s="https://"+ sharedPreferences.getString("server_url", "mm304.asuscomm.com")+":"+
                sharedPreferences.getString("server_port", "51443");
        String my_server=sharedPreferences.getString("server_name", "YOUR SERVER");
//        homeViewModel.setStringText(s);
//        homeViewModel.setNameText(my_server);

        String nc= " - not connected";
        Spannable myServer = new SpannableString(my_server);
        Spannable conn = new SpannableString(" - not connected");
        myServer.setSpan(new ForegroundColorSpan(Color.WHITE), 0, my_server.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        conn.setSpan(new ForegroundColorSpan(Color.RED), 0, conn.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//        Spannable ss= (Spannable) android.text.TextUtils.concat(myServer,conn);
//        getActivity().getActionBar().setTitle(android.text.TextUtils.concat(myServer,conn));
        getActivity().setTitle(my_server + nc);
    }
}