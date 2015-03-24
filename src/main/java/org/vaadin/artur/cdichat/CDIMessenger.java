package org.vaadin.artur.cdichat;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CDIMessenger {

	List<CDIChat> uis = new ArrayList<>();

	public synchronized void addMessageListener(CDIChat ui) {
		System.out.println("Add listener: " + ui);
		uis.add(ui);
	}

	public synchronized void removeMessageListener(CDIChat ui) {
		System.out.println("Remove listener: " + ui);
		uis.remove(ui);
	}

	public synchronized void sendMessage(String message) {
		for (CDIChat ui : uis.toArray(new CDIChat[uis.size()])) {
			ui.onMessage(message);
		}
	}

}
