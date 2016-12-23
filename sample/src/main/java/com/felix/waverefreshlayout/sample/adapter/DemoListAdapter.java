package com.felix.waverefreshlayout.sample.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.felix.waverefreshlayout.sample.R;
import com.felix.waverefreshlayout.sample.activity.ListViewDemoActivity;
import com.felix.waverefreshlayout.sample.activity.NestedScrollDemoActivity;
import com.felix.waverefreshlayout.sample.activity.RecyclerViewDemoActivity;
import com.felix.waverefreshlayout.sample.activity.ScrollViewDemoActivity;

/**
 * @author Felix
 */
public class DemoListAdapter extends RecyclerView.Adapter<DemoListAdapter.ViewHolder> {

    private String[] mNames = {
            "RecyclerView",
            "ListView",
            "ScrollView",
            "Nested Scroll"
    };

    private Class[] mClasses = {
            RecyclerViewDemoActivity.class,
            ListViewDemoActivity.class,
            ScrollViewDemoActivity.class,
            NestedScrollDemoActivity.class
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_demo, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.btName.setText(mNames[position]);
        holder.btName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                context.startActivity(new Intent(context, mClasses[holder.getAdapterPosition()]));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView btName;

        public ViewHolder(View itemView) {
            super(itemView);
            btName = (Button) itemView.findViewById(R.id.bt_message);
        }
    }

}
