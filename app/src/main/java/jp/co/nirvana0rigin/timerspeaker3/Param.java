package jp.co.nirvana0rigin.timerspeaker3;

import java.io.Serializable;

public class Param implements Serializable {

    private static final long serialVersionUID = 1L;

    private int carNo;  //アニメの車用。
    private long interval;  //秒。音声間隔。
    private volatile long startTime;  //ミリ秒。初回又は途中からのstart時のタイムスタンプ。
    private volatile long stopTime;  //ミリ秒。表示タイマーのstop時のタイムスタンプ。
    private boolean halfwayStopped; //途中でストップ状態か否か
    private boolean reset;  //リセット状態か否か。
    private volatile long endingTime;  //ミリ秒。残り時間。

    private boolean running;


    public Param(int carNo, long interval, long startTime, long stopTime, boolean halfwayStopped, boolean reset, long endingTime) {
        this.carNo = carNo;
        this.interval = interval;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.halfwayStopped = halfwayStopped;
        this.reset = reset;
        this.endingTime = endingTime;
        if (endingTime == 0) {
            if (interval <= 10) {
                this.endingTime = 300 * 1000;
            } else {
                this.endingTime = 60 * 60 * 1000;
            }
        }
        if (!halfwayStopped && !reset) {
            this.running = true;
        } else {
            this.running = false;
        }
    }

    public int getCarNo() {
        return carNo;
    }

    public void setCarNo(int carNo) {
        this.carNo = carNo;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
        if (interval <= 10) {
            this.endingTime = 300 * 1000;
        } else {
            this.endingTime = 60 * 60 * 1000;
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public synchronized void setStartTime(long time) {
        this.startTime = time;
    }

    public long getStopTime() {
        return stopTime;
    }

    public synchronized void setStopTime(long time) {
        this.stopTime = time;
        this.endingTime = endingTime - (stopTime-startTime);
    }

    public boolean isHalfwayStopped() {
        return halfwayStopped;
    }

    public void setHalfwayStopped(boolean halfwayStopped) {
        this.halfwayStopped = halfwayStopped;
        if(!halfwayStopped && !reset){
        	this.running = true;
        }else{
        	this.running = false;
        }
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
        if(!halfwayStopped && !reset){
        	this.running = true;
        }else{
        	this.running = false;
        }
    }
    
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    
    public long getEndingTime() {
        return endingTime;
    }

    public synchronized void setEndingTime(long time) {
        this.endingTime = time;
    }

    public void resetParam() {
        this.startTime = 0;
        this.stopTime = 0;
        this.halfwayStopped = false;
        this.reset = true;
        if(interval <= 10){
        	this.endingTime = 300*1000;
        }else{
        	this.endingTime = 60*60*1000;
        }
    }
    
    public long getResumeDelay(){
        long spend = System.currentTimeMillis() - startTime;
        long endingTime2 = endingTime - spend;
        endingTime = endingTime2;
        long delay = endingTime - (endingTime / 1000) * 1000;
        return delay;
    }

    public long getButtonDelay(){
        long delay = endingTime - ((endingTime / 1000) * 1000);
        return delay;
    }

    public boolean isAlreadyEnded(){
        if(running){
            long spend = System.currentTimeMillis() - startTime;
            if(endingTime - spend <= 0){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }


    
}
