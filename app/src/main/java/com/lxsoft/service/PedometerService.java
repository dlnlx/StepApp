package com.lxsoft.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lxsoft.bean.PedometerChartBean;
import com.lxsoft.db.DBHelper;
import com.lxsoft.frame.FrameApplication;
import com.lxsoft.frame.LogWriter;
import com.lxsoft.utiles.ACache;
import com.lxsoft.utiles.Settings;
import com.lxsoft.utiles.Utiles;
import com.lxsoft.bean.PedometerBean;

import java.util.ArrayList;
import java.util.List;


public class PedometerService extends Service {

    private SensorManager sensorManager;
    private PedometerBean pedometerBean;
    private PedometerListener pedometerListener;
    public static final int STATUS_NOT_RUN = 0;
    public static final int STATUS_RUNNING = 1;
    private int runStatus = STATUS_NOT_RUN;
    private SharedPreferences mSpf;
    private static final long SAVE_CHART_TIME=60000L;
    private Settings settings;
    private List<PedometerBean> dataList = new ArrayList<>();

    private PedometerChartBean pedometerChartBean;
    private static Handler handler = new Handler();
    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if(runStatus == STATUS_RUNNING){
                if(handler != null && pedometerChartBean != null){
                    handler.removeCallbacks(timeRunnable);
                    updateChartData();//更新数据
                    handler.postDelayed(timeRunnable, SAVE_CHART_TIME);
                }
            }
        }
    };

    public double getCalorieBySteps(int stepCount){
        //步长
        float stepLen = settings.getStepLength();
        //体重
        float bodyWeight=settings.getBodyWeight();
        double METRIC_WALKING_FACTOR = 0.708;//走路
        double METRIC_RUNNING_FACTOR = 1.02784823;//跑步
        //跑步热量（kcal）=体重（kg）×距离（km）×1.02784823
        //走路热量（kcal）=体重（kg）×距离（km）×0.708
        double calories = (bodyWeight*METRIC_WALKING_FACTOR)*stepLen*stepCount/100000.0;
        return calories;
    }


    public double getStepDistance(int stepCount){
        //步长
        float stepLen = settings.getStepLength();
        double distance = (stepCount*(long)(stepLen))/100000.0f;
        return distance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pedometerBean = new PedometerBean();
        pedometerListener = new PedometerListener(pedometerBean);
        settings = new Settings(this);
        pedometerChartBean = new PedometerChartBean();
    }

    //更新了计步器的图表数据
    public void updateChartData(){
        if(pedometerChartBean.getIndex()<1440-1){
            pedometerChartBean.setIndex(pedometerChartBean.getIndex()+1);
            pedometerChartBean.getArrayData()[pedometerChartBean.getIndex()] = pedometerBean.getStepCount();
            String jsonStr = Utiles.objToJson(pedometerChartBean);
            LogWriter.d("JSON", jsonStr);
        }
    }


    /*将对象保存*/
    private void saveChartData(){
        String jsonStr = Utiles.objToJson(pedometerChartBean);
        mSpf = super.getSharedPreferences(String.valueOf(pedometerBean.getId()+1),MODE_PRIVATE);
        SharedPreferences.Editor editor = mSpf.edit();
        editor.putString(String.valueOf(pedometerBean.getId()+1),jsonStr);
        LogWriter.d("CHART", "存储id:"+String.valueOf(pedometerBean.getId()));
//        editor.putString("test",jsonStr);
        editor.commit();
        LogWriter.d("JSON", jsonStr);
        ACache.get(FrameApplication.getInstance()).put("JsonChartData", jsonStr);
    }

    private IPedometerService.Stub iPedometerService = new IPedometerService.Stub(){

        @Override
        public void startCount() throws RemoteException {
            if(sensorManager != null && pedometerListener != null){
                Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(pedometerListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                pedometerBean.setStartTime(System.currentTimeMillis());
                pedometerBean.setDay(Utiles.getTimestempByDay());//记录的是哪天的数据
                runStatus=STATUS_RUNNING;
                handler.postDelayed(timeRunnable, SAVE_CHART_TIME);//开始触发数据刷新
            }
        }

        @Override
        public void stopCount() throws RemoteException {
            if(sensorManager != null && pedometerListener != null){
                Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.unregisterListener(pedometerListener,sensor);
                runStatus = STATUS_NOT_RUN;
                handler.removeCallbacks(timeRunnable);
            }
        }

        @Override
        public void resetCount() throws RemoteException {
            if(pedometerBean != null){
                saveData();
                pedometerBean.reset();
//                saveData();
            }
            if(pedometerChartBean != null){
                LogWriter.d("JSON", pedometerChartBean.toString());
                saveChartData();
                pedometerChartBean.reset();
                //saveChartData();
            }
            if(pedometerListener != null){
                pedometerListener.setCurrentSteps(0);
            }
        }

        @Override
        public int getSetpsCount() throws RemoteException {
            if(pedometerBean != null){
                return pedometerBean.getStepCount();
            }
            return 0;
        }

        @Override
        public double getCalorie() throws RemoteException {
            if(pedometerBean != null){
                return getCalorieBySteps(pedometerBean.getStepCount());
            }
            return 0;
        }

        @Override
        public double getDistance() throws RemoteException {
            return getDistanceVal();
        }

        private double getDistanceVal() {
            if(pedometerBean != null){
                return getStepDistance(pedometerBean.getStepCount());
            }
            return 0;
        }

        @Override
        public void saveData() throws RemoteException {
            if(pedometerBean != null){
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        LogWriter.d("JSON111", pedometerBean.toString());
                        DBHelper dbHelper = new DBHelper(PedometerService.this, DBHelper.DB_NAME);
                        pedometerBean.setStepCount(pedometerListener.getCurrentSteps());
                        pedometerBean.setLastStepTime(System.currentTimeMillis());
                        //设置距离
                        pedometerBean.setDistance(getDistanceVal());
                        //设置热量消耗
                        pedometerBean.setCalorie(getCalorieBySteps(pedometerBean.getStepCount()));
                        long time = (pedometerBean.getLastStepTime() - pedometerBean.getStartTime())/1000;
                        if(time == 0){
                            pedometerBean.setPace(0);//设置多少步/分钟
                            pedometerBean.setSpeed(0);
                        }else {
                            int pace = Math.round(60*pedometerBean.getStepCount()/time);
                            pedometerBean.setPace(pace);
                            long speed = Math.round((pedometerBean.getDistance()/1000)/(time/60*60));
                            pedometerBean.setSpeed(speed);
                        }
                        LogWriter.d("TAG", " "+pedometerBean.toString());
                        dbHelper.writeToDatabase(pedometerBean);
                        dataList = dbHelper.getFromDatabase();
                        pedometerBean.setId(dataList.size());
                        LogWriter.d("CHART", "个数:"+dataList.size());
                        pedometerBean.setStepCount(0);
                    }
                }).start();
            }
        }

        @Override
        public void setSensitivity(double sensitivity) throws RemoteException {
//            if(settings != null){
//                settings.setSensitivity((float) sensitivity);
//            }
            if(pedometerListener != null){
                pedometerListener.setSensitivity((float)sensitivity);
            }
        }

        @Override
        public double getSensitivity() throws RemoteException {
            if(settings != null){
                return settings.getSensitivity();
            }
            return 0;
        }

        @Override
        public void setInterval(int interval) throws RemoteException {
            if(settings != null){
                settings.setInterval(interval);
            }
            if(pedometerListener != null){
                pedometerListener.setmLimit(interval);
            }
        }

        @Override
        public int getInterval() throws RemoteException {
            if(settings != null){
                return settings.getInterval();
            }
            return 0;
        }

        @Override
        public long getStartTimeStamp() throws RemoteException {
            if(pedometerBean != null){
                return pedometerBean.getStartTime();
            }
            return 0L;
        }

        @Override
        public int getServiceStatus() throws RemoteException {
            return runStatus;
        }

        @Override
        public PedometerChartBean getCharData() throws RemoteException {
            return pedometerChartBean;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iPedometerService;
    }
}
