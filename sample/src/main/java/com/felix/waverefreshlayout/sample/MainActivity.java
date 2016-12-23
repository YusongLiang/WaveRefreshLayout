package com.felix.waverefreshlayout.sample;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.felix.waverefreshlayout.sample.adapter.DemoListAdapter;
import com.felix.waverefreshlayout.sample.util.BaseActivity;

public class MainActivity extends BaseActivity {

    private RecyclerView rvDemos;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        rvDemos = (RecyclerView) findViewById(R.id.rv_demo_list);
    }

    @Override
    protected void initData() {
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        rvDemos.setLayoutManager(manager);
        DemoListAdapter adapter = new DemoListAdapter();
        rvDemos.setAdapter(adapter);
    }
}
