package com.example.test3.ui.login;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.test3.MainActivity;
import com.example.test3.SHConnectionService;
import com.example.test3.data.LoginRepository;
import com.example.test3.data.model.LoggedInUser;
import com.example.test3.data.Result;
import com.example.test3.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    public LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }
    public LoginRepository getLoginRepository() { return loginRepository; }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public MutableLiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(SHConnectionService service, String username, String password, boolean remember) {
        // can be launched in a separate asynchronous job
        loginRepository.login(service, username, password, remember);
    }

    public void logout() {
        // can be launched in a separate asynchronous job
        loginRepository.logout();
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}