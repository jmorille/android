package net.kindleit.gae.example.client;

import net.kindleit.gae.example.client.ui.Shoutbox;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

public class IndexPage implements EntryPoint {

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        // remove Loading-Message from page
        RootPanel.getBodyElement().removeChild(
                DOM.getElementById("Loading-Message"));

        Shoutbox shoutbox = new Shoutbox();
        shoutbox.setHeader("Welcome to Google App Engine for Java!");

        // create greetings message
        RootPanel.get().add(shoutbox);
    }
}
