package com.nt.wechat.services.Impl;

import com.nt.wechat.entity.Account;
import com.nt.wechat.entity.Contact;
import com.nt.wechat.persistance.DatabaseBackend;
import com.nt.wechat.services.WeChatService;
import com.nt.wechat.services.interfaces.IContactManagerService;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by laoni on 2015/12/12.
 */
public class ContactManagerService implements IContactManagerService,RosterListener, WeChatService.IAccountEvent {
    private WeChatService mService;
    ArrayList<Contact> contacts = new ArrayList<>();

    public ContactManagerService(WeChatService service) {
        this.mService = service;
    }

    public void onLogin(Account account) {
    }

    public void onLogout(Account account) {
    }

    public Contact getContact(String jid) {
        Contact contact = null;
        for (Contact c : contacts) {
            if (c.getJid().equals(jid)) {
                contact = c;
                break;
            }
        }

        return contact;
    }

    public ArrayList<Contact> getContactList() {
        if (!contacts.isEmpty()) {
            return contacts;
        }

        ArrayList mRoster = new ArrayList<>();
        AbstractXMPPConnection connection = mService.getAccountService().getConnection();
        Roster roster = Roster.getInstanceFor(connection);

        contacts.clear();

        for (RosterEntry entry : roster.getEntries()) {
            Contact c = new Contact(entry.getUser(), entry.getName(), roster.getPresence(entry.getUser()).isAvailable());
            contacts.add(c);
        }

        return contacts;
    }

    /**
     * Called when roster entries are added.
     *
     * @param addresses the XMPP addresses of the contacts that have been added to the roster.
     */
    @Override
    public void entriesAdded(Collection<String> addresses) {
    }

    /**
     * Called when a roster entries are updated.
     *
     * @param addresses the XMPP addresses of the contacts whose entries have been updated.
     */
    @Override
    public void entriesUpdated(Collection<String> addresses) {

    }

    /**
     * Called when a roster entries are removed.
     *
     * @param addresses the XMPP addresses of the contacts that have been removed from the roster.
     */
    @Override
    public void entriesDeleted(Collection<String> addresses) {

    }

    /**
     * Called when the presence of a roster entry is changed. Care should be taken
     * when using the presence data delivered as part of this event. Specifically,
     * when a user account is online with multiple resources, the UI should account
     * for that. For example, say a user is online with their desktop computer and
     * mobile phone. If the user logs out of the IM client on their mobile phone, the
     * user should not be shown in the roster (contact list) as offline since they're
     * still available as another resource.<p>
     * <p>
     * To get the current "best presence" for a user after the presence update, query the roster:
     * <pre>
     *    String user = presence.getFrom();
     *    Presence bestPresence = roster.getPresence(user);
     * </pre>
     * <p>
     * That will return the presence value for the user with the highest priority and
     * availability.
     * <p>
     * Note that this listener is triggered for presence (mode) changes only
     * (e.g presence of types available and unavailable. Subscription-related
     * presence packets will not cause this method to be called.
     *
     * @param presence the presence that changed.
     * @see Roster#getPresence(String)
     */
    @Override
    public void presenceChanged(Presence presence) {

    }
}
