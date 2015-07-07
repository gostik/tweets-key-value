package com.pushtorefresh.storio.sample.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sample.R;
import com.pushtorefresh.storio.sample.ui.activity.db.TweetsSampleActivity;
import com.pushtorefresh.storio.sample.ui.fragment.TweetsFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.main_db_sample)
    void openDbSample() {
        TweetsFragment.isKV=false;
        startActivity(new Intent(this, TweetsSampleActivity.class));
    }

    @OnClick(R.id.main_tweet_key_value)
    void openContentResolverSample() {
        TweetsFragment.isKV=true;
        startActivity(new Intent(this, TweetsSampleActivity.class));
    }
}
