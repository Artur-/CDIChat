package org.vaadin.artur.cdichat;

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

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;

@CDIUI("")
@Push
@Theme("valo")
public class CDIChat extends UI implements MessageListener {

	@Inject
	private JMSMessenger jms;
	private CDIChatLayout layout;

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
		layout = new CDIChatLayout();
		setContent(layout);
		layout.chatMessage.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				String message = layout.chatMessage.getValue();
				layout.chatMessage.setValue("");
				sendMessage(message);
			}
		});
	}

	public void sendMessage(String message) {
		if (!"".equals(message)) {
			try {
				jms.sendMessage(getUserId() + ": " + message);
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
		WebBrowser browser = getPage().getWebBrowser();
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
						layout.chatLog.addComponent(l);
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