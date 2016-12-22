package com.felix.waverefreshlayout.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.felix.waverefreshlayout.library.WaveRefreshLayout;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private WaveRefreshLayout waveRefreshLayout;

    private Handler mHandler = new CustomHandler(this);
    private ListView lvTest;
    private String[] mData;
    private ArrayAdapter<String> mAdapter;
    private int mId;
    private boolean isCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawable(null);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        waveRefreshLayout = (WaveRefreshLayout) findViewById(R.id.wave_refresh_layout);
        lvTest = (ListView) findViewById(R.id.lv_test);
    }

    private void initData() {
        mData = new String[10];
        for (int i = 0; i < 10; i++) {
            mData[i] = "Item:" + i;
        }
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mData);
        lvTest.setAdapter(mAdapter);
    }

    private void initListener() {
        waveRefreshLayout.setOnRefreshListener(new WaveRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                isCancel = false;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isCancel) {
                            mId++;
                            waveRefreshLayout.finishRefresh();
                            for (int i = 0; i < 10; i++) {
                                mData[i] = "Item:" + i + "    refresh:" + mId + " times";
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }, 1000);
            }

            @Override
            public void onCancel() {
                isCancel = true;
            }
        });
    }

    private static class CustomHandler extends Handler {

        private WeakReference<MainActivity> reference;

        public CustomHandler(MainActivity activity) {
            reference = new WeakReference<>(activity);
        }
    }
}
