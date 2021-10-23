package com.example.test3;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class MyAsyncConnectionService extends Service {

    MyAsyncConnection asyncConnection;
    private final IBinder binder = new MyAsyncConnectionIBinder();
    MainActivity main;

    public MyAsyncConnectionService(MainActivity main) {
        this.main=main;
    }

    public class MyAsyncConnectionIBinder extends Binder {
        MyAsyncConnectionService getService() {
            return MyAsyncConnectionService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        asyncConnection=new MyAsyncConnection(main);
        return binder;
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}