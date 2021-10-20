package com.example.test3.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.example.test3.MainActivity;
import com.example.test3.MyConnection;
import com.example.test3.R;
import com.example.test3.TLS13;
import com.example.test3.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    public HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private Button btn;
//    public MutableLiveData<Boolean> connected = new MutableLiveData<>();
    private boolean connected;
    private SharedPreferences sharedPreferences;
    MainActivity main;
    public boolean goToLogin=false;
    String hostname;
    String my_server;
    int port;


    void setServerTitle()
    {
        String conn= homeViewModel.getConnected().getValue() ? " - connected" : " - not connected";
        Spannable ss = new SpannableString(my_server+conn);
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 0, my_server.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(new ForegroundColorSpan( homeViewModel.getConnected().getValue()  ? Color.GREEN : Color.RED), my_server.length(), conn.length()+my_server.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        main.getSupportActionBar().setTitle(ss);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView textNameView = binding.serverName;
        final TextView textStringView = binding.serverString;
        btn = binding.button;
        final ProgressBar progressBar1 = binding.progressBar1;


        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity()/* Activity context */);

        my_server=sharedPreferences.getString("selected_server", "Server not selected");
        Log.i("TLS13","My Server="+my_server);
        hostname=sharedPreferences.getString(my_server+"@server_url", "mm304.asuscomm.com");
        port=Integer.parseInt(sharedPreferences.getString(my_server+"@server_port", "51443"));
//        port=51443;
        String s="https://"+ hostname+":"+port;
        homeViewModel.setStringText(s);
        homeViewModel.setNameText(my_server);
//        homeViewModel.setConnected(false);

/*        Spannable myServer = new SpannableString(my_server);
        Spannable conn = new SpannableString(nc);
        myServer.setSpan(new ForegroundColorSpan(Color.WHITE), 0, myServer.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        conn.setSpan(new ForegroundColorSpan(Color.RED), 0, conn.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        String nc= " - not connected";
        Spannable myServer = new SpannableString(my_server);
        Spannable conn = new SpannableString(" - not connected");
        myServer.setSpan(new ForegroundColorSpan(Color.WHITE), 0, my_server.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        conn.setSpan(new ForegroundColorSpan(Color.RED), 0, conn.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        String my_server=sharedPreferences.getString("server_name", "YOUR SERVER");
        String conn= connected ? " - connected" : " - not connected";
        Spannable ss = new SpannableString(my_server+conn);
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 0, my_server.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(new ForegroundColorSpan( connected ? Color.GREEN : Color.RED), my_server.length(), conn.length()+my_server.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        getSupportActionBar().setTitle(ss);*/
//        Spannable ss= (Spannable) android.text.TextUtils.concat(myServer,conn);
//        getActivity().getActionBar().setTitle(android.text.TextUtils.concat(myServer,conn));
//        getActivity().setTitle(myServer);
//        getActivity().setTitle(ss);
//        spinner = (ProgressBar)findViewById(R.id.progressBar1);
//        spinner.setVisibility(View.GONE);
        progressBar1.setVisibility(View.GONE);
//        connected=((MainActivity)getActivity()).connected;
//        btn.setText(connected ? "Disconnect":"Connect");
//        ((MainActivity)getActivity()).connected.setValue(false);
//        connected.setValue(((MainActivity)getActivity()).connected.getValue());
/*        connected.observe(getViewLifecycleOwner(),new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean.booleanValue())
                    btn.setText("Disconnect");
                else
                btn.setText("Connect");                Log.i("TLS13","connected changes " + " aBoolean="+aBoolean);
                progressBar1.setVisibility(View.GONE);
            }
        });*/

        btn.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
//                    homeViewModel.setProgressBar1(true);
                    progressBar1.setVisibility(View.VISIBLE);
                    if(!homeViewModel.getConnected().getValue()) {
                        goToLogin=true;
//                        main.connect(hostname,port);
                        main.asyncConnection.asyncConnect(hostname, port);
                    }
                    else
                    {
//                        main.disconnect();
                          main.asyncConnection.asyncDisconnect();
                    }

                    Log.i("TLS13","connected settings");
//                    btn.setText(connected ? "Disconnect":"Connect");

//                    homeViewModel.setProgressBar1(false);
                }
        });

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
        homeViewModel.getButtonText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        homeViewModel.getConnected().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean b) {
                if(!b) main.login_state=MainActivity.CONNECT_REQUIRED;
                Log.i("TLS13","Connected changes "+b+" login_state="+main.login_state);
                btn.setText(b.booleanValue() ? "Disconnect" : "Connect");
                setServerTitle();
                progressBar1.setVisibility(View.GONE);
                if(b && (main.login_state==MainActivity.BASIC_LOGIN_REQUIRED))
                {
                    Log.i("TLS13","Navigate to login");
                    Navigation.findNavController(main, R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_home_to_login_fragment);
                }

//                btn.refreshDrawableState();
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
/*   @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
       super.onViewStateRestored(savedInstanceState);
       main.tls.disconnect();
       homeViewModel.setConnected(false);
   }*/
/*        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity() /* Activity context */
/*    );

        String s="https://"+ sharedPreferences.getString("server_url", "mm304.asuscomm.com")+":"+
                sharedPreferences.getString("server_port", "51443");
        String my_server=sharedPreferences.getString("server_name", "YOUR SERVER");
//        homeViewModel.setStringText(s);
//        homeViewModel.setNameText(my_server);

//        Spannable ss= (Spannable) android.text.TextUtils.concat(myServer,conn);
//        getActivity().getActionBar().setTitle(android.text.TextUtils.concat(myServer,conn));
        setServerTitle();
        //        btn.setText(connected ? "Disconnect":"Connect");
    }*/

/*    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        main=(MainActivity)context;
        main.homeViewModel=homeViewModel;
//        setServerTitle();
    }*/


    /*   @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
           super.onViewStateRestored(savedInstanceState);
           main.tls.disconnect();
           homeViewModel.setConnected(false);
       }*/
/*        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity() /* Activity context */
/*    );

        String s="https://"+ sharedPreferences.getString("server_url", "mm304.asuscomm.com")+":"+
                sharedPreferences.getString("server_port", "51443");
        String my_server=sharedPreferences.getString("server_name", "YOUR SERVER");
//        homeViewModel.setStringText(s);
//        homeViewModel.setNameText(my_server);

//        Spannable ss= (Spannable) android.text.TextUtils.concat(myServer,conn);
//        getActivity().getActionBar().setTitle(android.text.TextUtils.concat(myServer,conn));
        setServerTitle();
        //        btn.setText(connected ? "Disconnect":"Connect");
    }*/
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        main=(MainActivity)getActivity();
        main.homeViewModel=homeViewModel;
        setServerTitle();
    }
}