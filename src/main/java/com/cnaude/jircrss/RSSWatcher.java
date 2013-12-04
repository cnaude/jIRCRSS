/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.jircrss;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.horrabin.horrorss.RssChannelBean;
import org.horrabin.horrorss.RssFeed;
import org.horrabin.horrorss.RssItemBean;
import org.horrabin.horrorss.RssParser;
import org.pircbotx.PircBotX;

/**
 *
 * @author cnaude
 */
public class RSSWatcher {

    private final Timer timer = new Timer();
    Date date = new Date();
    
    public RSSWatcher(final PircBotX bot, final String url) {
        IRC.logDebug("Starting feed watcher: " + url);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                IRC.logDebug("Checking RSS feed: " + url);
                RssParser rss = new RssParser();
                
                try {
                    RssFeed feed = rss.load(url);

                    RssChannelBean rssChannel = feed.getChannel();                    
                    IRC.logDebug("Feed Title: " + rssChannel.getTitle());                   
                    
                    List<RssItemBean> items = feed.getItems();
                    
                    if (items.size() > 0) {
                        RssItemBean item = items.get(0);
                        IRC.logDebug("Prev Date: " + date);
                        IRC.logDebug("Pub Date: " + item.getPubDate());                        
                        if (!item.getPubDate().equals(date)) {
                            IRC.logDebug("Sending article");
                            bot.sendIRC().message(IRC.channel, item.getTitle() 
                            + " :: " + item.getLink()
                            + " :: " + item.getPubDate());                            
                            date = item.getPubDate();
                        }
                    }

                } catch (Exception e) {
                    // Something to do if an exception occurs
                }
            }

        }, 0, 1000*IRC.rssRefreshRate);
    }
    
    public void cancel() {
        timer.cancel();
    }
}
