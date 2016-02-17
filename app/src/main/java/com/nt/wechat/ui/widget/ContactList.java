package com.nt.wechat.ui.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nt.wechat.R;
import com.nt.wechat.entity.Contact;
import com.nt.wechat.services.Impl.ContactManagerService;
import com.nt.wechat.services.WeChatService;
import com.nt.wechat.ui.activity.BaseActivity;
import com.nt.wechat.ui.activity.ChatActivity;
import com.nt.wechat.ui.activity.MainActivity;
import com.nt.wechat.ui.activity.VideoChatActivity;
import com.nt.wechat.ui.adapter.ContactListAdapter;
import com.nt.wechat.util.CommonDefine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laoni on 2015/12/12.
 */
public class ContactList extends BasePagerFragment {
    private MainActivity mActivity = null;
    private ListView contactListView = null;
    private ContactListAdapter contactListAdapter = null;

    public ContactList() {
    }

    public void setActivity(MainActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_tab, container, false);

        List<Contact> contacts = new ArrayList<>();
        WeChatService.WeChatBinder binder = this.mActivity.getBinder();
        if (binder != null) {
            List<Contact> cts = binder.getContactManagerService().getContactList();
            if (cts != null) {
                contacts.addAll(cts);
            }
        }

        this.contactListView = (ListView) rootView.findViewById(R.id.contact_list);
        this.contactListAdapter = new ContactListAdapter(this.mActivity, contacts);

        this.contactListView.setAdapter(this.contactListAdapter);

        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = ContactList.this.contactListAdapter.getItem(position);

                Intent intent = new Intent(mActivity, VideoChatActivity.class);
                intent.putExtra(CommonDefine.JSON_JID, contact.getJid());
                mActivity.startActivity(intent);
            }
        });
        registerForContextMenu(this.contactListView);
        return rootView;
    }

    public void onBackendConnected() {
        WeChatService.WeChatBinder binder = this.mActivity.getBinder();
        if (binder == null) {
            return;
        }

        List<Contact> cts = binder.getContactManagerService().getContactList();

        if (contactListAdapter != null) {
            contactListAdapter.clear();
            if (cts != null) {
                contactListAdapter.addAll(cts);
            }
        }

        //contactListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mActivity.getMenuInflater().inflate(R.menu.menu_chat, menu);
    }

    public boolean onMenuItemSelected(MenuItem item) {
        if (this.contactListAdapter == null) {
            return  false;
        }

        Intent intent = null;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Contact contact = this.contactListAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.id.menu_item_text_chat:
                intent = new Intent(mActivity, ChatActivity.class);
                intent.putExtra(CommonDefine.CONTACT_JID, contact.getJid());
                mActivity.startActivity(intent);
                return true;
            case R.id.menu_item_vedio_chat:
                intent = new Intent(mActivity, VideoChatActivity.class);
                intent.putExtra(CommonDefine.CONTACT_JID, contact.getJid());
                mActivity.startActivity(intent);
                return true;
        }
        return false;
    }

}
