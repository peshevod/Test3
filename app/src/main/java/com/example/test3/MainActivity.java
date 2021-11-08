package com.example.test3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ProgressBar;

import com.example.test3.databinding.NavHeaderMainBinding;
import com.example.test3.ui.home.HomeViewModel;
import com.example.test3.ui.login.LoginViewModel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.test3.databinding.ActivityMainBinding;
import com.google.firebase.messaging.FirebaseMessaging;


import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{

    public SHConnectionService shConnectionService;
    boolean mBound = false;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavHeaderMainBinding navbind;
    public boolean connected;
    SharedPreferences sharedPreferences;
    private ProgressBar spinner;
    public SSLContext ctx=null;
    public MainActivity main;
     final String TAG="TLS13";
    public HomeViewModel homeViewModel;
    public LoginViewModel loginViewModel;
    public final static int NOT_CONNECTED=0;
    public final static int CONNECT_REQUIRED=1;
    public final static int BASIC_LOGIN_REQUIRED=2;
    public final static int LOGGED_IN=3;

    public int login_state=NOT_CONNECTED;
    ExecutorService pool;

    public MainActivity()
    {
        super();
        main=this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        connected=false;
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.i(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.i(TAG,"token "+token);
                    }
                });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener (
                new NavController.OnDestinationChangedListener() {
                    @Override
                    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @org.jetbrains.annotations.Nullable Bundle arguments) {
                        switch (destination.getId()) {
                            case R.id.nav_home:
                                break;
                        }
                    }
        });

/*        SharedPreferences.Editor ed=sharedPreferences.edit();
        ed.clear();
        ed.commit();*/
        SharedPreferences.Editor ed2=sharedPreferences.edit();
        ed2.remove("last_user@mm304.asuscomm.com");
        ed2.remove("ilya@mm304.asuscomm.com");
        ed2.commit();

        String selectedServer=sharedPreferences.getString("selected_server","");
        Set<String> availableServers=sharedPreferences.getStringSet("available_servers",new HashSet<String>());
        Log.i("TLS13","Begin: AvailServers="+availableServers.toString()+" SelectedServer="+selectedServer);
        if(availableServers.isEmpty() || !availableServers.contains(selectedServer))
        {
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("selected_server","");
            selectedServer="";
            editor.commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            NavController nc=Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            switch(nc.getCurrentDestination().getId()) {
                case R.id.nav_home:
                    nc.navigate(R.id.action_nav_home_to_newSettingsFragment);
                    break;
                case R.id.nav_gallery:
                    nc.navigate(R.id.action_nav_gallery_to_newSettingsFragment);
                    break;
                case R.id.nav_slideshow:
                    nc.navigate(R.id.action_nav_slideshow_to_newSettingsFragment);
                    break;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SHConnectionService.SHConnectionIBinder binder = (SHConnectionService.SHConnectionIBinder) service;
            shConnectionService = binder.getService();
            shConnectionService.main=MainActivity.this;
            Log.i(TAG+ "Service Connection","Thread id="+Thread.currentThread().getId());
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    public void onStart() {
        super.onStart();
//        Intent asyncIntent = new Intent(this, MyAsyncConnectionService.class);
//        bindService(asyncIntent, serviceAsyncConnection, Context.BIND_AUTO_CREATE);
        Intent Intent = new Intent(this, SHConnectionService.class);
        bindService(Intent, serviceConnection, Context.BIND_AUTO_CREATE);
        login_state=MainActivity.CONNECT_REQUIRED;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        mBound = false;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);

        Log.i("TLS13","pref.getKey="+pref.getKey());
        if(pref.getKey().equals("server_select_fragment"))
        {
            navController.navigate(R.id.action_newSettingsFragment_to_serverSelectFragment);
        }
        else
        {
            String[] fields=pref.getKey().split("@");
            Log.i("TLS13","prepare to navigate"+pref.getKey());
            if(fields.length!=2 && pref.getKey().equals("server_settings_fragment"))
            {
                navController.navigate(R.id.action_newSettingsFragment_to_serverSettingsFragment);
            }
            else
            {
                if(fields[1].equals("server_settings_fragment"))
                {
                    Bundle bundle=new Bundle();
                    bundle.putString("server_name",fields[0]);
                    navController.navigate(R.id.action_newSettingsFragment_to_serverSettingsFragment,bundle);
                }
            }
        }
        return true;
    }
}

