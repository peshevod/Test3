package com.example.test3;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MySession {
    public String devName;
    public String Sensor1;
    public String Sensor2;
    public GregorianCalendar lastTime;
    public int devnonce;
    public long fcntup;
    public int temperature;
    public int batlevel;
    public int rssi;
    public int snr;
    public int power;
    public int local_power;
    public int local_rssi;
    public int local_snr;
    public int values;

    public MySession(String devName)
    {
        this.devName=devName;
        lastTime=new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
    }

    public void setTime(long time)
    {
        lastTime.setTime(new Date(time*1000));
    }

    public String getTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        return dateFormat.format(lastTime.getTime());
    }
}
