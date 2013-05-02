package org.vaadin.artur.cdichat;

import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;

import org.vaadin.artur.cdichat.Messenger.MessageEvent;
import org.vaadin.artur.cdichat.Messenger.MessageListener;
import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;

import com.vaadin.annotations.Push;
import com.vaadin.cdi.CDIUI;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@CDIUI
@Push
public class CDIChat extends UI implements MessageListener {

    @Inject
    private Messenger messager;

    @UiField
    private VerticalLayout chatLog;

    @Override
    protected void init(VaadinRequest request) {
        setContent(Clara.create(getClass().getResourceAsStream("CDIChat.xml"),
                this));
        messager.addMessageListener(this);
    }

    @UiHandler("chatMessage")
    public void onTextInput(Property.ValueChangeEvent event) {
        TextField field = (TextField) event.getProperty();
        if (!"".equals(field.getValue())) {
            messager.sendMessage(field.getValue());
            field.setValue("");
        }
    }

    @Override
    public void messageReceived(MessageEvent event) {
        System.out.println("incoming message " + event.getMessage() + " for "
                + getUIId() + "(" + this + ")");
        getSession().lock();
        DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        try {
            Label l = new Label(df.format(new Date()) + ": "
                    + event.getMessage());
            l.setSizeUndefined();
            chatLog.addComponent(l);
        } finally {
            getSession().unlock();
        }
    }

}