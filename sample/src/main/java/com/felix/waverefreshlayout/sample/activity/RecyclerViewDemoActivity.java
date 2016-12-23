package com.felix.waverefreshlayout.sample.activity;

import android.os.Handler;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.felix.waverefreshlayout.library.WaveRefreshLayout;
import com.felix.waverefreshlayout.sample.R;
import com.felix.waverefreshlayout.sample.adapter.RecyclerViewDemoAdapter;
import com.felix.waverefreshlayout.sample.util.BaseActivity;

import java.lang.ref.WeakReference;

/**
 * @author Felix
 */
public class RecyclerViewDemoActivity extends BaseActivity {

    private static final int SIZE_OF_DATA = 64;
    private String[] mData = new String[SIZE_OF_DATA];
    private RecyclerView rvDemo;
    private WaveRefreshLayout waveRefreshLayout;
    private Handler mHandler = new CustomHandler(this);
    private int mRefreshTime;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            final String[] data = mData;
            mRefreshTime++;
            for (int i = 0; i < data.length; i++) {
                data[i] = "Item:" + i + "\nrefresh:" + mRefreshTime;
            }
            waveRefreshLayout.finishRefresh();
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_recycler_view_demo;
    }

    @Override
    protected void initView() {
        waveRefreshLayout = (WaveRefreshLayout) findViewById(R.id.wave_refresh_layout);
        rvDemo = (RecyclerView) findViewById(R.id.rv_demo);
    }

    @Override
    protected void initData() {
        for (int i = 0; i < SIZE_OF_DATA; i++) {
            mData[i] = "Item: " + i;
        }
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        rvDemo.setLayoutManager(manager);
        RecyclerViewDemoAdapter adapter = new RecyclerViewDemoAdapter(mData);
        rvDemo.setAdapter(adapter);
        rvDemo.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvDemo.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
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
                rvDemo.getAdapter().notifyDataSetChanged();
            }
        });
    }

    private static class CustomHandler extends Handler {
        private WeakReference<RecyclerViewDemoActivity> reference;
        private int refreshTime;

        public CustomHandler(RecyclerViewDemoActivity activity) {
            reference = new WeakReference<>(activity);
        }
    }
}
