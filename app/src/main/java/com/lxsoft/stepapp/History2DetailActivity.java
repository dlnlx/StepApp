package com.lxsoft.stepapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.gson.Gson;
import com.lxsoft.bean.PedometerBean;
import com.lxsoft.bean.PedometerChartBean;
import com.lxsoft.frame.BaseActivity;
import com.lxsoft.frame.FrameApplication;
import com.lxsoft.frame.LogWriter;
import com.lxsoft.utiles.ACache;
import com.lxsoft.utiles.Utiles;

import org.json.JSONArray;

import java.math.BigDecimal;
import java.util.ArrayList;

public class History2DetailActivity extends BaseActivity {

    private ImageView back;
    private PedometerBean pedometerBean;
    private BarChart dataChart;
    private PedometerChartBean pedometerChartBean;
    private TextView show_id;
    private TextView show_stepCount;
    private TextView show_calorie;
    private TextView show_distance;
    private TextView show_pace;
    private TextView show_speed;
    private TextView show_startTime;
    private TextView show_lastStepTime;
    private TextView show_day;
    private Gson gson;
    private SharedPreferences mSpf;
    @Override
    protected void onInitVariable() {

    }

    @Override
    protected void onInitView(Bundle savedInstanceState) {
        setContentView(R.layout.ac_history2_detail);
        back = findViewById(R.id.imageView4);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Intent intent = getIntent();
        pedometerBean = (PedometerBean)intent.getSerializableExtra("Pedometer");

        //获得系统的时间，单位为毫秒,转换为妙
        long startTime = pedometerBean.getStartTime();
        long totalSeconds = startTime / 1000;

//求出现在的秒
        long currentSecond = totalSeconds % 60;

//求出现在的分
        long totalMinutes = totalSeconds / 60;
        long currentMinute = totalMinutes % 60;

//求出现在的小时
        long totalHour = totalMinutes / 60;
        long currentHour = totalHour % 24;


        //获得系统的时间，单位为毫秒,转换为妙
        long lastStepTime = pedometerBean.getLastStepTime();
        long totalSeconds2 = lastStepTime / 1000;

//求出现在的秒
        long currentSecond2 = totalSeconds2 % 60;

//求出现在的分
        long totalMinutes2 = totalSeconds2 / 60;
        long currentMinute2 = totalMinutes2 % 60;

//求出现在的小时
        long totalHour2 = totalMinutes2 / 60;
        long currentHour2 = totalHour2 % 24;



        show_id = findViewById(R.id.show_id);
        show_id.setText(String.valueOf(pedometerBean.getId()));
        show_stepCount = findViewById(R.id.show_stepCount);
        show_stepCount.setText(String.valueOf(pedometerBean.getStepCount()));
        show_calorie = findViewById(R.id.show_calorie);
        show_calorie.setText(String.valueOf(Utiles.round(pedometerBean.getCalorie(),3, BigDecimal.ROUND_HALF_UP)));
        show_distance = findViewById(R.id.show_distance);
        show_distance.setText(String.valueOf(Utiles.round(pedometerBean.getDistance(),3,BigDecimal.ROUND_HALF_UP)));
        show_pace = findViewById(R.id.show_pace);
        show_pace.setText(String.valueOf(pedometerBean.getPace()));
        show_speed = findViewById(R.id.show_speed);
        show_speed.setText(String.valueOf(pedometerBean.getSpeed()));
        show_startTime = findViewById(R.id.show_startTime);
        show_startTime.setText(currentHour + ":" + currentMinute + ":" + currentSecond);
        show_lastStepTime = findViewById(R.id.show_lastStepTime);
        show_lastStepTime.setText(currentHour2 + ":" + currentMinute2 + ":" + currentSecond2);
        show_day = findViewById(R.id.show_day);
        show_day.setText(String.valueOf(pedometerBean.getDay()));
        dataChart = findViewById(R.id.datachart2);
        mSpf = super.getSharedPreferences(String.valueOf(pedometerBean.getId()),MODE_PRIVATE);
        String info = mSpf.getString(String.valueOf(pedometerBean.getId()),"");
        LogWriter.d("CHART", "读取id:"+String.valueOf(pedometerBean.getId()));
//        String info = mSpf.getString("test","");
        LogWriter.d("CHART", info);
        showChart(info);
    }

    @Override
    protected void onRequestData() {

    }

    public boolean showChart(String gson) {
        PedometerChartBean bean = Utiles.jsonToObj(gson);
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
        if(bean != null){
            for(int i=0;i<=bean.getIndex();i++){
                xVals.add(String.valueOf(i)+"分");
                int valY = bean.getArrayData()[i];
                yVals.add(new BarEntry(valY, i));
            }
            //time.setText(String.valueOf(bean.getIndex())+"分");
            BarDataSet set1 = new BarDataSet(yVals, "所走的步数");
            set1.setBarSpacePercent(2f);
            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);
            BarData data = new BarData(xVals,dataSets);
            data.setValueTextSize(10f);
            dataChart.setData(data);
            dataChart.invalidate();
            return true;
        }
        return false;
    }
}
