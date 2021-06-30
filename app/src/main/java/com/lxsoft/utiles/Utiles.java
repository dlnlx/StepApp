package com.lxsoft.utiles;

import android.app.ActivityManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.google.gson.Gson;
import com.lxsoft.bean.PedometerChartBean;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Utiles {
    public static String getTimestempByDay(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = new Date();
        String dateStr = sdf.format(d);
        try {
            System.out.println(dateStr);
            Date date = sdf.parse(dateStr);
            return dateStr;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String objToJson(Object obj){
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static PedometerChartBean jsonToObj(String gson){
        Gson gson2 = new Gson();
        return gson2.fromJson(gson, PedometerChartBean.class);
    }

    public static String getFormatVal(double val){
        return getFormatVal(val,"0.00");
    }

    public static String getFormatVal(double val,String formatStr){
        DecimalFormat decimalFormat = new DecimalFormat(formatStr);
        return decimalFormat.format(val);
    }

    /*判断服务是否在运行*/
    public static boolean isServiceRunning(Context ctx,String serviceName){
        boolean isRunning = false;
        if(ctx == null || serviceName == null){
            return isRunning;
        }
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        Iterator iterator = serviceList.iterator();
        while (iterator.hasNext()){
            ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) iterator.next();
            if(serviceName.trim().equals(runningServiceInfo.service.getClassName())){
                isRunning = true;
                return isRunning;
            }
        }
        return isRunning;
    }

    public static double round(double value, int scale, int roundingMode)
    {
        BigDecimal bigData = new BigDecimal(value);
        bigData = bigData.setScale(scale, roundingMode);
        double dv = bigData.doubleValue();
        bigData = null;
        return dv;
    }
}
