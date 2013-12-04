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
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author cnaude
 */
public class MessageListener extends ListenerAdapter {

    /**
     *
     * @param event
     */
    @Override
    public void onMessage(MessageEvent event) {

        String message = event.getMessage();
        Channel channel = event.getChannel();
        User user = event.getUser();

        if (channel.getName().equals(IRC.channel)) {
            IRC.logDebug("Message caught <" + user.getNick() + ">: " + message);
            if (channel.getOps().contains(user)) {
                if (message.equalsIgnoreCase(".stop")) {
                    IRC.logDebug("Shutdown initiated by op: " + user.getNick());
                    IRC.shutdown();
                }
            }
        }
    }
}
