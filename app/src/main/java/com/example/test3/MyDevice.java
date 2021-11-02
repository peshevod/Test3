package com.example.test3;

public class MyDevice {
    public final String devName;
    public final String devEui;
    public final String version;

    public MyDevice(String devName,String devEui, String version)
    {
        this.devName=devName;
        this.devEui=devEui;
        this.version=version;
    }
}
