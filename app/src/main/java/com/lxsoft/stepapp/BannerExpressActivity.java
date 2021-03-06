package com.lxsoft.stepapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DislikeInfo;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.PersonalizationPrompt;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.lxsoft.bean.PedometerChartBean;
import com.lxsoft.dialog.DislikeDialog;
import com.lxsoft.frame.BaseActivity;
import com.lxsoft.frame.LogWriter;
import com.lxsoft.service.IPedometerService;
import com.lxsoft.service.PedometerService;
import com.lxsoft.utiles.TTAdManagerHolder;
import com.lxsoft.utiles.TToast;
import com.lxsoft.utiles.Utiles;
import com.lxsoft.view.LoadMoreRecyclerView;
import com.lxsoft.widgets.CircleProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * created by wuzejian on 2019-12-22
 */
@SuppressWarnings("unused")
public class BannerExpressActivity extends Activity {

    private TTAdNative mTTAdNative;
    private FrameLayout mExpressContainer;
    private Context mContext;
    private TTAdDislike mTTAdDislike;
    private TTNativeExpressAd mTTAd;
    private LoadMoreRecyclerView mListView;
    private List<AdSizeModel> mBannerAdSizeModelList;
    private Button showBanner;
    private long startTime = 0;
    private boolean mHasShowDownloadActive = false;


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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_home);
        mContext = this.getApplicationContext();
        initView();
        initData();
        initRecycleView();
        initTTSDKConfig();

    }

