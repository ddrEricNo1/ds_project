package com.ds.utils;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import static com.ds.role.remoteClient.RemoteClientImpl.*;

public class CountDownTimer {
    public CountDownTimer() {}
    public Timer timer;
    public TimerTask timerTask;

    public void start() {
        this.timer = new Timer();
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                String msg = "10 seconds has passed...";
                try {
                    if (registeredUsers.size() > 0) {
                        broadcast(msg);
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (NotBoundException e) {
                    throw new RuntimeException(e);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(msg);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 10 * 1000, 10 * 1000); // 每隔一秒执行一次任务
    }

    // reset the timer
    public void reset() {
        this.timerTask.cancel();
        start();
    }
}