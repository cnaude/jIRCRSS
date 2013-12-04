/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cnaude.jircrss.IRCListeners;

import com.cnaude.jircrss.IRC;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;

/**
 *
 * @author cnaude
 */
public class ConnectListener extends ListenerAdapter {
      
    /**
     *
     * @param event
     */
    @Override
    public void onConnect(ConnectEvent event) {    

        IRC.logDebug("Successfully connected");
        IRC.startBotWatcher();
    }
}
