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
//    private SHConnectionService service;
    private LoggedInUser loggedInUser;
    private Exception error;
    private boolean success=false;
/*    public LoginDataSource(SHConnectionService service)
    {
        this.service=service;
    }*/

    public Result<LoggedInUser> login(SHConnectionService service, String username, String password) {

        Log.i("TLS13","Datasource login");
        service.login(new SHConnection(service) {
            @Override
            public void onLoginSuccess(String welcome) {
                loggedInUser = new LoggedInUser(username, welcome);
                success=true;
            }

            @Override
            public void onLoginFailure(Exception error1) {
                error=error1;
                success=false;
            }
        },username,password);
        if(success) return new Result.Success<>(loggedInUser);
        else return new Result.Error(error);
    }

    public void logout() {
        // TODO: revoke authentication
    }
}