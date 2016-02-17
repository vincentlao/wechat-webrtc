package com.nt.wechat.ui.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nt.wechat.R;
import com.nt.wechat.entity.Message;

import java.util.List;

/**
 * Created by vincentlao on 2015/12/13.
 */
public class ChatMessageAdapter extends ArrayAdapter<Message> {
    private Activity mActivity = null;

    public ChatMessageAdapter(Activity activity, List<Message> objects) {
        super(activity, 0, objects);
        this.mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message item = getItem(position);

        if (convertView == null) {
            convertView = this.mActivity.getLayoutInflater().inflate(R.layout.fragment_chat_message_item, parent, false);
        }

        TextView chatMessageTextView = (TextView) convertView.findViewById(R.id.chat_message_textview);
        chatMessageTextView.setText(item.getFromJid() + " : " + item.getMessage());

        return convertView;
    }
}
