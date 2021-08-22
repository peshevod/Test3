package com.example.test3;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.test3.data.Result;
import com.example.test3.data.model.LoggedInUser;
import com.example.test3.ui.home.HomeFragment;
import com.example.test3.ui.home.HomeViewModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLS13 extends AsyncTask<String,Void,Boolean> {

    final String TAG = "TLS13";
    private String server_string;
    private int server_port = 443;
    private MainActivity main;
    private String s;
    boolean err=false;
    public MutableLiveData<Boolean> connected;
    public boolean ready;
    HomeViewModel homeViewModel;
    private HurlStack stack;
    private RequestQueue queue;
    public String basic=null;
    static final int DISCONNECT=0;
    static final int CONNECT=1;
    static final int LOGIN=2;
    public String cmd;

    public TLS13(MainActivity main, HomeViewModel homeViewModel, String server_string, int server_port)
    {
        this.server_string=server_string;
        this.server_port=server_port;
        this.main=main;
        this.homeViewModel=homeViewModel;
        s="https://"+server_string+":"+server_port+"/";
    }

    public boolean CreateConnection(){

        try {
           main.ctx = SSLContext.getInstance("TLSv1.3");
           main.ctx.init(null, null, null);
           main.factory = new MySSLSocketFactory(main.ctx);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        Log.i(TAG, " MySSLSocketFactory successfully created");
        stack = new HurlStack(null, main.factory);
        queue = Volley.newRequestQueue(main,stack);
        if(queue!=null)
        {
            Log.i(TAG, " Request Queue successfully created");
            return true;
        }
        else
        {
            Log.e(TAG, " Failure to create Request Queue");
            return false;
        }
    }

    public boolean testSendRequest(){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, s+"monitor",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,"Response is: "+ response);
                        homeViewModel.setConnected(true);
                     }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode!=401)
                {
                    Log.e(TAG,"That didn't work! "+error.networkResponse.statusCode);
                    homeViewModel.setConnected(false);
                }
                else {
                    List<Header> allheaders=error.networkResponse.allHeaders;
                    for(Header h:allheaders)
                    {
                        Log.i(TAG,"Header "+h.getName()+" : "+h.getValue());
                        if(h.getName().equalsIgnoreCase("WWW-Authenticate"))
                        {
                            if(h.getValue().contains("Basic")) main.login_state=MainActivity.BASIC_LOGIN_REQUIRED;
                        }
                    }
                    String data0="";
                    for(int i=0;i<error.networkResponse.data.length;i++) data0+=" "+Integer.toHexString(error.networkResponse.data[i]);
                    Log.i(TAG, "Content: " + data0);
                    homeViewModel.setConnected(true);
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> head = new HashMap<String, String>();
                head.put("Host","mm304.asuscomm.com");
                head.put("Accept", "text/html");
                return head;
            }
        };
        Log.i(TAG,"stringRequest ready");
        // Add the request to the RequestQueue.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
        Log.i(TAG,"stringRequest added");
        return true;
    }

    public boolean test()
    {
        if(!homeViewModel.getConnected().getValue()) {
            if (!CreateConnection())
            {
                return false;
            }
        }
        if(!testSendRequest())
        {
            return false;
        }
        Log.i("TLS13", "test return true");
        return true;
    }

    public Boolean disconnect()
    {
//        queue.cancelAll(TAG);
        return false;
    }

    public boolean login(String username, String password)
    {
        Log.i(TAG, "Trying to login "+username+":"+password);
        String s=username+":"+password;
        try {
            basic=Base64.encodeToString(s.getBytes("ASCII"),Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, s+"monitor",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,"Response is: "+ response);
                        main.login_state=MainActivity.LOGGED_IN;
                        main.loginViewModel.setResult(new Result.Success<>(new LoggedInUser("0",response)));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                    Log.e(TAG,"That didn't work! "+error.networkResponse.statusCode);
                    main.login_state=MainActivity.NOT_LOGGED_IN;
                    main.loginViewModel.setResult(new Result.Error(new Exception("Login Error "+error.networkResponse.statusCode)));
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> head = new HashMap<String, String>();
                head.put("Host","mm304.asuscomm.com");
                head.put("Accept", "text/html");
                head.put("Authorization", "Basic "+basic);
                return head;
            }
        };
        // Add the request to the RequestQueue.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
        Log.i(TAG,"stringRequest with credentials added");
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result){
        if(cmd.equalsIgnoreCase("CONNECT")) Log.i("TLS13", "set connected");
    }

    @Override
    protected Boolean doInBackground(String... arg){
        cmd=arg[0];
        Log.i(TAG, "cmd="+arg[0]);
        if(arg[0].equalsIgnoreCase("CONNECT")) return test();
        else if(arg[0].equalsIgnoreCase("DISCONNECT"))  return disconnect();
        else if(arg[0].equalsIgnoreCase("LOGIN"))
        {
            Log.i(TAG, "tls enter");
            return login(arg[1],arg[2]);
        }
        return false;
    }
}
