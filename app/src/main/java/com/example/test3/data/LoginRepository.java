package com.example.test3.data;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.example.test3.MainActivity;
import com.example.test3.SHConnectionService;
import com.example.test3.data.model.LoggedInUser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;


    private LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    public void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result login(SHConnectionService service, String username, String password, boolean rememberCredentials) {
        // handle login
        String password1=null;
        String password_old=null;
        Log.i("TLS13","LoginRepository login");
        String encryption=service.main.sharedPreferences.getString(username+"@"+service.getHostname(),null);
        if(encryption!=null) {
            if (rememberCredentials)
            {
                String decryption=decrypt(encryption);
                if(decryption!=null) {
                    int i1 = decryption.indexOf(':');
                    int i2 = decryption.indexOf('@');
                    if (i1 != -1 && i2 != -1 && i2 > i1 && decryption.substring(0, i1).equalsIgnoreCase(username) && decryption.substring(i2 + 1).equalsIgnoreCase(service.getHostname()))
                        password_old = decryption.substring(i1 + 1, i2);
                }
            }
            else {
                SharedPreferences.Editor ed1 = service.main.sharedPreferences.edit();
                ed1.remove(username + "@" + service.getHostname());
                ed1.commit();
            }
        }
        if(password==null || password.equals("::::::::"))
        {
            if(password_old!=null) password1=password_old;
            else return new Result.Error(new Exception("Password not set"));
        } else password1=password;
        Result result = dataSource.login(username, password1);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
            if( rememberCredentials && (password_old==null || !password1.equals(password_old)) )
            {
                String new_encryption=encrypt(username+":"+password1+"@"+service.getHostname());
                SharedPreferences.Editor ed2=service.main.sharedPreferences.edit();
                ed2.putString(username+"@"+service.getHostname(),new_encryption);
                ed2.putString("last_user@"+service.getHostname(),username);
                ed2.commit();
            }
        }
        return result;
    }




    private String encrypt(String tocrypt)
    {
        String encryption=null;
        final SecretKey secretKey=getKey("credentials_key_alias");
        if(secretKey!=null)
        {
            final Cipher cipher;
            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                String iv = new String(Base64.encodeToString(cipher.getIV(),Base64.DEFAULT));
                encryption = new String(Base64.encodeToString(cipher.doFinal(tocrypt.getBytes("windows-1251")),Base64.DEFAULT))+","+iv;
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException e) {
                Log.e("TLS13", "Encryption error " + e.getMessage());
            }
            Log.i("TLS13","Encrypted to "+encryption);
            return encryption;
        }
        return null;

    }

    private String decrypt(String encrString)
    {

        SecretKey secretKey=getKey("credentials_key_alias");
        if(secretKey==null) return null;
        final Cipher cipher;
        String s=null;
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
            return s;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
            Log.e("TLS13","Decrypt error "+e.getClass().toString()+" "+e.getMessage());
        }
        Log.e("TLS13","Error in decryption string "+encrString);
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

}