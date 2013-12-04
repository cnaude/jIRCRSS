/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.jircrss.IRCListeners;

import com.cnaude.jircrss.IRC;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;

/**
 *
 * @author cnaude
 */
public class JoinListener extends ListenerAdapter {

    /**
     *
     * @param event
     */
    @Override
    public void onJoin(JoinEvent event) {
        Channel channel = event.getChannel();
        User user = event.getUser();

        if (user.getNick().equals(event.getBot().getNick()))  {
            IRC.logDebug("Successfully joined " + channel.getName());
            IRC.startFeedWatcher();
        }
    }
}
