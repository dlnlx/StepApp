package com.lxsoft.stepapp;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.lxsoft.frame.BaseActivity;
import com.lxsoft.frame.LogWriter;
import com.umeng.analytics.MobclickAgent;

public class WelcomeActivity extends BaseActivity {


    public static final int DELAY_MILLIS = 3000;
    private Handler handler;
    private Runnable jumpRunnable;
    @Override
    protected void onInitVariable() {

//        new Thread("lxsoft-step"){
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(3*60*1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
        new MyTask("lxsoft-step").start();

        handler = new Handler();
        jumpRunnable = new Runnable() {
            @Override
            public void run() {
                /*跳转到Home*/
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this,BannerExpressActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        };
    }

    public static class MyTask extends Thread{
        public MyTask(String name){
            super(name);
        }
        @Override
        public void run() {
            try {
                Thread.sleep(3*60*1000);
            } catch (InterruptedException e) {
                LogWriter.d(e.toString());
            }
        }
    }

    @Override
    protected void onInitView(Bundle savedInstanceState) {

        setContentView(R.layout.act_welcome);
    }

    @Override
    protected void onRequestData() {
        handler.postDelayed(jumpRunnable,DELAY_MILLIS);
    }

}
