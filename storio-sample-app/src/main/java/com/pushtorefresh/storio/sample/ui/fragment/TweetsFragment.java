package com.pushtorefresh.storio.sample.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pushtorefresh.storio.sample.R;
import com.pushtorefresh.storio.sample.SampleApp;
import com.pushtorefresh.storio.sample.db.entity.Tweet;
import com.pushtorefresh.storio.sample.db.entity.TweetKV;
import com.pushtorefresh.storio.sample.db.table.TweetTableMeta;
import com.pushtorefresh.storio.sample.ui.DividerItemDecoration;
import com.pushtorefresh.storio.sample.ui.UiStateController;
import com.pushtorefresh.storio.sample.ui.adapter.TweetsAdapter;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.put.PutResults;
import com.squareup.wire.Wire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.pushtorefresh.storio.sample.ui.Toasts.safeShowShortToast;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class TweetsFragment extends BaseFragment {

    @Inject
    StorIOSQLite storIOSQLite;

    UiStateController uiStateController;

    @InjectView(R.id.tweets_recycler_view)
    RecyclerView recyclerView;

    TweetsAdapter tweetsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SampleApp.get(getActivity()).appComponent().inject(this);
        tweetsAdapter = new TweetsAdapter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tweets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(tweetsAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        uiStateController = new UiStateController.Builder()
                .withLoadingUi(view.findViewById(R.id.tweets_loading_ui))
                .withErrorUi(view.findViewById(R.id.tweets_error_ui))
                .withEmptyUi(view.findViewById(R.id.tweets_empty_ui))
                .withContentUi(recyclerView)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadData();
        //reloadDataKV();
    }

    void reloadData() {
        uiStateController.setUiStateLoading();

        final Subscription subscription = storIOSQLite
                .get()
                .listOfObjects(Tweet.class)
                .withQuery(TweetTableMeta.QUERY_ALL)
                .prepare()
                .createObservable() // it will be subscribed to changes in tweets table!
                .delay(1, SECONDS) // for better User Experience :) Actually, StorIO is so fast that we need to delay emissions (it's a joke, or not)
                .observeOn(mainThread())
                .subscribe(new Action1<List<Tweet>>() {
                    @Override
                    public void call(List<Tweet> tweets) {
                        if (tweets.isEmpty()) {
                            uiStateController.setUiStateEmpty();
                            tweetsAdapter.setTweets(null);
                        } else {
                            uiStateController.setUiStateContent();
                            tweetsAdapter.setTweets(tweets);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "reloadData()");
                        uiStateController.setUiStateError();
                        tweetsAdapter.setTweets(null);
                    }
                });

        unsubscribeOnStop(subscription); // preventing memory leak
    }


    @Inject
    Wire wire;

    void reloadDataKV() {
        uiStateController.setUiStateLoading();

        final Subscription subscription = storIOSQLite
                .get()
                .listOfObjects(TweetKV.class)
                .withQuery(TweetTableMeta.QUERY_KV_ALL)
                .prepare()
                .createObservable() // it will be subscribed to changes in tweets table!
                .flatMap(new Func1<List<TweetKV>, Observable<List<Tweet>>>() {
                    @Override
                    public Observable<List<Tweet>> call(List<TweetKV> tweetKVs) {
                        List<Tweet> tweets = new ArrayList<Tweet>();
                        for (TweetKV tweetKV : tweetKVs) {
                            try {
                                tweets.add(wire.parseFrom(tweetKV.getBlobField(), Tweet.class));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        return Observable.just(tweets);
                    }
                })
                        //   .delay(1, SECONDS) // for better User Experience :) Actually, StorIO is so fast that we need to delay emissions (it's a joke, or not)
                .observeOn(mainThread())
                .subscribe(new Action1<List<Tweet>>() {
                    @Override
                    public void call(List<Tweet> tweets) {
                        if (tweets.isEmpty()) {
                            uiStateController.setUiStateEmpty();
                            tweetsAdapter.setTweets(null);
                        } else {
                            uiStateController.setUiStateContent();
                            tweetsAdapter.setTweets(tweets);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "reloadData()");
                        uiStateController.setUiStateError();
                        tweetsAdapter.setTweets(null);
                    }
                });

        unsubscribeOnStop(subscription); // preventing memory leak
    }

    @OnClick(R.id.tweets_empty_ui_add_tweets_button)
    void addTweets() {
        final List<Tweet> tweets = new ArrayList<Tweet>();

        Observable.range(1, 5000)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        tweets.add(Tweet.newTweet("artem_zin", "Checkout StorIO — modern API for SQLiteDatabase & ContentResolver", "Content1", "Content2"));
                    }
                });


//        tweets.add(Tweet.newTweet("artem_zin", "Checkout StorIO — modern API for SQLiteDatabase & ContentResolver"));
//        tweets.add(Tweet.newTweet("HackerNews", "It's revolution! Dolphins can write news on HackerNews with our new app!"));
//        tweets.add(Tweet.newTweet("AndroidDevReddit", "Awesome library — StorIO"));
//        tweets.add(Tweet.newTweet("Facebook", "Facebook community in Twitter is more popular than Facebook community in Facebook and Instagram!"));
//        tweets.add(Tweet.newTweet("Google", "Android be together not the same: AOSP, AOSP + Google Apps, Samsung Android"));
//        tweets.add(Tweet.newTweet("Reddit", "Now we can send funny gifs directly into your brain via Oculus Rift app!"));
//        tweets.add(Tweet.newTweet("ElonMusk", "Tesla Model S OTA update with Android Auto 5.2, fixes for memory leaks"));
//        tweets.add(Tweet.newTweet("AndroidWeekly", "Special issue #1: StorIO — forget about SQLiteDatabase, ContentResolver APIs, ORMs sucks!"));
//        tweets.add(Tweet.newTweet("Apple", "Yosemite update: fixes for Wifi issues, yosemite-wifi-patch#142"));


//        storIOSQLite
//                .put()
//                .objects(tweets)
//                .prepare()
//                .createObservable()
//                .subscribeOn(Schedulers.io())
//                .observeOn(mainThread())
//                .subscribe(new Observer<PutResults<Tweet>>() {
//                    @Override
//                    public void onError(Throwable e) {
//                        safeShowShortToast(getActivity(), R.string.tweets_add_error_toast);
//                    }
//
//                    @Override
//                    public void onNext(PutResults<Tweet> putResults) {
//                        safeShowShortToast(getActivity(), "Added to default: " + putResults.results().size());
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        // no impl
//                    }
//                });

        //////////////KV//////////////////////////
        ArrayList<TweetKV> tweetKVs = new ArrayList<TweetKV>();
        for (Tweet tweet : tweets) {
            tweetKVs.add(new TweetKV(tweet.getByteArray()));
        }

        storIOSQLite.put()
                .objects(tweetKVs)
                .prepare()
                .createObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe(new Observer<PutResults<TweetKV>>() {
                    @Override
                    public void onError(Throwable e) {
                        safeShowShortToast(getActivity(), R.string.tweets_add_error_toast);
                    }

                    @Override
                    public void onNext(PutResults<TweetKV> putResults) {

                        safeShowShortToast(getActivity(), "Added to KV: " + putResults.results().size());
                        // handled via reactive stream! see reloadData()
                    }

                    @Override
                    public void onCompleted() {
                        // no impl
                    }
                });
    }

}
