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
import com.nt.wechat.entity.Conversation;
import com.nt.wechat.services.WeChatService;
import com.nt.wechat.ui.activity.BaseActivity;
import com.nt.wechat.ui.activity.ChatActivity;
import com.nt.wechat.ui.activity.MainActivity;
import com.nt.wechat.ui.activity.VideoChatActivity;
import com.nt.wechat.ui.adapter.ContactListAdapter;
import com.nt.wechat.ui.adapter.ConversationAdapter;
import com.nt.wechat.util.CommonDefine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laoni on 2015/12/12.
 */
public class ConversationList extends BasePagerFragment {
    private MainActivity mActivity = null;
    private ConversationAdapter mConversationAdapter = null;
    private  ListView conversationListView;

    public ConversationList() {

    }
    public void setActivity(MainActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conversations_tab, container, false);

        List<Conversation> conversations = new ArrayList<>();
        WeChatService.WeChatBinder binder = this.mActivity.getBinder();
        if (binder != null) {
            ArrayList<Conversation> cvs = binder.getConversationManagerService().getConversation();
            if (cvs != null) {
                conversations.addAll(cvs);
            }
        }

        this.conversationListView = (ListView) rootView.findViewById(R.id.conversation_list);


        this.mConversationAdapter = new ConversationAdapter(this.mActivity, conversations);

        this.conversationListView.setAdapter(this.mConversationAdapter);

        conversationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Conversation conversation = ConversationList.this.mConversationAdapter.getItem(position);

                Intent intent = new Intent(mActivity, VideoChatActivity.class);
                intent.putExtra(CommonDefine.JSON_JID, conversation.getContact());
                mActivity.startActivity(intent);
            }
        });

        registerForContextMenu(this.conversationListView);

        return rootView;
    }

    public void onBackendConnected() {
        WeChatService.WeChatBinder binder = this.mActivity.getBinder();
        if (binder != null) {
            ArrayList<Conversation> cvs = binder.getConversationManagerService().getConversation();

            if (mConversationAdapter != null) {
                mConversationAdapter.clear();
                if (cvs != null) {
                    mConversationAdapter.addAll(cvs);
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mActivity.getMenuInflater().inflate(R.menu.menu_chat, menu);
    }

    public boolean onMenuItemSelected(MenuItem item) {
        if (mConversationAdapter == null) {
            return false;
        }

        Intent intent = null;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Conversation conversation = this.mConversationAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.id.menu_item_text_chat:
                intent = new Intent(mActivity, ChatActivity.class);
                intent.putExtra(CommonDefine.CONTACT_JID, conversation.getContact());
                mActivity.startActivity(intent);
                return true;
            case R.id.menu_item_vedio_chat:
                intent = new Intent(mActivity, VideoChatActivity.class);
                intent.putExtra(CommonDefine.CONTACT_JID, conversation.getContact());
                mActivity.startActivity(intent);
                return true;
        }
        return false;
    }
}
