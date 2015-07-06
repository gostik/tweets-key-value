package com.pushtorefresh.storio.sample.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sample.db.entity.Tweet;
import com.pushtorefresh.storio.sample.db.entity.TweetKV;
import com.pushtorefresh.storio.sample.db.entity.TweetKVStorIOSQLiteDeleteResolver;
import com.pushtorefresh.storio.sample.db.entity.TweetKVStorIOSQLiteGetResolver;
import com.pushtorefresh.storio.sample.db.entity.TweetKVStorIOSQLitePutResolver;
import com.pushtorefresh.storio.sample.db.entity.TweetStorIOSQLiteDeleteResolver;
import com.pushtorefresh.storio.sample.db.entity.TweetStorIOSQLiteGetResolver;
import com.pushtorefresh.storio.sample.db.entity.TweetStorIOSQLitePutResolver;
import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.squareup.wire.Wire;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Module;
import dagger.Provides;

@Module()
public class DbModule {

    @Provides
    @NonNull
    @Singleton
    public StorIOSQLite provideStorIOSQLite(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        return DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(sqLiteOpenHelper)
                .addTypeMapping(Tweet.class, SQLiteTypeMapping.<Tweet>builder()
                        .putResolver(new TweetStorIOSQLitePutResolver())
                        .getResolver(new TweetStorIOSQLiteGetResolver())
                        .deleteResolver(new TweetStorIOSQLiteDeleteResolver())
                        .build())
                .addTypeMapping(TweetKV.class, SQLiteTypeMapping.<TweetKV>builder()
                        .putResolver(new TweetKVStorIOSQLitePutResolver())
                        .getResolver(new TweetKVStorIOSQLiteGetResolver())
                        .deleteResolver(new TweetKVStorIOSQLiteDeleteResolver())
                        .build())
                .build();
    }

    @Provides
    @NonNull
    @Singleton
    public SQLiteOpenHelper provideSQSqLiteOpenHelper(@NonNull Context context) {
        return new DbOpenHelper(context);
    }

    @Provides
    @NonNull
    @Singleton
    Wire provideWire() {
        return new Wire();
    }
}
