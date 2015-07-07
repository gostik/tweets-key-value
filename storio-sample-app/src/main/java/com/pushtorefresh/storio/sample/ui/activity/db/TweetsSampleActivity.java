package com.pushtorefresh.storio.sample.ui.activity.db;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.pushtorefresh.storio.sample.R;
import com.pushtorefresh.storio.sample.ui.activity.BaseActivity;

public class TweetsSampleActivity extends BaseActivity implements SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets_sample);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem sortAbMenuItem = menu.findItem(R.id.action_sort);


        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                SearchView mSearchView = (SearchView) item.getActionView();
                mSearchView.requestFocus();
                break;
            case R.id.action_sort:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
