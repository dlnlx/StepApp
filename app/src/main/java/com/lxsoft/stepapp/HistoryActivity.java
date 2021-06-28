package com.lxsoft.stepapp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.preload.geckox.model.UpdatePackage;
import com.lxsoft.bean.PedometerBean;
import com.lxsoft.db.DBHelper;
import com.lxsoft.frame.BaseActivity;
import com.lxsoft.frame.LogWriter;
import com.lxsoft.service.IPedometerService;
import com.lxsoft.service.PedometerService;
import com.lxsoft.utiles.Settings;
import com.lxsoft.utiles.Utiles;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import win.smartown.android.library.tableLayout.TableAdapter;
import win.smartown.android.library.tableLayout.TableLayout;

public class HistoryActivity extends BaseActivity {


    private TableLayout tableLayout;
    private ImageView back;
    private PedometerBean pedometerBean;
    private List<PedometerBean> dataList;



    @Override
    protected void onInitVariable() {

    }

    @Override
    protected void onInitView(Bundle savedInstanceState) {
        setContentView(R.layout.act_history);
        tableLayout = (TableLayout) findViewById(R.id.history);
        back = findViewById(R.id.imageView2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        DBHelper dbHelper = new DBHelper(HistoryActivity.this, DBHelper.DB_NAME);
        dataList = dbHelper.getFromDatabase();
        tableLayout.setAdapter(new TableAdapter() {
            @Override
            public int getColumnCount() {
                return dataList.size();
            }

            @Override
            public String[] getColumnContent(int position) {
                return dataList.get(position).toArray();
            }
        });
    }


    @Override
    protected void onRequestData() {

    }

    @Override
    protected void onDestory() {
        super.onDestory();
    }

}
