package com.ds.utils;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.Timer;
import java.util.TimerTask;

import static com.ds.role.remoteClient.RemoteClientImpl.processFinalBid;

public class AuctionTimer {
    public Timer timer;
    public TimerTask timerTask;

    // each auction has 60s for bidding
    private static final int countDownForAuction = 60;
    private int remainingTime;
    public CountDownTimer countDownTimer;

    public AuctionTimer() {
        countDownTimer = new CountDownTimer();
    }

    public void start() {
        this.timer = new Timer();
        this.remainingTime = countDownForAuction;

        this.timerTask = new TimerTask() {
            @Override
            public void run() {
            if (remainingTime <= 0) {
                try {
                    timerTask.cancel();
                    countDownTimer.timerTask.cancel();
                    processFinalBid();
                } catch (IOException | NotBoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                remainingTime -= 1;
                System.out.println("remaining time: " + remainingTime);
            }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1 * 1000, 1 * 1000);
        this.countDownTimer.start();
    }

    // reset the timer
    public void reset() {
        this.timerTask.cancel();
        this.remainingTime = countDownForAuction;
        this.countDownTimer.reset();
        start();
    }

    public static void main(String[] args) {
        AuctionTimer at = new AuctionTimer();
        at.start();
    }
}
