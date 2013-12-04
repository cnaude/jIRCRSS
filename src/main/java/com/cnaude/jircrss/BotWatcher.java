/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.jircrss;

import static com.cnaude.jircrss.IRC.logDebug;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.pircbotx.PircBotX;

/**
 *
 * @author cnaude
 */
public class BotWatcher {

    private final Timer timer = new Timer();
    Date date = new Date();

    public BotWatcher(final PircBotX bot) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (bot.isConnected()) {
                    logDebug("Bot is connected.");
                } else {
                    logDebug("Bot is not connected.");
                }
            }

        }, 0, 1000 * IRC.botAliveCheckInterval);
    }

    public void cancel() {
        timer.cancel();
    }
}
