package com.pushtorefresh.storio.sample.ui.fragment;

import android.app.ProgressDialog;
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

import org.fluttercode.datafactory.impl.DataFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.pushtorefresh.storio.sample.ui.Toasts.safeShowShortToast;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class TweetsFragment extends BaseFragment {

    private static final int COUNT_TO_ADD = 5000;
    @Inject
    StorIOSQLite storIOSQLite;

    UiStateController uiStateController;

    @InjectView(R.id.tweets_recycler_view)
    RecyclerView recyclerView;

    TweetsAdapter tweetsAdapter;

    @Inject
    Wire wire;

    public static boolean isKV = false;

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
    }

    @NonNull
    private Observable<List<Tweet>> getObservable() {
        return storIOSQLite
                .get()
                .listOfObjects(Tweet.class)
                .withQuery(TweetTableMeta.QUERY_ALL)
                .prepare()
                .createObservable();
    }

    void reloadData() {
        uiStateController.setUiStateLoading();
        Observable<List<Tweet>> listObservable = isKV ? getKVListObservable() : getObservable();

        final Subscription subscription = listObservable
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

    @NonNull
    private Observable<List<Tweet>> getKVListObservable() {
        return storIOSQLite
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
                });
    }

    @OnClick(R.id.tweets_empty_ui_add_tweets_button)
    void addTweets() {
        final List<Tweet> tweets = generateTweets();

        if (!isKV) putTweets(tweets);
        else
            putKVTweets(tweets);
    }

    @NonNull
    private List<Tweet> generateTweets() {
        final List<Tweet> tweets = new ArrayList<Tweet>();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Generating");
        final DataFactory df = new DataFactory();
        Observable.range(1, COUNT_TO_ADD)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.show();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.dismiss();
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {

                        tweets.add(Tweet.newTweet(df.getFirstName(), df.getRandomText(20, 100), df.getAddress(), df.getCity()));
                    }
                });
        return tweets;
    }

    private void putKVTweets(List<Tweet> tweets) {
        ArrayList<TweetKV> tweetKVs = convertTweetsToKV(tweets);
        final long timeBefore = System.currentTimeMillis();
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Adding");
        storIOSQLite.put()
                .objects(tweetKVs)
                .prepare()
                .createObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.show();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.dismiss();
                    }
                })
                .subscribe(new Observer<PutResults<TweetKV>>() {
                    @Override
                    public void onError(Throwable e) {
                        safeShowShortToast(getActivity(), R.string.tweets_add_error_toast);
                    }

                    @Override
                    public void onNext(PutResults<TweetKV> putResults) {
                        safeShowShortToast(getActivity(), "Added to KV: " + putResults.numberOfInserts() + "in " + String.valueOf(System.currentTimeMillis() - timeBefore) + " millis");
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }

    @NonNull
    private ArrayList<TweetKV> convertTweetsToKV(List<Tweet> tweets) {
        ArrayList<TweetKV> tweetKVs = new ArrayList<TweetKV>();
        for (Tweet tweet : tweets) {
            tweetKVs.add(new TweetKV(tweet.getByteArray()));
        }
        return tweetKVs;
    }

    private void putTweets(List<Tweet> tweets) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Adding");
        final long timeBefore = System.currentTimeMillis();
        storIOSQLite
                .put()
                .objects(tweets)
                .prepare()
                .createObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.show();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.dismiss();
                    }
                })
                .subscribe(new Observer<PutResults<Tweet>>() {
                    @Override
                    public void onError(Throwable e) {
                        safeShowShortToast(getActivity(), R.string.tweets_add_error_toast);
                    }

                    @Override
                    public void onNext(PutResults<Tweet> putResults) {
                        safeShowShortToast(getActivity(), "Added to default: " + putResults.numberOfInserts() + "in " + String.valueOf(System.currentTimeMillis() - timeBefore) + " millis");
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }

}
