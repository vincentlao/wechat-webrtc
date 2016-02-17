package com.nt.wechat.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.nt.wechat.R;
import com.nt.wechat.entity.Account;
import com.nt.wechat.entity.Message;
import com.nt.wechat.entity.Contact;
import com.nt.wechat.services.WeChatService;
import com.nt.wechat.services.interfaces.IMessageManagerService;
import com.nt.wechat.ui.adapter.ChatMessageAdapter;
import com.nt.wechat.util.CommonDefine;

import org.jivesoftware.smack.SmackException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatActivity extends BaseActivity implements IMessageManagerService.IMessageListener {
    private EditText contentEditText;
    private Button sendButton;
    private ListView messageListView;
    private Contact contact;
    private String contactJid;
    private ArrayList<Message> mesages = new ArrayList<Message>();
    private ChatMessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messageListView = (ListView)findViewById(R.id.message_listview);
        sendButton = (Button)findViewById(R.id.send_button);
        contentEditText = (EditText)findViewById(R.id.message_edit_text);

        contactJid = getIntent ().getExtras().getString(CommonDefine.CONTACT_JID);

        messageAdapter = new ChatMessageAdapter(this, mesages);
        this.messageListView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = contentEditText.getText().toString();
                if (message.isEmpty()) {
                    return;
                }

                try {
                    WeChatService.WeChatBinder binder = getBinder();
                    if (binder == null) {
                        return;
                    }

                    Account account = binder.getAccountManagerService().getAccount();
                    if (account == null) {
                        return;
                    }

                    try {
                        JSONObject payload = new JSONObject();
                        payload.put(CommonDefine.JSON_JID, contactJid);
                        payload.put(CommonDefine.JSON_MESSAGE_TYPE, CommonDefine.MessageType_Text);
                        payload.put(CommonDefine.JSON_MESSAGE_TEXT, message);

                        binder.getMessageManagerService().sendMessage(ChatActivity.this.contact.getJid(), payload.toString());
                    } catch (JSONException e) {
                        //e.print
                    }

                    contentEditText.getText().clear();
                    //ChatMessageAdapter adapter = (ChatMessageAdapter) messageListView.getAdapter();
                    mesages.add(new Message(account.getJid(), ChatActivity.this.contact.getJid(), message));
                    messageAdapter.notifyDataSetChanged();
                    //adapter.add(new Message().setMessage(message).setParticipantJid(account.getJid()));
                    //adapter.notifyDataSetChanged();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    //// TODO: 2015/12/13 send message failed
                }
            }
        });
    }

    @Override
    void onBackendConnected() {
        WeChatService.WeChatBinder binder = getBinder();
        if (binder == null) {
            return;
        }

        binder.getMessageManagerService().addMessageListener(this);

        Contact contact = binder.getContactManagerService().getContact(contactJid);
        if (contact == null) {
            return;
        }

        this.contact = contact;

        ArrayList<Message> historyMessage = binder.getMessageManagerService().getHistoryMessage(contactJid);
        //ChatMessageAdapter adapter = (ChatMessageAdapter) messageListView.getAdapter();
        //if (adapter != null && historyMessage != null) {
            //adapter.clear();
            //adapter.addAll();
            //adapter.notifyDataSetChanged();
        //}

        mesages.clear();
        if (historyMessage != null) {
            mesages.addAll(historyMessage);
        }

        //messageAdapter.notifyDataSetChanged();
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
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void processMessage(final String senderJid, final org.jivesoftware.smack.packet.Message message) {
        //ChatMessageAdapter adapter = (ChatMessageAdapter) messageListView.getAdapter();
        //if (adapter != null && message.getBody() != null && !message.getBody().isEmpty()) {
           // adapter.add(new Message().setMessage(message.getBody()).setParticipantJid(senderJid));
            //adapter.notifyDataSetChanged();
       // }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mesages.add(new Message(message.getFrom().toString(), message.getTo().toString(), message.getBody()));
                messageAdapter.notifyDataSetChanged();
            }
        });

    }
}
