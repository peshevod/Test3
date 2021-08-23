package com.example.test3;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.test3.databinding.NavHeaderMainBinding;
import com.example.test3.ui.home.HomeViewModel;
import com.example.test3.ui.login.LoginViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.test3.databinding.ActivityMainBinding;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ok2c.hc.android.http.AndroidHttpClientConnectionManagerBuilder;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavHeaderMainBinding navbind;
//    public MutableLiveData<Boolean> connected = new MutableLiveData<>();
    public boolean connected;
    SharedPreferences sharedPreferences;
    private ProgressBar spinner;
    public SSLContext ctx=null;
    public MainActivity main;
    public SSLSocketFactory factory=null;
    public TLS13 tls;
    final String TAG="TLS13";
    public HomeViewModel homeViewModel;
    public LoginViewModel loginViewModel;
    public final static int NOT_CONNECTED=0;
    public final static int NOT_LOGGED_IN=1;
    public final static int BASIC_LOGIN_REQUIRED=2;
    public final static int LOGGED_IN=3;

    public int login_state=NOT_CONNECTED;
    public HttpClientConnectionManager connMgr=null;
    public HttpClientConnection conn;


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
//        Button btn=findViewById(R.id.button);
//        spinner = (ProgressBar)findViewById(R.id.progressBar1);
//        spinner.setVisibility(View.GONE);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
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

        connMgr = AndroidHttpClientConnectionManagerBuilder.create()
                .setConnectionTimeToLive(1, TimeUnit.MINUTES)
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(5000)
                        .build())
                .build();

//        TLS13 tls=new TLS13(this, homeViewModel, sharedPreferences.getString("server_string", "mm304.asuscomm.com"), Integer.parseInt(sharedPreferences.getString("server_port", "51443")) );
//        loginViewModel.setTLS(tls);
//        Log.i("TLS13","TLS "+main.tls.toString());
//        Log.i(TAG,tls.toString());
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
/*        btn.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
//                    spinner.setVisibility(View.VISIBLE);
                    get
                    TLS13 tls=new TLS13(main, sharedPreferences.getString("server_string", "mm304.asuscomm.com"), Integer.parseInt(sharedPreferences.getString("server_port", "51443")) );
                    tls.execute();
//                    spinner.setVisibility(View.GONE);
                }
        });*/
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
                    nc.navigate(R.id.action_nav_home_to_settingsFragment);
                    break;
                case R.id.nav_gallery:
                    nc.navigate(R.id.action_nav_gallery_to_settingsFragment);
                    break;
                case R.id.nav_slideshow:
                    nc.navigate(R.id.action_nav_slideshow_to_settingsFragment);
                    break;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStart() {
        super.onStart();
    }
}

