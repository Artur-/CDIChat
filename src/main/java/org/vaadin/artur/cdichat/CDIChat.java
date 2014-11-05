package org.vaadin.artur.cdichat;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;

import com.vaadin.annotations.Push;
import com.vaadin.cdi.CDIUI;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;
import com.vaadin.ui.VerticalLayout;

@CDIUI("")
@Push
public class CDIChat extends UI implements MessageListener {

    @UiField
    private VerticalLayout chatLog;

    @Inject
    private JMSMessenger jms;

    @PostConstruct
    public void registerConsumer() {
        try {
            jms.setMessageListener(this);
        } catch (JMSException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Unable to register consumer", e);
        }
    }

    @Override
    protected void init(VaadinRequest request) {
        InputStream res = getClass().getResourceAsStream("CDIChat.xml");
        Component content = (Clara.create(res, this));
        setContent(content);
    }

    /**
     * Called by Clara whenever the text input changes
     */
    @UiHandler("chatMessage")
    public void onTextInput(Property.ValueChangeEvent event) {
        TextField field = (TextField) event.getProperty();
        if (!"".equals(field.getValue())) {
            String text = field.getValue();
            try {
                jms.sendMessage(getUserId() + ": " + text);
                field.setValue("");
            } catch (JMSException e) {
                Notification.show("Unable to send message. See server log");
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                        "Unable to send message", e);
            }
        }
    }

    /**
     * Just return something which uniquely identifies the user
     */
    private String getUserId() {
        WebBrowser browser = getSession().getBrowser();
        String browserString = browser.isChrome() ? "Chrome" : browser
                .isFirefox() ? "Firefox" : browser.isIE() ? "IE" : browser
                .isSafari() ? "Safari" : "Unknown";
        browserString += " " + browser.getBrowserMajorVersion();
        String osString = browser.isWindows() ? "Windows"
                : browser.isMacOSX() ? "Mac" : browser.isLinux() ? "Linux"
                        : "Unknown";
        return hashCode() + " (" + browserString + "/" + osString + ")";
    }

    /**
     * Called by the MessageConsumer whenever a JMS message arrives
     */
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        final String text;
        try {
            text = textMessage.getText();
            System.out.println("incoming message " + text + " for " + getUIId()
                    + "(" + this + ")");
            try {

                access(new Runnable() {
                    @Override
                    public void run() {
                        DateFormat df = DateFormat
                                .getTimeInstance(DateFormat.MEDIUM);
                        Label l = new Label(df.format(new Date()) + ": " + text);
                        l.setSizeUndefined();
                        chatLog.addComponent(l);
                    }
                });
            } catch (UIDetachedException e) {
                Logger.getLogger(getClass().getName()).info(
                        "Trying to send message to detached UI");
            }
        } catch (JMSException e) {
            Notification.show("Unable to retrieve message. See server log");
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Unable to retrieve message", e);
        }

    }

}