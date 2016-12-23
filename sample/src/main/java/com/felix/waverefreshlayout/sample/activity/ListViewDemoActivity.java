package com.felix.waverefreshlayout.sample.activity;

import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.felix.waverefreshlayout.library.WaveRefreshLayout;
import com.felix.waverefreshlayout.sample.R;
import com.felix.waverefreshlayout.sample.util.BaseActivity;

import java.lang.ref.WeakReference;

/**
 * @author Felix
 */
public class ListViewDemoActivity extends BaseActivity {

    private static final int SIZE_OF_DATA = 30;
    private ListView lvDemo;
    private WaveRefreshLayout waveRefreshLayout;
    private String[] mData = new String[SIZE_OF_DATA];
    private Handler mHandler = new CustomHandler(this);
    private ArrayAdapter<String> mAdapter;
    private int mRefreshTime;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            final String[] data = mData;
            mRefreshTime++;
            for (int i = 0; i < data.length; i++) {
                data[i] = "Item:" + i + " ,refresh:" + mRefreshTime;
            }
            waveRefreshLayout.finishRefresh();
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_list_view_demo;
    }

    @Override
    protected void initView() {
        lvDemo = (ListView) findViewById(R.id.lv_demo);
        waveRefreshLayout = (WaveRefreshLayout) findViewById(R.id.wave_refresh_layout);
    }

    @Override
    protected void initData() {
        for (int i = 0; i < SIZE_OF_DATA; i++) {
            mData[i] = "Item: " + i;
        }
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mData);
        lvDemo.setAdapter(mAdapter);
    }

    @Override
    protected void initListener() {
        waveRefreshLayout.setOnRefreshListener(new WaveRefreshLayout.OnRefreshListener() {
            @Override
            public void onAcquireData() {
                mHandler.postDelayed(mRunnable, 1000);
            }

            @Override
            public void onLoadData() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private static class CustomHandler extends Handler {
        private WeakReference<ListViewDemoActivity> reference;

        public CustomHandler(ListViewDemoActivity activity) {
            reference = new WeakReference<>(activity);
        }
    }
}
