package org.vaadin.artur.cdichat;

import java.text.DateFormat;
import java.util.Date;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.vaadin.artur.cdichat.Messager.IncomingChatEvent;
import org.vaadin.artur.cdichat.Messager.OutgoingChatEvent;
import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;

import com.vaadin.cdi.CDIUI;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@CDIUI
public class CDIChat extends UI {

    @Inject
    private Messager messager;

    @UiField
    private VerticalLayout chatLog;

    @Inject
    private javax.enterprise.event.Event<OutgoingChatEvent> chatEvent;

    @Override
    protected void init(VaadinRequest request) {
        setContent(Clara.create(getClass().getResourceAsStream("CDIChat.xml"),
                this));
    }

    @UiHandler("chatMessage")
    public void onTextInput(Property.ValueChangeEvent event) {
        TextField field = (TextField) event.getProperty();
        if (!"".equals(field.getValue())) {
            chatEvent.fire(new OutgoingChatEvent(field.getValue()));
            field.setValue("");
        }
    }

    public void onIncomingMessage(@Observes
    IncomingChatEvent event) {
        getSession().lock();
        DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        try {
            Label l = new Label(df.format(new Date()) + ": " + event.message);
            l.setSizeUndefined();
            chatLog.addComponent(l);
        } finally {
            getSession().unlock();
        }

    }

}