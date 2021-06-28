package com.lxsoft.stepapp;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxsoft.adapter.PedometerAdapter;
import com.lxsoft.bean.PedometerBean;
import com.lxsoft.db.DBHelper;
import com.lxsoft.frame.BaseActivity;
import com.lxsoft.view.MyDecoration;


import java.util.ArrayList;
import java.util.List;

import win.smartown.android.library.tableLayout.TableAdapter;
import win.smartown.android.library.tableLayout.TableLayout;

public class HistoryActivity2 extends BaseActivity {


    private ImageView back;
    private RecyclerView recyclerView;
    private PedometerBean pedometerBean;
    private List<PedometerBean> dataList = new ArrayList<>();



    @Override
    protected void onInitVariable() {

    }

    @Override
    protected void onInitView(Bundle savedInstanceState) {
        setContentView(R.layout.act_history2);
        back = findViewById(R.id.imageView3);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        DBHelper dbHelper = new DBHelper(HistoryActivity2.this, DBHelper.DB_NAME);
        dataList = dbHelper.getFromDatabase();
        RecyclerView recyclerView = findViewById(R.id.history2);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        PedometerAdapter adapter = new PedometerAdapter(dataList);
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onRequestData() {

    }

    @Override
    protected void onDestory() {
        super.onDestory();
    }

    class GridLayoutItemDecoration extends RecyclerView.ItemDecoration {
        private Context context;
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
        {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.dividerHeight));
        }
    }
}
