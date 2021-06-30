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
//                builder.setTitle("确认重置");
//                builder.setMessage("您的记录将要被清除，确定吗？");
//                builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
//
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if(remoteService != null){
//                            try {
//                                remoteService.stopCount();
//                                remoteService.resetCount();
//                                chartBean = remoteService.getCharData();
//                                updateChart(chartBean);
//                                stepCount.setText("0步");
//                                status = remoteService.getServiceStatus();
//                                if(status == PedometerService.STATUS_RUNNING){
//                                    btnStart.setText("停止");
//                                }else if(status == PedometerService.STATUS_NOT_RUN){
//                                    btnStart.setText("启动");
//                                }
//                            } catch (RemoteException e) {
//                                LogWriter.d(e.toString());
//                            }
//                        }
//                        dialogInterface.dismiss();
//                    }
//                });
//                builder.setNegativeButton("取消",null );
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
//                        btnStart.setText("启动");
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
//        //检查服务是否运行
//        //服务没有运行，启动服务，如果服务已经运行，直接绑定服务
//        Intent serviceIntent1 =null;
//        if(!Utiles.isServiceRunning(this, PedometerService.class.getName())){
//            //服务没有运行，启动服务
//            serviceIntent1 = new Intent(this,PedometerService.class);
//            startService(serviceIntent1);
//        }else{
//            //服务运行
//            serviceIntent1 = new Intent(this,PedometerService.class);
//            serviceIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        //绑定服务操作
//        bindService = bindService(serviceIntent1, serviceConnection, BIND_AUTO_CREATE);
//        //初始化一些对应状态，按钮文字等
//        if(bindService && remoteService != null){
//            try {
//                status = remoteService.getServiceStatus();
//                if(status == PedometerService.STATUS_NOT_RUN){
//                    btnStart.setText("启动");
//                }else if(status == PedometerService.STATUS_RUNNING){
//                    btnStart.setText("停止");
//                    isRunning = true;
//                    isChartUpdate = true;
//                    //启动两个线程，定时获取数据，刷新UI
//                    new Thread(new StepRunnable()).start();
//                    new Thread(new ChartRunable()).start();
//                }
//            } catch (RemoteException e) {
//                LogWriter.e(e.toString());
//            }
//        }else{
//            btnStart.setText("启动");
//        }
//    }


    private void initTTSDKConfig() {
        //step2:创建TTAdNative对象，createAdNative(Context context) banner广告context需要传入Activity对象
        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
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
//            TToast.show(mContext, "请先加载广告..");
                }
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
//                        btnStart.setText("启动");
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
//                        //请求banner广告
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
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("946236413") //广告位id
                .setAdCount(3) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
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
//            TToast.show(mContext, "请先加载广告..");
        }
    }


    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
//                TToast.show(mContext, "广告被点击");
            }

            @Override
            public void onAdShow(View view, int type) {
//                TToast.show(mContext, "广告展示");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                LogWriter.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
//                TToast.show(mContext, msg + " code:" + code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                LogWriter.e("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
//                TToast.show(mContext, "渲染成功");
                mExpressContainer.removeAllViews();
                mExpressContainer.addView(view);
            }
        });
        //dislike设置
        bindDislike(ad, false);
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
//                TToast.show(BannerExpressActivity.this, "点击开始下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
//                    TToast.show(BannerExpressActivity.this, "下载中，点击暂停", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "下载暂停，点击继续", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "下载失败，点击重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "安装完成，点击图片打开", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "点击安装", Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * 设置广告的不喜欢, 注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     *
     * @param ad
     * @param customStyle 是否自定义样式，true:样式自定义
     */
    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (customStyle) {
            //使用自定义样式
            final DislikeInfo dislikeInfo = ad.getDislikeInfo();
            if (dislikeInfo == null || dislikeInfo.getFilterWords() == null || dislikeInfo.getFilterWords().isEmpty()) {
                return;
            }
            final DislikeDialog dislikeDialog = new DislikeDialog(this, dislikeInfo);
            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
                @Override
                public void onItemClick(FilterWord filterWord) {
                    //屏蔽广告
//                    TToast.show(mContext, "点击 " + filterWord.getName());
                    //用户选择不喜欢原因后，移除广告展示
                    mExpressContainer.removeAllViews();
                }
            });
            dislikeDialog.setOnPersonalizationPromptClick(new DislikeDialog.OnPersonalizationPromptClick() {
                @Override
                public void onClick(PersonalizationPrompt personalizationPrompt) {
//                    TToast.show(mContext, "点击了为什么看到此广告");
                }
            });
            ad.setDislikeDialog(dislikeDialog);
            return;
        }
        //使用默认模板中默认dislike弹出样式
        ad.setDislikeCallback(BannerExpressActivity.this, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }


            @Override
            public void onSelected(int position, String value) {
//                TToast.show(mContext, "点击 " + value);
                mExpressContainer.removeAllViews();
                //用户选择不喜欢原因后，移除广告展示
            }

            @Override
            public void onCancel() {
//                TToast.show(mContext, "点击取消 ");
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
        stepCount.setText("0步");
        isChartUpdate = true;
        isRunning = true;
        //启动两个线程，定时获取数据，刷新UI
        new Thread(new StepRunnable()).start();
        new Thread(new ChartRunable()).start();
        chartBean = remoteService.getCharData();
        updateChart(chartBean);
    }
}
