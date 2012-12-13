package net.kindleit.gae.example.client.ui;

import net.kindleit.gae.example.client.MessagesServiceAsync;
import net.kindleit.gae.example.model.Message;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * 
 * @author drone
 * @version 1.0
 */
public class MessageForm extends HorizontalPanel {

    private final TextBox textBox;
    private final Button button;
    private MessageBox messageBox;

    private MessagesServiceAsync messagesServiceAsync;

    public MessageForm() {
        Label label = new Label("Text: ");
        label.setStyleName("textLabel");
        add(label);

        textBox = new TextBox();
        label.setStyleName("textBox");
        add(textBox);

        button = new Button("Create");
        button.addStyleName("sendButton");
        button.addClickHandler(new SayClickHandler());
        add(button);
    }

    public void setMessageBox(MessageBox messageBox) {
        this.messageBox = messageBox;
    }

    public void setMessagesService(MessagesServiceAsync messagesServiceAsync) {
        this.messagesServiceAsync = messagesServiceAsync;
    }

    protected class SayClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            Message message = new Message();
            message.setText(textBox.getText());

            AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                    messageBox.update();
                }

                @Override
                public void onSuccess(Void result) {
                    messageBox.update();
                }
            };

            messagesServiceAsync.create(message, callback);
            textBox.setText("");
        }
    }
}