//    @Override
//    protected void onInitVariable() {
//
//    }
//
//    @Override
//    protected void onInitView(Bundle savedInstanceState) {
//        setContentView(R.layout.act_home);
//        progressBar = (CircleProgressBar) findViewById(R.id.progressBar);
//        progressBar.setProgress(0);
//        progressBar.setMaxProgress(10000);
//        setting = (ImageView) findViewById(R.id.imageView);
//        textCalorie = (TextView) findViewById(R.id.textCalorie);
//        time = (TextView) findViewById(R.id.time);
//        distance = (TextView) findViewById(R.id.distance);
//        stepCount = (TextView) findViewById(R.id.stepCount);
//        reset = (Button) findViewById(R.id.reset);
//        btnStart = (Button) findViewById(R.id.btnStart);
//        dataChart = (BarChart) findViewById(R.id.datachart);
//        setting.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setClass(BannerExpressActivity.this, SettingActivity.class);
//                startActivity(intent);
//            }
//        });
//        reset.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(BannerExpressActivity.this);
//                builder.setTitle("????????????");
//                builder.setMessage("??????????????????????????????????????????");
//                builder.setPositiveButton("??????", new DialogInterface.OnClickListener(){
//
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if(remoteService != null){
//                            try {
//                                remoteService.stopCount();
//                                remoteService.resetCount();
//                                chartBean = remoteService.getCharData();
//                                updateChart(chartBean);
//                                stepCount.setText("0???");
//                                status = remoteService.getServiceStatus();
//                                if(status == PedometerService.STATUS_RUNNING){
//                                    btnStart.setText("??????");
//                                }else if(status == PedometerService.STATUS_NOT_RUN){
//                                    btnStart.setText("??????");
//                                }
//                            } catch (RemoteException e) {
//                                LogWriter.d(e.toString());
//                            }
//                        }
//                        dialogInterface.dismiss();
//                    }
//                });
//                builder.setNegativeButton("??????",null );
//
//                AlertDialog resetDlg = builder.create();
//                resetDlg.show();
//            }
//        });
//        btnStart.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                try {
//                    status = remoteService.getServiceStatus();
//                } catch (RemoteException e) {
//                    LogWriter.d(e.toString());
//                }
//                if(status == STATUS_RUMMING && remoteService != null){
//                    try {
//                        remoteService.stopCount();
//                        btnStart.setText("??????");
//                        isRunning = false;
//                        isChartUpdate = false;
//                    } catch (RemoteException e) {
//                        LogWriter.d(e.toString());
//                    }
//                }else if(status == STATUS_NOT_RUNNING && remoteService != null){
//                    try {
//                        remoteService.startCount();
//                        startStepCount();
//                    } catch (RemoteException e) {
//                        LogWriter.d(e.toString());
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void onRequestData() {
//        //????????????????????????
//        //?????????????????????????????????????????????????????????????????????????????????
//        Intent serviceIntent1 =null;
//        if(!Utiles.isServiceRunning(this, PedometerService.class.getName())){
//            //?????????????????????????????????
//            serviceIntent1 = new Intent(this,PedometerService.class);
//            startService(serviceIntent1);
//        }else{
//            //????????????
//            serviceIntent1 = new Intent(this,PedometerService.class);
//            serviceIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        //??????????????????
//        bindService = bindService(serviceIntent1, serviceConnection, BIND_AUTO_CREATE);
//        //?????????????????????????????????????????????
//        if(bindService && remoteService != null){
//            try {
//                status = remoteService.getServiceStatus();
//                if(status == PedometerService.STATUS_NOT_RUN){
//                    btnStart.setText("??????");
//                }else if(status == PedometerService.STATUS_RUNNING){
//                    btnStart.setText("??????");
//                    isRunning = true;
//                    isChartUpdate = true;
//                    //????????????????????????????????????????????????UI
//                    new Thread(new StepRunnable()).start();
//                    new Thread(new ChartRunable()).start();
//                }
//            } catch (RemoteException e) {
//                LogWriter.e(e.toString());
//            }
//        }else{
//            btnStart.setText("??????");
//        }
//    }


    private void initTTSDKConfig() {
        //step2:??????TTAdNative?????????createAdNative(Context context) banner??????context????????????Activity??????
        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        //step3:(?????????????????????????????????????????????):????????????????????????read_phone_state,??????????????????imei????????????????????????????????????????????????
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
    }

    private void initRecycleView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        mListView.setLayoutManager(layoutManager);
        AdapterForBannerType adapterForBannerType = new AdapterForBannerType(this, mBannerAdSizeModelList);
        mListView.setAdapter(adapterForBannerType);

    }

    private void initView() {
        mExpressContainer = (FrameLayout) findViewById(R.id.banner);
        mListView = findViewById(R.id.my_list);
        showBanner = findViewById(R.id.showBanner);
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
                intent.setClass(BannerExpressActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BannerExpressActivity.this);
                builder.setTitle("????????????");
                builder.setMessage("??????????????????????????????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(remoteService != null){
                            try {
                                remoteService.stopCount();
                                remoteService.resetCount();
                                chartBean = remoteService.getCharData();
                                updateChart(chartBean);
                                stepCount.setText("0???");
                                status = remoteService.getServiceStatus();
                                if(status == PedometerService.STATUS_RUNNING){
                                    btnStart.setText("??????");
                                }else if(status == PedometerService.STATUS_NOT_RUN){
                                    btnStart.setText("??????");
                                }
                            } catch (RemoteException e) {
                                LogWriter.d(e.toString());
                            }
                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("??????",null );

                AlertDialog resetDlg = builder.create();
                resetDlg.show();
            }
        });
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickShowBanner();
            }
        });
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTTAd != null) {
                    mTTAd.render();
                } else {
//            TToast.show(mContext, "??????????????????..");
                }
                try {
                    status = remoteService.getServiceStatus();
                } catch (RemoteException e) {
                    LogWriter.d(e.toString());
                }
                if(status == STATUS_RUMMING && remoteService != null){
                    try {
                        remoteService.stopCount();
                        btnStart.setText("??????");
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
//        btnStart.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                onClickShowBanner();
//                try {
//                    status = remoteService.getServiceStatus();
//                } catch (RemoteException e) {
//                    LogWriter.d(e.toString());
//                }
//                if(status == STATUS_RUMMING && remoteService != null){
//                    try {
//                        remoteService.stopCount();
//                        btnStart.setText("??????");
//                        isRunning = false;
//                        isChartUpdate = false;
//                    } catch (RemoteException e) {
//                        LogWriter.d(e.toString());
//                    }
//                }else if(status == STATUS_NOT_RUNNING && remoteService != null){
//                    try {
//                        remoteService.startCount();
//                        startStepCount();
//                    } catch (RemoteException e) {
//                        LogWriter.d(e.toString());
//                    }
//                }
//            }
//        });
    }

    private void initData() {
        mBannerAdSizeModelList = new ArrayList<>();
        mBannerAdSizeModelList.add(new AdSizeModel("690*388", 345, 194, "946236413"));
        //????????????????????????
        //?????????????????????????????????????????????????????????????????????????????????
        Intent serviceIntent =null;
        if(!Utiles.isServiceRunning(this, PedometerService.class.getName())){
            //?????????????????????????????????
            serviceIntent = new Intent(this,PedometerService.class);
            startService(serviceIntent);
        }else{
            //????????????
            serviceIntent = new Intent(this,PedometerService.class);
            serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        //??????????????????
        bindService = bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        //?????????????????????????????????????????????
        if(bindService && remoteService != null){
            try {
                status = remoteService.getServiceStatus();
                if(status == PedometerService.STATUS_NOT_RUN){
                    btnStart.setText("??????");
                }else if(status == PedometerService.STATUS_RUNNING){
                    btnStart.setText("??????");
                    isRunning = true;
                    isChartUpdate = true;
                    //????????????????????????????????????????????????UI
                    new Thread(new StepRunnable()).start();
                    new Thread(new ChartRunable()).start();
                }
            } catch (RemoteException e) {
                LogWriter.e(e.toString());
            }
        }else{
            btnStart.setText("??????");
        }
    }


    public static class AdapterForBannerType extends RecyclerView.Adapter<AdapterForBannerType.ViewHolder> {
        private List<AdSizeModel> mBannerSizeList;
        private BannerExpressActivity mActivity;

        public AdapterForBannerType(BannerExpressActivity activity, List<AdSizeModel> bannerSize) {
            this.mActivity = activity;
            this.mBannerSizeList = bannerSize;
        }

        @NonNull
        @Override
        public AdapterForBannerType.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.express_banner_list_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdapterForBannerType.ViewHolder viewHolder, int i) {
            final AdSizeModel bannerSize = mBannerSizeList == null ? null : mBannerSizeList.get(i);
            if (bannerSize != null) {
                viewHolder.btnSize.setText(bannerSize.adSizeName);
                mActivity.loadExpressAd(bannerSize.codeId, bannerSize.width, bannerSize.height);
//                viewHolder.btnSize.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        //??????banner??????
//                        mActivity.loadExpressAd(bannerSize.codeId, bannerSize.width, bannerSize.height);
//                    }
//                });
            }
        }

        @Override
        public int getItemCount() {
            return mBannerSizeList != null ? mBannerSizeList.size() : 0;
        }


        public static class ViewHolder extends RecyclerView.ViewHolder {
            private Button btnSize;

            public ViewHolder(View view) {
                super(view);
                btnSize = view.findViewById(R.id.btn_banner_size);
            }

        }
    }

    private void loadExpressAd(String codeId, int expressViewWidth, int expressViewHeight) {
        mExpressContainer.removeAllViews();
        //step4:????????????????????????AdSlot,??????????????????????????????
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("946236413") //?????????id
                .setAdCount(3) //?????????????????????1???3???
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //??????????????????view???size,??????dp
                .build();
        //step5:??????????????????????????????????????????????????????
        mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
//                TToast.show(BannerExpressActivity.this, "load error : " + code + ", " + message);
                mExpressContainer.removeAllViews();
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    return;
                }
                mTTAd = ads.get(0);
                mTTAd.setSlideIntervalTime(30 * 1000);
                bindAdListener(mTTAd);
                startTime = System.currentTimeMillis();
//                TToast.show(mContext,"load success!");
            }
        });
    }

    public void onClickShowBanner() {
        if (mTTAd != null) {
            mTTAd.render();
        } else {
//            TToast.show(mContext, "??????????????????..");
        }
    }


    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
