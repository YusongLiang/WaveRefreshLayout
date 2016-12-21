package com.felix.waverefreshlayout.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.felix.waverefreshlayout.library.WaveRefreshLayout;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private WaveRefreshLayout waveRefreshLayout;

    private Handler mHandler = new CustomHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawable(null);
        initView();
        initListener();
    }

    private void initView() {
        waveRefreshLayout = (WaveRefreshLayout) findViewById(R.id.wave_refresh_layout);
    }

    private void initListener() {
        waveRefreshLayout.setOnRefreshListener(new WaveRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waveRefreshLayout.finishRefresh();
                    }
                }, 3000);
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
