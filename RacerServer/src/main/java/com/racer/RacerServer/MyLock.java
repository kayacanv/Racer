package com.racer.RacerServer;


public class MyLock {

    private boolean lockOpen = true;

    public synchronized void unlock() {
        lockOpen = true;
        notify();
    }

    public synchronized void lock() throws InterruptedException {
        while (lockOpen == false) {
            wait();
        }
        lockOpen = false;
    }
}