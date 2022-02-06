package com.example.test3;

public class MyDevice {
    public final String devName;
    public final String devEui;
    public final String version;
    public final String Sensor1;
    public final String Sensor2;

    public MyDevice(String devName,String devEui, String version, String Sensor1, String Sensor2)
    {
        this.devName=devName;
        this.devEui=devEui;
        this.version=version;
        this.Sensor1=Sensor1;
        this.Sensor2=Sensor2;
    }
}
