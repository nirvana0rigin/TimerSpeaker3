package jp.co.nirvana0rigin.timerspeaker3;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class Sync extends Fragment {

	/*
	paramは全てのFragmentに共通で使う。
    */
    public static Param param ;
    private static final String PARAM = "param";
    public static long sec;
    
    private static Bundle args;
    private OnSyncListener mListener;
    public static Context con;
    public static Resources res;




    //__________________________________________________for life cycles

    //初回は必ずここから起動
    public static Sync newInstance(Param p) {
        param =p;
        Sync fragment = new Sync();
        args = new Bundle();
        args.putSerializable(PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }

    public Sync() {
        // Required empty public constructor
    }

    //paramの復帰とコンテキスト取得
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            param = (Param)getArguments().getSerializable(PARAM);
        }
		//activity再生成時に破棄させないフラグを立てる
		setRetainInstance(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		//NOTHING
    }

    //paramを復帰
    @Override
    public void onStart() {
        super.onStart();
        param = (Param)args.getSerializable(PARAM);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        args.putSerializable(PARAM, param);
        toActivity(param);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PARAM, param);
        toActivity(param);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

	@Override
    public void onDestroy() {
        super.onDestroy();
    }








    //________________________________________________for connection on Activity

    public interface OnSyncListener {
        public void onSyncParam(Param param);
        public void onSyncSec();
        public void onSyncTimeUp();
    }

    public void toActivity(Param param) {
        if (mListener != null) {
            mListener.onSyncParam(param);
        }
    }

    public void sendSec() {
        if (mListener != null) {
            mListener.onSyncSec();
        }
    }

    public void onTimeUp(){
        if (mListener != null) {
            param.resetParam();
            toActivity(param);
            mListener.onSyncTimeUp();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnSyncListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSynkListener");
        }
        con = context.getApplicationContext();
        res = getResources();
    }








    //_________________________________________________for work on this Fragment


    public void resetParam() {
        param.resetParam();
        toActivity(param);
    }

    
}


