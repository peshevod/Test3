package com.example.test3.data;

import android.util.Log;

import com.example.test3.MainActivity;
import com.example.test3.TLS13;
import com.example.test3.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public boolean login(TLS13 tls, String username, String password) {

    Log.i("TLS13","Datasource login");
        try {
            // TODO: handle loggedInUser authentication
            String credentials[]=new String[3];
            credentials[0]="LOGIN";
            credentials[1]=username;
            credentials[2]=password;
            tls.execute(credentials);
/*            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");*/
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}