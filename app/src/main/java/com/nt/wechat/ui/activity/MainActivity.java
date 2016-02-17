package com.nt.wechat.ui.activity;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nt.wechat.R;
import com.nt.wechat.ui.widget.ConversationList;
import com.nt.wechat.ui.widget.ContactList;
import com.nt.wechat.ui.widget.MyselfWidget;

public class MainActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private int mCurrentPagerIndex;
    private ConversationList mConversationPager;
    private ContactList mContactPager;
    private MyselfWidget mMyselftPager;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    public MainActivity() {
        mConversationPager = new ConversationList();
        mContactPager = new ContactList();
        mMyselftPager = new MyselfWidget();

        mConversationPager.setActivity(this);
        mContactPager.setActivity(this);
        mMyselftPager.setActivity(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mCurrentPagerIndex = -1;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        int nFragment = mViewPager.getCurrentItem();
        switch (nFragment) {
            case 0:
                mConversationPager.onMenuItemSelected(item);
                break;
            case 1:
                mContactPager.onMenuItemSelected(item);
                break;
            case 2:
                mMyselftPager.onMenuItemSelected(item);
                break;
        }

        return true;
    }

    @Override
    protected  void onBackendConnected() {
        mConversationPager.onBackendConnected();
        mContactPager.onBackendConnected();
        mMyselftPager.onBackendConnected();
    }

    @Override
    protected void refreshUiReal() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = mConversationPager;
            } else if (position == 1) {
                fragment = mContactPager;
            } else if (position == 2) {
                fragment = mMyselftPager;
            }

            mCurrentPagerIndex = position;
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "We Chat";
                case 1:
                    return "Friends";
                case 2:
                    return "Myself";
            }
            return null;
        }
    }
}
