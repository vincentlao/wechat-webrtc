package com.nt.wechat.ui.widget;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

/**
 * Created by laoni on 2015/12/19.
 */
public abstract class BasePagerFragment extends Fragment {
    public abstract void onBackendConnected();
    public boolean onMenuItemSelected(MenuItem item){return true;}
}
