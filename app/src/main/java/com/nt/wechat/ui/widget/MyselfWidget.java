package com.nt.wechat.ui.widget;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nt.wechat.R;
import com.nt.wechat.ui.activity.BaseActivity;
import com.nt.wechat.ui.activity.MainActivity;

/**
 * Created by laoni on 2015/12/12.
 */
public class MyselfWidget   extends BasePagerFragment {
    private MainActivity mActivity = null;

    public MyselfWidget() {
    }

    public void setActivity(MainActivity activity) {
        this.mActivity = activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_myself_tab, container, false);
        return rootView;
    }


    public void onBackendConnected() {

    }
}