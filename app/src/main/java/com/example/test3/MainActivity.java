package com.example.test3;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.test3.databinding.NavHeaderMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.test3.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavHeaderMainBinding navbind;
    public boolean connected;
    SharedPreferences sharedPreferences;
    private ProgressBar spinner;
    Button btn;

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
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        btn=findViewById(R.id.button);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener (
                new NavController.OnDestinationChangedListener() {
                    @Override
                    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @org.jetbrains.annotations.Nullable Bundle arguments) {
                        switch (destination.getId()) {
                            case R.id.nav_home:
                                setConnLabel();
                                break;
                        }
                    }
        });
        btn.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        spinner.setVisibility(View.VISIBLE);
                        if(!connected)
                        {
                            connect();
                        }
                    }

        });
    }

    public void setConnLabel()
    {
        String my_server=sharedPreferences.getString("server_name", "YOUR SERVER");
        String conn= connected ? " - connected" : " - not connected";
        Spannable ss = new SpannableString(my_server+conn);
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 0, my_server.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(new ForegroundColorSpan( connected ? Color.GREEN : Color.RED), my_server.length(), conn.length()+my_server.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        getSupportActionBar().setTitle(ss);
        btn.setText(connected ? "Disconnect" : "Connect");
    }

    public boolean connect()
    {
        URL url = null;
        URLConnection con=null;
        String s="https://"+ sharedPreferences.getString("server_url", "mm304.asuscomm.com")+":"+
                sharedPreferences.getString("server_port", "51443");
        try {
            url = new URL(s);
            con = url.openConnection();
            con.connect();
        } catch (MalformedURLException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        } catch (IOException e) {
            if(s!=null) return false;
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
        return true;
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
}

