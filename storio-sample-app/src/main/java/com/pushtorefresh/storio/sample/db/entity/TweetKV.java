package com.pushtorefresh.storio.sample.db.entity;

import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.squareup.wire.Message;

import java.util.Arrays;

/**
 * Created by user_sca on 06.07.2015.
 */
@StorIOSQLiteType(table = "tweetskv")
public class TweetKV extends Message {
    @Nullable
    @StorIOSQLiteColumn(name = "_id", key = true)
    public Long id;

    @StorIOSQLiteColumn(name = "blobField")
    byte[] blobField;

    @Nullable
    public Long getId() {
        return id;
    }

    public byte[] getBlobField() {
        return blobField;
    }

    public TweetKV() {
    }

    public TweetKV(Long id, byte[] blobField) {
        this.id = id;
        this.blobField = blobField;
    }

    public TweetKV(byte[] blobField) {
        this.blobField = blobField;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TweetKV tweetKV = (TweetKV) o;

        if (id != null ? !id.equals(tweetKV.id) : tweetKV.id != null) return false;
        return Arrays.equals(blobField, tweetKV.blobField);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (blobField != null ? Arrays.hashCode(blobField) : 0);
        return result;
    }
}
