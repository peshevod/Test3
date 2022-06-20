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
    SharedPreferences sharedPreferences;
    CheckBox remember;
    EditText usernameEditText;
    EditText passwordEditText;
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
        if(sharedPreferences.contains("remember_credentials"))
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
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        remember = binding.CheckBox;
        usernameEditText = binding.username;
        passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

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
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(main.shConnectionService, usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               SharedPreferences.Editor ed=sharedPreferences.edit();
               if(b) ed.putBoolean("remember_credentials",true);
               else ed.remove("remember_credentials");
               ed.commit();
            }
        });
    }

    private boolean encrypt(String username, String hostname, String password)
    {
        String encryption;
        String tocrypt=username+":"+password+"@"+hostname;
        final SecretKey secretKey=getKey("credentials_key_alias");
        if(secretKey!=null)
        {
            final Cipher cipher;
            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                String iv = new String(Base64.encodeToString(cipher.getIV(),Base64.DEFAULT));
                encryption = new String(Base64.encodeToString(cipher.doFinal(tocrypt.getBytes("windows-1251")),Base64.DEFAULT))+","+iv;
                SharedPreferences.Editor ed1=sharedPreferences.edit();
                ed1.putString(username+"@"+hostname,encryption);
                ed1.putString("last_user@"+hostname,username);
                ed1.commit();
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException e) {
                Log.e("TLS13", "Encryption error " + e.getMessage());
                return false;
            }
            Log.i("TLS13","Encrypted to "+username+"@"+hostname+" encrypted="+encryption);
            return true;
        }
        return false;

    }

    private String decrypt(String username, String hostname, String encrString)
    {

        SecretKey secretKey=getKey("credentials_key_alias");
        if(secretKey==null) return null;
        final Cipher cipher;
        String s;
        String fields[]=encrString.split(",");
        byte[] encrypted=Base64.decode(fields[0],Base64.DEFAULT);
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Log.i("TLS13","enc="+fields[0]+" ivString="+fields[1]);
            byte[] iv=Base64.decode(fields[1],Base64.DEFAULT);
            Log.i("TLS13","After base64.decode iv l="+iv.length);
//            final IvParameterSpec spec = new IvParameterSpec(iv);
//            Log.i("TLS13","After GCMSpec");
            cipher.init(Cipher.DECRYPT_MODE, secretKey,new IvParameterSpec(iv));
            Log.i("TLS13","after decrypt cipher init");
            s=new String(cipher.doFinal(encrypted),"UTF-8");
            Log.i("TLS13","after decryption");
            int i1=s.indexOf(':');
            int i2=s.indexOf('@');
            if(i1!=-1 && i2!=-1 && i2>i1 && s.substring(0,i1).equalsIgnoreCase(username) && s.substring(i2+1).equalsIgnoreCase(hostname))
            return s.substring(i1+1,i2);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
            Log.e("TLS13","Decrypt error "+e.getClass().toString()+" "+e.getMessage());
            return null;
        }
        Log.e("TLS13","Error in decryption string "+s);
        return null;
    }

    private SecretKey getKey(String alias)
    {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
//            keyStore.deleteEntry(alias);
            if (!keyStore.containsAlias(alias)) {
                final KeyGenerator keyGenerator = KeyGenerator
                        .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                final KeyGenParameterSpec keyGenParameterSpec =
                new KeyGenParameterSpec.Builder(alias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build();
                keyGenerator.init(keyGenParameterSpec);
                Log.i("TLS13","Key generated!");
                return keyGenerator.generateKey();
            }
            else
            {
                final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                        .getEntry(alias, null);
                return secretKeyEntry.getSecretKey();

            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | KeyStoreException | CertificateException | IOException | UnrecoverableEntryException e) {
            Log.e("TLS13","Error while getting key "+e.getMessage());
            return null;
        }
    }

    private boolean rememberCredentials(LoggedInUserView loggedInUser){
        return encrypt(loggedInUser.getDisplayName(),"hostname","password");
    }


    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        if(sharedPreferences.getBoolean("remember_credentials",false)) rememberCredentials(model);
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