package com.lxsoft.stepapp;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.lxsoft.frame.BaseActivity;

public class WelcomeActivity extends BaseActivity {


    public static final int DELAY_MILLIS = 3000;
    private Handler handler;
    private Runnable jumpRunnable;
    @Override
    protected void onInitVariable() {

        handler = new Handler();
        jumpRunnable = new Runnable() {
            @Override
            public void run() {
                /*跳转到Home*/
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this,HomeActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        };
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
