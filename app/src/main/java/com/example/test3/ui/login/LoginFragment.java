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
import android.graphics.Color;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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
import com.example.test3.data.Result;
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

//    private LoginViewModel loginViewModel;
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
        Log.i(TAG,"Start OnViewCreated");
//        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
//                .get(LoginViewModel.class);
        main=(MainActivity)(this.getActivity());
        if(main.loginViewModel==null) main.loginViewModel = new LoginViewModel(LoginRepository.getInstance(new LoginDataSource(main.shConnectionService)));
//        main.loginViewModel=loginViewModel;
//        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        remember = binding.CheckBox;
        loginButton = binding.login;
        loadingProgressBar = binding.loading;
        loadingProgressBar.setEnabled(true);
        loadingProgressBar.setVisibility(View.INVISIBLE);
        usernameEditText = binding.username;
        passwordEditText = binding.password;
        Log.i(TAG,"main.shConnectionService="+main.shConnectionService);
        if(main.sharedPreferences.contains("last_user@"+main.shConnectionService.getHostname()))
            usernameEditText.setText(main.sharedPreferences.getString("last_user@"+main.shConnectionService.getHostname(),""));
        if(main.connection_state.getValue()==MainActivity.CONNECTED) {
            loginButton.setText("Sign In");
            passwordEditText.setVisibility(View.VISIBLE);
            remember.setVisibility(View.VISIBLE);
            if (main.sharedPreferences.contains(usernameEditText.getText().toString() + "@" + main.shConnectionService.getHostname())) {
                passwordEditText.setText("::::::::");
                loginButton.setEnabled(true);
            }
        } else if(main.connection_state.getValue()==MainActivity.LOGGED_IN)
        {
            passwordEditText.setVisibility(View.INVISIBLE);
            remember.setVisibility(View.INVISIBLE);
            loginButton.setText("Sign Out");
            Log.i(TAG, "loginResult="+main.loginViewModel.getLoginResult()==null ? "null":main.loginViewModel.getLoginResult().toString());
        }

        main.loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
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

        if(main.shConnectionService.result.getValue() instanceof Result.Error) Log.i(TAG, "result is Error");
        if(main.shConnectionService.result.getValue() instanceof Result.Success) Log.i(TAG, "result is Success");
        if(main.connection_state.getValue()!=MainActivity.LOGGED_IN) main.shConnectionService.result.observe(main, new Observer<Result>() {
            @Override
            public void onChanged(@Nullable Result result) {
                if (result == null) {
                    return;
                }
                Log.i(TAG,"Result changed "+result.toString());
                if (result instanceof Result.Error) {
                    main.loginViewModel.getLoginResult().postValue(new LoginResult(R.string.login_failed));
                    Log.i("TLS13 loginViewModel", "Login failed with " + ((Result.Error) result).getError().getMessage());
                }
                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    main.loginViewModel.getLoginResult().postValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                    if(remember.isChecked()) main.loginViewModel.getLoginRepository().storeCredentials(data);
                    main.connection_state.postValue(MainActivity.LOGGED_IN);
                }
            }
        });

        if(main.connection_state.getValue()!=MainActivity.LOGGED_IN) main.loginViewModel.getLoginResult().observe(main, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                Log.i(TAG,"LoginResult changed");
                if (loginResult == null) {
                    return;
                }
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                    Log.i(TAG,"Stop progress bar");
                    loadingProgressBar.setVisibility(View.GONE);
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    main.shConnectionService.getSessions();
                    main.shConnectionService.requestCompleted.observeForever(new Observer<Boolean>() {
                        @Override
                        public void onChanged(@Nullable Boolean b) {
                            if(b)
                            {
                                Log.i(TAG,"Stop progress bar");
                                loadingProgressBar.setVisibility(View.GONE);
                                Log.i(TAG,"Request completed");
                                if(main.shConnectionService.sessions!=null) {
                                    Log.i(TAG, "Navigate to Devices");
                                    Navigation.findNavController(main, R.id.nav_host_fragment_content_main).navigate(R.id.action_login_fragment_to_devicesFragment);
                                }
                                else
                                {
                                    if (getContext() != null && getContext().getApplicationContext() != null) {
                                        String s="Error getting data!!!";
                                        SpannableString sstring=new SpannableString(s);
                                        sstring.setSpan(new ForegroundColorSpan(Color.RED),0,s.length()-1, SpannableString.SPAN_MARK_POINT);
                                        sstring.setSpan(new RelativeSizeSpan(2f),0,s.length()-1, SpannableString.SPAN_MARK_POINT);
                                        Toast.makeText(getContext().getApplicationContext(), sstring, Toast.LENGTH_LONG).show();
                                        main.connection_state.postValue(MainActivity.CONNECTED);
                                        main.loginViewModel=null;
                                        main.shConnectionService.result.setValue(null);
                                        Navigation.findNavController(main, R.id.nav_host_fragment_content_main).navigate(R.id.action_login_fragment_self);
                                    }
                                }
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
                main.loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    main.loginViewModel.login(main.shConnectionService, usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), remember.isChecked());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(main.connection_state.getValue()!=MainActivity.LOGGED_IN)
                {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    Log.i(TAG,"Start progress bar");
                    main.loginViewModel.login(main.shConnectionService, usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(),remember.isChecked());
                } else
                {
                    main.loginViewModel.logout();
                    main.connection_state.postValue(MainActivity.CONNECTED);
                    main.loginViewModel=null;
                    main.shConnectionService.result.setValue(null);
                    Navigation.findNavController(main, R.id.nav_host_fragment_content_main).navigate(R.id.action_login_fragment_self);
                }
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
//        String welcome = getString(R.string.welcome) + model.getDisplayName();
        String welcome = "Welcome " + model.getDisplayName();
        // TODO : initiate successful logged in experience
        if (getContext() != null && getContext().getApplicationContext() != null) {
            SpannableString sstring=new SpannableString(welcome);
            sstring.setSpan(new ForegroundColorSpan(Color.BLACK),0,welcome.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        //    wstring.setSpan(new BackgroundColorSpan(Color.BLACK),0,welcome.length()-1, SpannableString.SPAN_MARK_POINT);
            sstring.setSpan(new RelativeSizeSpan(1.5f),0,welcome.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
            Toast toast=Toast.makeText(getContext().getApplicationContext(), sstring, Toast.LENGTH_LONG);
            toast.show();
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