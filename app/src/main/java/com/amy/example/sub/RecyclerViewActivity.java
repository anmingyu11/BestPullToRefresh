package com.amy.example.sub;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amy.example.R;
import com.amy.inertia.widget.ARecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    private ARecyclerView mRecyclerView;

    //private PullToRefreshContainer mPullToRefreshContainer;

    private Context mContext;

    private Handler mHandler = new Handler();

    private List<String> mStrings = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.recycler_view_layout);
        setTitle("RecyclerView");

        /*PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.pull_to_refresh);
        pullToRefreshLayout.setEnableHeaderPullToRefresh(true);
        pullToRefreshLayout.addOnPullListener("sample", new PullListenerAdapter() {
            @Override
            public void footerRefresh(final PullToRefreshLayout pullToRefreshLayout) {
                super.footerRefresh(pullToRefreshLayout);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshLayout.finishFooterRefresh();
                    }
                }, 2000);
            }

            @Override
            public void headerRefresh(final PullToRefreshLayout pullToRefreshLayout) {
                super.headerRefresh(pullToRefreshLayout);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshLayout.finishHeaderRefresh();
                    }
                }, 2000);
            }
        });*/
        initRecyclerView();
    }

    private void initRecyclerView() {
        for (int i = 0; i < 20; i++) {
            mStrings.add("item  " + i);
        }
/*

        mPullToRefreshContainer = (PullToRefreshContainer) findViewById(R.id.container);
        final TopLoadingRefreshView headerView = new TopLoadingRefreshView(this);
        mPullToRefreshContainer.setHeaderView(headerView);
        mPullToRefreshContainer.addIPullListener(new PullListenerAdapter() {
            @Override
            public void onHeaderRefresh() {
                super.onHeaderRefresh();
                mPullToRefreshContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshContainer.finishHeaderRefresh();
                    }
                }, 2000);
            }
        });
        mPullToRefreshContainer.setScrollBackInterpolator(new ViscousInterpolator());
*/

        mRecyclerView = (ARecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {

            @Override
            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            public void onBindViewHolder(MyViewHolder holder, int position) {
                holder.mTextView.setText(mStrings.get(position));
            }

            @Override
            public int getItemCount() {
                return mStrings.size();
            }

        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRecyclerView.smoothScrollBy(0, 200);
                }
            });
        }
    }
}
