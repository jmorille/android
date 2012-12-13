package net.kindleit.gae.example.client.ui;

import net.kindleit.gae.example.client.MessagesService;
import net.kindleit.gae.example.client.MessagesServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * 
 * @author drone
 * @version 1.0
 */
public class Shoutbox extends Composite {

    private final Panel messagePanel;
    private final Label header;
    private final MessageBox messageBox;
    private final MessageForm messageForm;

    private final MessagesServiceAsync messagesServiceAsync;

    public Shoutbox() {

        // create service
        this.messagesServiceAsync = GWT.create(MessagesService.class);

        // create view
        messagePanel = new FlowPanel();

        header = new Label();
        messagePanel.add(header);

        messageBox = new MessageBox();
        messageBox.setMessagesService(messagesServiceAsync);
        messageBox.update();
        messagePanel.add(messageBox);

        messageForm = new MessageForm();
        messageForm.setMessageBox(messageBox);
        messageForm.setMessagesService(messagesServiceAsync);
        messagePanel.add(messageForm);

        // initialize
        initWidget(messagePanel);
    }

    public void setHeader(String text) {
        header.setText(text);
    }
}
