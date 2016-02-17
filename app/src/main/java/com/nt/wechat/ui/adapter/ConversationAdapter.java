package com.nt.wechat.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nt.wechat.R;
import com.nt.wechat.entity.Contact;
import com.nt.wechat.entity.Conversation;
import com.nt.wechat.ui.activity.ChatActivity;
import com.nt.wechat.util.CommonDefine;

import java.util.List;

/**
 * Created by laoni on 2015/12/12.
 */
public class ConversationAdapter  extends ArrayAdapter<Conversation> {
    private Activity activity = null;

    public ConversationAdapter(Activity activity, List<Conversation> objects) {
        super(activity, 0, objects);
        this.activity = activity;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = this.activity.getLayoutInflater().inflate(R.layout.fragment_contact_item, parent, false);
        }

        final Conversation conversation = getItem(position);

        TextView displayNameTextView = (TextView)convertView.findViewById(R.id.contact_display_name);
        if (conversation.getContactName() != null && !conversation.getContactName().isEmpty()) {
            displayNameTextView.setText(conversation.getContactName());
        } else {
            displayNameTextView.setText(conversation.getContact());
        }

        TextView jidTextView = (TextView)convertView.findViewById(R.id.contact_jid);
        jidTextView.setText(conversation.getContact());

        return convertView;
    }
}
