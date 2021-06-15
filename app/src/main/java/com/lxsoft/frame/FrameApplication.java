package com.lxsoft.frame;

import android.app.Activity;
import android.app.Application;
import android.content.Context;


import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.BuildConfig;
import com.umeng.commonsdk.UMConfigure;

import java.util.LinkedList;

public class FrameApplication extends Application {
    private  static LinkedList<Activity> actList = new LinkedList<Activity>();

    public LinkedList<Activity> getActivityList() {
        return actList;
    }
    /*添加到列表*/
    public  static void addToActivityList(final Activity act){
        if(act != null){
            actList.add(act);
        }
    }
    /*删除act*/
    public static void removeFromActivityList(final  Activity act){
        if(actList != null && actList.size() > 0 && actList.indexOf(act) != -1){
            actList.remove(act);
        }
    }
    /*清理所有activity*/
    public static void clearActivityList(){
        for(int i = actList.size()-1;i>=0;i--){
            final Activity act = actList.get(i);
            if(act != null){
                act.finish();
            }
        }
    }

    public static void exitApp(){
        try {
            clearActivityList();
        }catch (final Exception e){

        }finally {
            /*退出*/
            System.exit(0);
            /*杀掉当前进程的PID*/
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private PrefsManager prefsManager;
    private static FrameApplication instance;

    public PrefsManager getPrefsManager() {
        return prefsManager;
    }

    public void setPrefsManager(PrefsManager prefsManager) {
        this.prefsManager = prefsManager;
    }

    public static FrameApplication getInstance() {
        return instance;
    }

    public static void setInstance(FrameApplication instance) {
        FrameApplication.instance = instance;
    }

    private ErrorHandler errorHandler;
    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        UMConfigure.setLogEnabled(BuildConfig.DEBUG);
        UMConfigure.init(this,UMConfigure.DEVICE_TYPE_BOX,null);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

        instance = this;
        prefsManager = new PrefsManager(this);
        errorHandler = ErrorHandler.getInstance();
    }

}
