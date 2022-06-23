package com.example.test3.data;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.example.test3.R;
import com.example.test3.SHConnection;
import com.example.test3.SHConnectionService;
import com.example.test3.data.Result.Success;
import com.example.test3.data.model.LoggedInUser;
import com.example.test3.ui.login.LoggedInUserView;
import com.example.test3.ui.login.LoginResult;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private SHConnectionService service;

    public LoginDataSource(SHConnectionService service)
    {
        this.service=service;
    }

    public void login(String username, String password)
    {
        Log.i("TLS13","Datasource login");
        service.login(new SHConnection(service) {
            @Override
            public void onLoginSuccess(String welcome) {
                service.result.postValue(new Success<LoggedInUser>(new LoggedInUser(username, welcome)));
            }

            @Override
            public void onLoginFailure(Exception error) {
                service.result.postValue(new Result.Error(error));
            }
        },username,password);
    }

    public void logout() {
        // TODO: revoke authentication
    }
}