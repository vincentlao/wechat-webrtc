package com.nt.wechat.services.interfaces;

import com.nt.wechat.entity.Contact;

import java.util.ArrayList;

/**
 * Created by laoni on 2015/12/18.
 */
public interface IContactManagerService {
    public Contact getContact(String jid);
    public ArrayList<Contact> getContactList();
}
