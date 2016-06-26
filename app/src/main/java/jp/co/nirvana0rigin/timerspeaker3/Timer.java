package jp.co.nirvana0rigin.timerspeaker3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Timer extends Service {

    public static long sec;
    public static long end ;
    public static long interval ;
	public static boolean isDestroyed = true;

    @Override
    public void onCreate() {
        super.onCreate();
        //バインドの度に初期化されるため、何もしない
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

	public class TimerBinder extends Binder {
        Timer getService() {
            return Timer.this;
        }
    }

	private final IBinder timerBinder = new TimerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return timerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }







	private static ScheduledExecutorService scheduler;
    private static ScheduledFuture<?> future;
    private static Speak speak;

    public void startTimer(boolean reset) {
    	if(reset){
    		sec = 0;
    	}
    	isDestroyed = false;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        future = scheduler.scheduleAtFixedRate(new Task(), 0, 10, TimeUnit.MILLISECONDS);
        setNotification();
        speak = new Speak(getApplicationContext(),interval);
    }

    public void stopTimer() {
        if (future != null) {
            future.cancel(true);
        }
    }

    public void endTimer() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            isDestroyed = true;
        }
    }
    
    public void serviceEnd(){
    	stopTimer();
        endTimer();
        stopSelf();
    }

    public void speakMinute(){
        String min = ("" + (sec/100) );
        speak.speakMinute(min);
    }

    public long getSec(){
       return sec;
    }

    public long getEnd(){
        return end;
    }
    
    public boolean isDestroyed(){
        return isDestroyed;
    }

    public void setEndInterval(long e, long i){
        end = e;
        interval = i;
    }


    private class Task implements Runnable {
        public void run() {
            synchronized (this) {
                sec++;
            }
            synchronized (this) {
                end--;
            }
			if( sec %(interval*100) == 0){
                speakMinute();
			}

            if(end == 0) {
                serviceEnd();
            }
        }
    }

    public void setNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pen = PendingIntent.getActivity(this, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pen);
        builder.setTicker(getText(R.string.app_name));
        builder.setSmallIcon(R.drawable.c01b);
        builder.setContentTitle(getText(R.string.app_name));
        builder.setContentText(getString(R.string.now_running));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(false);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }


}
