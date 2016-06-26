package jp.co.nirvana0rigin.timerspeaker3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.Calendar;

public class MainActivity
        extends AppCompatActivity
        implements Config.OnConfigListener, GoConfig.OnGoConfigListener, Reset.OnResetListener, Start.OnStartListener, Sync.OnSyncListener {

    private static Context con;
    private Resources res;
    private Bundle b ;
    private FragmentManager fm;
    private static Timer timer;
	private boolean isBound;
    private static boolean isPause;

    private static Param param ;
    private static final String PARAM = "param";

    private Counter counter;
    private Info info;
    private Start start;
    private GoConfig goConfig;
    private Reset reset;
    private Config config;
    private static Sync sync;

    private Fragment[] fragments ;
    private int[] fragmentsID;
    private String[] fragmentsTag = {"counter","info","start","reset","go_config"};
    /*
        0: counter
        1: info
        2: start
        3: go_config
        4: reset
     */







    //________________________________________________________for life cycles

    //リソース生成のみ
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        res = getResources();
        con = getApplicationContext();
        b = savedInstanceState;
        fm = getSupportFragmentManager();

        if (b != null) {
            param = (Param)b.getSerializable(PARAM);
        } else {
            b = new Bundle();
            if(sync != null){
                param = sync.param;
            }else{
                param = new Param(1,1,0,0,false,true,300*1000);
            }
            b.putSerializable(PARAM, param);
        }

        int[] fragmentsID2 = {R.id.counter, R.id.info, R.id.start, R.id.reset, R.id.go_config};
        fragmentsID = fragmentsID2;
        
    }

    //描画生成
    @Override
    public void onStart() {
    	Log.d("_______main_____","onStart");
        super.onStart();
        startService(new Intent(con, Timer.class));
        doBindService();
        if(sync == null) {
            param = (Param)b.getSerializable(PARAM);
        }else {
            if (sync != null) {
                param = sync.param;
            } else {
                param = new Param(1, 1, 0, 0, false, true, 300*1000);
            }
        }
        createMainFragments();
        addMainFragments() ;
    }

	@Override
    public void onResume() {
        super.onResume();
    	Log.d("_______main_____","onResume");
        isPause = false;
    	if(param.isRunning() && !param.isAlreadyEnded()){
            resumeRestartTimer();
    	}
    }

    @Override
    public void onPause() {
    	Log.d("_______main_____","onPause");
        isPause = true;
        doUnbindService();
        super.onPause();
    }

    @Override
    public void onStop() {
    	Log.d("_______main_____","onStop");
        b.putSerializable(PARAM, param);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    	Log.d("_______main_____","onSave");
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onDestroy() {
    	Log.d("_______main_____","onD");
        super.onDestroy();
    }








    //___________________________________________________for connection on Fragments

    @Override
    public void onConfig(){
        //NOTHING
    }

    @Override
    public void onConfigBackButton(){
        createMainFragments();
        addMainFragments() ;
    }

    @Override
    public void onGoConfig() {
        removeMainFragments();
    }

    @Override
    public void onReset() {
        if (timer != null) {
            resetTimer();
        }
        if (sync != null) {
            sync.resetParam();
        }
        if( counter != null ){
            counter.resetCounterText();
        }
        if (goConfig != null) {
            goConfig.addButton();
        }
        if (reset != null) {
            reset.removeButton();
        }
        if (reset != null) {
            start.startButtonStatus();
        }
    }

    @Override
    public void onStartButton() {
        if(param.isRunning()){
            if(param.getStopTime() ==0) {
                startTimer();
                Log.d("_____startB___S___","___"+param.getEndingTime()+"___");
                Log.d("_____startB___S__","____"+System.currentTimeMillis()+"___");
                Log.d("_____startB__S____","____"+param.getStartTime()+"___");
            }else{
                Log.d("_____startB__RES___","___"+param.getEndingTime()+"___");
                Log.d("_____startB___RES__","____"+System.currentTimeMillis()+"___");
                Log.d("_____startB__RES____","____"+param.getStartTime()+"___");
                buttonRestartTimer();
            }
            goConfig.removeButton();
            reset.removeButton();
        }else if(!param.isReset() && param.isHalfwayStopped()){
        	stopTimer();
            reset.addButton();
        }else if (param.isReset() && param.isHalfwayStopped()) {
            //スレッド終了まではここではしないので、
            //上２つ以外の変更（この３つ目）はここではありえない。
            //スレッド終了に関してはresetにて。
        }
    }

    @Override
    public void onSyncParam(Param p) {
        param = p;
        b.putSerializable(PARAM, param);
    }

    @Override
    public void onSyncSec() {
        counter.createCounterText();
    }

    @Override
    public void onSyncTimeUp(){
        onReset();
    }









    //___________________________________________________________for work on Activity

    private void createMainFragments() {
        addNewSyncFragment();
        if(counter == null) {
            counter = new Counter();
        }
        if (info == null) {
            info = new Info();
        }
        if (start == null) {
            start = new Start();
        }
        if (goConfig == null) {
            goConfig = new GoConfig();
        }
        if (reset == null) {
            reset = new Reset();
        }
        Fragment[] fragments2 = {counter, info, start, reset,goConfig};
        fragments = fragments2;
    }

    private void addMainFragments(){
        FragmentTransaction transaction = fm.beginTransaction();
        if(isAlive("config")){
            transaction.remove(config);
            config = null;
        }
        for (int i = 0; i < 5; i++) {
            if (!isAlive(fragmentsTag[i])) {
                transaction.add(fragmentsID[i], fragments[i], fragmentsTag[i]);
            }
        }
        transaction.commit();
    }

    private void removeMainFragments(){
        FragmentTransaction transaction = fm.beginTransaction();
        for(int i=0; i<5; i++){
            if(isAlive(fragmentsTag[i]) ) {
                transaction.remove(fragments[i]);
            }
        }
        transaction.addToBackStack(null);

        if(!(isAlive("config")) ){
            if(config ==null) {
                config = new Config();
            }
            transaction.add(R.id.config, config, "config");
        }else{
            transaction.show(config);
        }
        transaction.commit();
    }

    private void addNewSyncFragment() {
        if (!(isAlive("sync"))) {
            if (sync == null) {
                sync = Sync.newInstance(param);
            }
            fm.beginTransaction().add(sync, "sync").commit();
        }
    }

    private void removeFragment(Fragment fra, String tag) {
        if (isAlive(tag)) {
            fm.beginTransaction().remove(fra).commit();
        }
    }

    private boolean isAlive(String tag){
        if(fm.findFragmentByTag(tag) == null){
            return false;
        }else{
            return true;
        }
    }

	private void startTimer(){
        startService(new Intent(con, Timer.class));
        doBindService();
        timer.setEndInterval(param.getEndingTime(),param.getInterval());
        if(param.getStopTime() ==0) {
        	timer.startTimer(true);
        }else{
        	timer.startTimer(false);
        }
        TimeReceiver timeReceiver = new TimeReceiver();
        timeReceiver.execute(0L,0L);
	}
	
	private void resumeRestartTimer(){
        long delay = param.getResumeDelay();
        restartTimer(delay); 
	}
	
	private void buttonRestartTimer(){
        long delay = param.getButtonDelay();
        restartTimer(delay); 
	}
	
	private void restartTimer(long delay){
        doBindService();
        timer.startTimer(false);
        long sec = timer.getSec()/100;
        TimeReceiver timeReceiver = new TimeReceiver();
		timeReceiver.execute(delay,sec);
	}

	private void stopTimer(){
		timer.stopTimer();
	}

	private void resetTimer(){
		timer.endTimer();
        doUnbindService();
		stopService(new Intent(MainActivity.this, Timer.class));
	}

	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            timer = ((Timer.TimerBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            timer = null;
        }
    };

    private void doBindService() {
        bindService(new Intent(con, Timer.class), mConnection, Context.BIND_NOT_FOREGROUND);
        isBound = true;
        if(timer.isDestroyed){
        	onReset();
        }
    }

    private void doUnbindService() {
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
    }

    private class TimeReceiver extends AsyncTask<Long, Long, Long> {

        public TimeReceiver() {
            super();
        }

        @Override
        protected Long doInBackground(Long... value) {
            try {
                publishProgress(value[1]);
                Thread.sleep(value[0]);
            } catch (InterruptedException e) {
                //NOTHING
            }
            long sec = value[1] ;
            while (param.isRunning()) {
                try {
                    synchronized (this) {
                        sec++;
                    }
                    publishProgress(sec);
                    Thread.sleep(1000);

                    if (isPause) {
                        Log.d("_____async_isPause____","_______while______");
                        break;
                    }
                    if(param.isAlreadyEnded()){
                        Log.d("_____async_isAlr____","___"+param.getEndingTime()+"___");
                        Log.d("_____async_isAlr____","____"+System.currentTimeMillis()+"___");
                        Log.d("_____async_isAlr____","____"+param.getStartTime()+"___");
                        Log.d("_____async_isAlr____","_______while______");
                        break;
                    }
                } catch (InterruptedException e) {
                    //NOTHING
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... progress) {
            sync.sec = progress[0];
            sync.sendSec();
        }

        @Override
        protected void onPostExecute(Long result) {
            if(param.isAlreadyEnded()) {
                sync.onTimeUp();
            }
        }

    }





}
