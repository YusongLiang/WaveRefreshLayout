package com.felix.waverefreshlayout.sample.util;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Felix
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());
        getWindow().setBackgroundDrawable(null);
        initView();
        initData();
        initListener();
    }

    protected abstract int getLayoutResID();

    protected void initView() {
    }

    protected void initData() {
    }

    protected void initListener() {
    }
}
