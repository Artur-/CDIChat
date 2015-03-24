package org.vaadin.artur.cdichat;


public class MessageEvent {

	private String message;

	public MessageEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

}
