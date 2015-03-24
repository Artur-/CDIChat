package org.vaadin.artur.cdichat;

import java.text.DateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;

@CDIUI("")
@Push
@Theme("valo")
public class CDIChat extends UI {

	private CDIChatLayout layout;

	@Inject
	CDIMessenger messenger;

	@PostConstruct
	public void registerListener() {
		messenger.addMessageListener(this);
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
			messenger.sendMessage(getUserId() + ": " + message);
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
	 * Called by CDIMessenger
	 */
	public void onMessage(final String text) {
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
			System.out.println("Trying to send message to detached UI");
			messenger.removeMessageListener(this);

		}
	}

}