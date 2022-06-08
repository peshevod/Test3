package com.example.test3;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MySession {
    public String devName;
    public String sensor1;
    public String sensor2;
    public GregorianCalendar lastDataTime;
    public int devNonce;
    public long fcntUp;
    public int temperature;
    public int battery;
    public int remoteRSSI;
    public int remoteSNR;
    public int remotePower;
    public int localPower;
    public int localRssi;
    public int localSnr;
    public int values;

    public MySession(String devName)
    {
        this.devName=devName;
        lastDataTime =new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
    }

    public void setTime(long time)
    {
        lastDataTime.setTime(new Date(time*1000));
    }

    public String getTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        return dateFormat.format(lastDataTime.getTime());
    }
}
