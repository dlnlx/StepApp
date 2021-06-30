package com.lxsoft.stepapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.lxsoft.bean.PedometerChartBean;
import com.lxsoft.frame.BaseActivity;
import com.lxsoft.frame.LogWriter;
import com.lxsoft.service.IPedometerService;
import com.lxsoft.service.PedometerService;
import com.lxsoft.utiles.Utiles;
import com.lxsoft.widgets.CircleProgressBar;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class HomeActivity extends BaseActivity{

    private CircleProgressBar progressBar;
    private TextView textCalorie;
    private TextView time;
    private TextView distance;
    private TextView stepCount;
    private Button reset;
    private Button btnStart;
    private BarChart dataChart;

    private IPedometerService remoteService;
    private ImageView setting;
    private int status = -1;
    private static final int STATUS_NOT_RUNNING = 0;
    private static final int STATUS_RUMMING = 1;
    private boolean isRunning = false;
    private boolean isChartUpdate = false;
    private static final int MESSAGE_UPDATE_STEP_COUNT = 1000;
    private static final int MESSAGE_UPDATE_CHART_DATA = 2000;
    private static final int GET_DATA_TIME = 200;
    private static final long GET_CHART_DATA_TIME = 60000L;
    private PedometerChartBean chartBean;
    private boolean bindService = false;

    @Override
    protected void onInitVariable() {

    }

    @Override
    protected void onInitView(Bundle savedInstanceState) {

        setContentView(R.layout.act_home);
        progressBar = (CircleProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressBar.setMaxProgress(10000);
        setting = (ImageView) findViewById(R.id.imageView);
        textCalorie = (TextView) findViewById(R.id.textCalorie);
        time = (TextView) findViewById(R.id.time);
        distance = (TextView) findViewById(R.id.distance);
        stepCount = (TextView) findViewById(R.id.stepCount);
        reset = (Button) findViewById(R.id.reset);
        btnStart = (Button) findViewById(R.id.btnStart);
        dataChart = (BarChart) findViewById(R.id.datachart);
        setting.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("确认重置");
                builder.setMessage("您的记录将要被清除，确定吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(remoteService != null){
                            try {
                                remoteService.stopCount();
                                remoteService.resetCount();
                                chartBean = remoteService.getCharData();
                                updateChart(chartBean);
                                stepCount.setText("0步");
                                status = remoteService.getServiceStatus();
                                if(status == PedometerService.STATUS_RUNNING){
                                    btnStart.setText("停止");
                                }else if(status == PedometerService.STATUS_NOT_RUN){
                                    btnStart.setText("启动");
                                }
                            } catch (RemoteException e) {
                                LogWriter.d(e.toString());
                            }
                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("取消",null );

                AlertDialog resetDlg = builder.create();
                resetDlg.show();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try {
                    status = remoteService.getServiceStatus();
                } catch (RemoteException e) {
                    LogWriter.d(e.toString());
                }
                if(status == STATUS_RUMMING && remoteService != null){
                    try {
                        remoteService.stopCount();
                        btnStart.setText("启动");
                        isRunning = false;
                        isChartUpdate = false;
                    } catch (RemoteException e) {
                        LogWriter.d(e.toString());
                    }
                }else if(status == STATUS_NOT_RUNNING && remoteService != null){
                    try {
                        remoteService.startCount();
                        startStepCount();
                    } catch (RemoteException e) {
                        LogWriter.d(e.toString());
                    }
                }
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteService = IPedometerService.Stub.asInterface(service);
            try {
                status = remoteService.getServiceStatus();
                if(status ==STATUS_RUMMING){
                    startStepCount();
                }else{
                    btnStart.setText("启动");
                }
            } catch (RemoteException e) {
                LogWriter.d(e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            remoteService = null;
        }
    };

    private void startStepCount() throws RemoteException {
        btnStart.setText("停止");
        isChartUpdate = true;
        isRunning = true;
        //启动两个线程，定时获取数据，刷新UI
        new Thread(new StepRunnable()).start();
        new Thread(new ChartRunable()).start();
        chartBean = remoteService.getCharData();
        updateChart(chartBean);
    }

    @Override
    protected void onRequestData() {
        //检查服务是否运行
        //服务没有运行，启动服务，如果服务已经运行，直接绑定服务
        Intent serviceIntent =null;
        if(!Utiles.isServiceRunning(this, PedometerService.class.getName())){
            //服务没有运行，启动服务
            serviceIntent = new Intent(this,PedometerService.class);
            startService(serviceIntent);
        }else{
            //服务运行
            serviceIntent = new Intent(this,PedometerService.class);
            serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        //绑定服务操作
        bindService = bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        //初始化一些对应状态，按钮文字等
        if(bindService && remoteService != null){
            try {
                status = remoteService.getServiceStatus();
                if(status == PedometerService.STATUS_NOT_RUN){
                    btnStart.setText("启动");
                }else if(status == PedometerService.STATUS_RUNNING){
                    btnStart.setText("停止");
                    isRunning = true;
                    isChartUpdate = true;
                    //启动两个线程，定时获取数据，刷新UI
                    new Thread(new StepRunnable()).start();
                    new Thread(new ChartRunable()).start();
                }
            } catch (RemoteException e) {
                LogWriter.e(e.toString());
            }
        }else{
            btnStart.setText("启动");
        }
    }


    private class StepRunnable implements Runnable{

        @Override
        public void run() {
            while (isRunning){
                try {
                    status = remoteService.getServiceStatus();
                    if(status == STATUS_RUMMING){
                        handler.removeMessages(MESSAGE_UPDATE_STEP_COUNT);
                        //发送消息，让Handler去更新数据
                        handler.sendEmptyMessage(MESSAGE_UPDATE_STEP_COUNT);
                        Thread.sleep(GET_DATA_TIME);
                    }
                } catch (RemoteException e) {
                    LogWriter.d(e.toString());
                } catch (InterruptedException e) {
                    LogWriter.d(e.toString());
                }
            }
        }
    }

    private class ChartRunable implements  Runnable{

        @Override
        public void run() {
            while (isChartUpdate){
                try {
                    chartBean = remoteService.getCharData();
                    handler.removeMessages(MESSAGE_UPDATE_CHART_DATA);
                    handler.sendEmptyMessage(MESSAGE_UPDATE_CHART_DATA);
                    Thread.sleep(GET_CHART_DATA_TIME);
                } catch (RemoteException e) {
                    LogWriter.d(e.toString());
                } catch (InterruptedException e) {
                    LogWriter.d(e.toString());
                }
            }
        }
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_UPDATE_STEP_COUNT:{
                    //更新计步数据
                    updateStepCount();
                }
                break;
                case MESSAGE_UPDATE_CHART_DATA:{
                    if(chartBean != null){
                        updateChart(chartBean);
                    }
                }
                break;
                default:
                    LogWriter.d("Default = "+msg.what);
            }
            super.handleMessage(msg);
        }
    };

    public void updateStepCount(){
        if(remoteService != null){
            int stepCountVal = 0;
            double calorieVal = 0;
            double distanceVal = 0;
            try {
                stepCountVal = remoteService.getSetpsCount();
                calorieVal = remoteService.getCalorie();
                distanceVal = remoteService.getDistance();
            } catch (RemoteException e) {
                LogWriter.d(e.toString());
            }
            //更新数据到UI
            stepCount.setText(String.valueOf(stepCountVal)+"步");
            textCalorie.setText(com.lxsoft.utiles.Utiles.getFormatVal(calorieVal)+"卡");
            distance.setText(com.lxsoft.utiles.Utiles.getFormatVal(distanceVal));
            progressBar.setProgress(stepCountVal);
        }
    }


    public void updateChart(PedometerChartBean bean){
        String jsonStr = Utiles.objToJson(bean);
        LogWriter.d("JSON333", jsonStr);
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
        if(bean != null){
            for(int i=0;i<=bean.getIndex();i++){
                xVals.add(String.valueOf(i)+"分");
                int valY = bean.getArrayData()[i];
                yVals.add(new BarEntry(valY, i));
            }
            time.setText(String.valueOf(bean.getIndex())+"分");
            BarDataSet set1 = new BarDataSet(yVals, "所走的步数");
            set1.setBarSpacePercent(2f);
            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);
            BarData data = new BarData(xVals,dataSets);
            data.setValueTextSize(10f);
            dataChart.setData(data);
            dataChart.invalidate();
        }
    }


    @Override
    protected void onDestory() {
        super.onDestory();
        if(bindService){
            bindService = false;
            isRunning = false;
            isChartUpdate = false;
            unbindService(serviceConnection);
        }
    }


}
