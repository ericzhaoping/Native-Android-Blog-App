package com.spantom.redcloud.model;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class MemoId {

    @Exclude
    public String MemoId;

    public <T extends MemoId> T withId(@NonNull final String id) {
        this.MemoId = id;
        return (T) this;
    }

}