//                TToast.show(mContext, "???????????????");
            }

            @Override
            public void onAdShow(View view, int type) {
//                TToast.show(mContext, "????????????");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                LogWriter.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
//                TToast.show(mContext, msg + " code:" + code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                LogWriter.e("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //??????view????????? ?????? dp
//                TToast.show(mContext, "????????????");
                mExpressContainer.removeAllViews();
                mExpressContainer.addView(view);
            }
        });
        //dislike??????
        bindDislike(ad, false);
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
//                TToast.show(BannerExpressActivity.this, "??????????????????", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
//                    TToast.show(BannerExpressActivity.this, "????????????????????????", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "???????????????????????????", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "?????????????????????????????????", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "?????????????????????????????????", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "????????????", Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * ????????????????????????, ??????????????????????????????????????????????????????dislike???????????????????????????????????? dislike???????????????dislike?????????
     *
     * @param ad
     * @param customStyle ????????????????????????true:???????????????
     */
    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (customStyle) {
            //?????????????????????
            final DislikeInfo dislikeInfo = ad.getDislikeInfo();
            if (dislikeInfo == null || dislikeInfo.getFilterWords() == null || dislikeInfo.getFilterWords().isEmpty()) {
                return;
            }
            final DislikeDialog dislikeDialog = new DislikeDialog(this, dislikeInfo);
            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
                @Override
                public void onItemClick(FilterWord filterWord) {
                    //????????????
//                    TToast.show(mContext, "?????? " + filterWord.getName());
                    //???????????????????????????????????????????????????
                    mExpressContainer.removeAllViews();
                }
            });
            dislikeDialog.setOnPersonalizationPromptClick(new DislikeDialog.OnPersonalizationPromptClick() {
                @Override
                public void onClick(PersonalizationPrompt personalizationPrompt) {
//                    TToast.show(mContext, "?????????????????????????????????");
                }
            });
            ad.setDislikeDialog(dislikeDialog);
            return;
        }
        //???????????????????????????dislike????????????
        ad.setDislikeCallback(BannerExpressActivity.this, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }


            @Override
            public void onSelected(int position, String value) {
//                TToast.show(mContext, "?????? " + value);
                mExpressContainer.removeAllViews();
                //???????????????????????????????????????????????????
            }

            @Override
            public void onCancel() {
//                TToast.show(mContext, "???????????? ");
            }

            @Override
            public void onRefuse() {

            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTTAd != null) {
            mTTAd.destroy();
        }
        if(bindService){
            bindService = false;
            isRunning = false;
            isChartUpdate = false;
            unbindService(serviceConnection);
        }
    }


    public static class AdSizeModel {
        public AdSizeModel(String adSizeName, int width, int height, String codeId) {
            this.adSizeName = adSizeName;
            this.width = width;
            this.height = height;
            this.codeId = codeId;
        }

        public String adSizeName;
        public int width;
        public int height;
        public String codeId;
    }

    private class StepRunnable implements Runnable{

        @Override
        public void run() {
            while (isRunning){
                try {
                    status = remoteService.getServiceStatus();
                    if(status == STATUS_RUMMING){
                        handler.removeMessages(MESSAGE_UPDATE_STEP_COUNT);
                        //??????????????????Handler???????????????
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
                    //??????????????????
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
            //???????????????UI
            stepCount.setText(String.valueOf(stepCountVal)+"???");
            textCalorie.setText(com.lxsoft.utiles.Utiles.getFormatVal(calorieVal)+"???");
            distance.setText(com.lxsoft.utiles.Utiles.getFormatVal(distanceVal));
            progressBar.setProgress(stepCountVal);
        }
    }


    public void updateChart(PedometerChartBean bean){
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
        if(bean != null){
            for(int i=0;i<=bean.getIndex();i++){
                xVals.add(String.valueOf(i)+"???");
                int valY = bean.getArrayData()[i];
                yVals.add(new BarEntry(valY, i));
            }
            time.setText(String.valueOf(bean.getIndex())+"???");
            BarDataSet set1 = new BarDataSet(yVals, "???????????????");
            set1.setBarSpacePercent(2f);
            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);
            BarData data = new BarData(xVals,dataSets);
            data.setValueTextSize(10f);
            dataChart.setData(data);
            dataChart.invalidate();
        }
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
                    btnStart.setText("??????");
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
        btnStart.setText("??????");
        stepCount.setText("0???");
        isChartUpdate = true;
        isRunning = true;
        //????????????????????????????????????????????????UI
        new Thread(new StepRunnable()).start();
        new Thread(new ChartRunable()).start();
        chartBean = remoteService.getCharData();
        updateChart(chartBean);
    }
}
