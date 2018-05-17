package com.todo;

import android.app.Application;

import com.custom.volley.requester.VolleyInitiator;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VolleyInitiator.init(this);
    }
}
