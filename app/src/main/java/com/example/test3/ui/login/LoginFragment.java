package com.example.test3.ui.login;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test3.MainActivity;
import com.example.test3.MyItemRecyclerViewAdapter;
import com.example.test3.data.LoginDataSource;
import com.example.test3.data.LoginRepository;
import com.example.test3.data.model.LoggedInUser;
import com.example.test3.databinding.FragmentLoginBinding;

import com.example.test3.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;
    public MainActivity main;
    CheckBox remember;
    EditText usernameEditText;
    EditText passwordEditText;
    ProgressBar loadingProgressBar;
    Button loginButton;
    private String TAG="TLS13 LoginFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private void setRemember()
    {
        if(main.sharedPreferences.contains("remember_credentials"))
        {
            remember.setChecked(true);
            String lastUser;
            String lastPassword;

/*            if((lastUser=sharedPreferences.getString("last_user@"+main.host.getHostName(),null))!=null)
            {
                String encrypted=sharedPreferences.getString(lastUser+"@"+main.host.getHostName(),null);
                if(encrypted!=null)
                {
                    lastPassword=decrypt(lastUser, main.host.getHostName(), encrypted);
                    if(lastPassword!=null)
                    {
                        usernameEditText.setText(lastUser);
                        passwordEditText.setText(lastPassword);
                    }
                }
            }*/
        }
        else remember.setChecked(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
//                .get(LoginViewModel.class);
        main=(MainActivity)(this.getActivity());
        loginViewModel = new LoginViewModel(LoginRepository.getInstance(new LoginDataSource(main.shConnectionService)));
//        main.loginViewModel=loginViewModel;
//        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        remember = binding.CheckBox;
        loginButton = binding.login;
        loadingProgressBar = binding.loading;
        loadingProgressBar.setVisibility(View.INVISIBLE);
        usernameEditText = binding.username;
        if(main.sharedPreferences.contains("last_user@"+main.shConnectionService.getHostname()))
            usernameEditText.setText(main.sharedPreferences.getString("last_user@"+main.shConnectionService.getHostname(),""));
        passwordEditText = binding.password;
        if(main.sharedPreferences.contains(usernameEditText.getText().toString()+"@"+main.shConnectionService.getHostname())) {
            passwordEditText.setText("::::::::");
            loginButton.setEnabled(true);
        }

        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                Log.i(TAG,"Stop progress bar");
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    main.shConnectionService.getSessions();
                    main.shConnectionService.requestCompleted.observeForever(new Observer<Boolean>() {
                        @Override
                        public void onChanged(@Nullable Boolean b) {
                            if(b)
                            {
                                Log.i(TAG,"Request completed");
                                Log.i(TAG,"Navigate to Devices");
                                Navigation.findNavController(main, R.id.nav_host_fragment_content_main).navigate(R.id.action_login_fragment_to_devicesFragment);
                                main.shConnectionService.requestCompleted.removeObserver(this);
                            } else Log.i(TAG,"Request started");
                        }
                    });
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(main.shConnectionService, usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), remember.isChecked());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                Log.i(TAG,"Start progress bar");
                loginViewModel.login(main.shConnectionService, usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(),remember.isChecked());
            }
        });

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               SharedPreferences.Editor ed=main.sharedPreferences.edit();
               if(b) ed.putBoolean("remember_credentials",true);
               else ed.remove("remember_credentials");
               ed.commit();
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        main=(MainActivity)context;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        setRemember();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 /*   public void onActivityCreated (Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }*/
}