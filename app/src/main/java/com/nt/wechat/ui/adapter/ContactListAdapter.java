package com.nt.wechat.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nt.wechat.R;
import com.nt.wechat.entity.Contact;
import com.nt.wechat.ui.activity.ChatActivity;
import com.nt.wechat.ui.activity.VideoChatActivity;
import com.nt.wechat.util.CommonDefine;

import java.util.List;

/**
 * Created by laoni on 2015/12/12.
 */
public class ContactListAdapter extends ArrayAdapter<Contact> {
    private Activity activity;


    public ContactListAdapter(Activity activity, List<Contact> objects){
        super(activity, 0, objects);
        this.activity = activity;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = this.activity.getLayoutInflater().inflate(R.layout.fragment_contact_item, parent, false);
        }

        final Contact contact = getItem(position);

        TextView displayNameTextView = (TextView)convertView.findViewById(R.id.contact_display_name);
        if (contact.getDisplayName() == null || contact.getDisplayName().isEmpty()) {
            displayNameTextView.setText(contact.getJid());
        } else {
            displayNameTextView.setText(contact.getDisplayName());
        }

        TextView jidTextView = (TextView)convertView.findViewById(R.id.contact_jid);
        jidTextView.setText(contact.getJid());

        return convertView;
    }
}