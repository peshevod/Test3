package com.example.test3.data;

import android.util.Log;

import androidx.preference.PreferenceManager;

import com.example.test3.MainActivity;
import com.example.test3.SHConnection;
import com.example.test3.SHConnectionService;
import com.example.test3.data.model.LoggedInUser;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private SHConnectionService service;
    private Result result;

    public LoginDataSource(SHConnectionService service)
    {
        this.service=service;
        result=null;
    }

    public Result login(String username, String password)
    {
        Log.i("TLS13","Datasource login");
        service.login(new SHConnection(service) {
            @Override
            public void onLoginSuccess(String welcome) {
                result = new Result.Success<LoggedInUser>(new LoggedInUser(username, welcome));
            }

            @Override
            public void onLoginFailure(Exception error) {
                result = new Result.Error(error);
            }
        },username,password);
        try {
            while (result == null) Thread.sleep(3000);
        } catch (InterruptedException e){
            result = new Result.Error(e);
        }
        Log.i("TLS13 LoginDataSouce","result="+result.toString());
        return result;
    }

    public void logout() {
        // TODO: revoke authentication
    }
}