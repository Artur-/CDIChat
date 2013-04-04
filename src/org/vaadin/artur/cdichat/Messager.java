package org.vaadin.artur.cdichat;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class Messager {

    public static class OutgoingChatEvent {
        public String message;

        public OutgoingChatEvent(String message) {
            this.message = message;
        }
    }

    public static class IncomingChatEvent {
        public String message;

        public IncomingChatEvent(String message) {
            this.message = message;
        }

    }

    @Resource
    private Topic chatTopic;

    @Resource
    private ConnectionFactory connectionFactory;

    private MessageProducer producer;

    private Session messagingSession;

    @Inject
    private Event<IncomingChatEvent> incomingChatEvent;

    @PostConstruct
    private void setupMessaging() throws JMSException {
        Connection connection = connectionFactory.createConnection();
        connection.start();
        messagingSession = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        producer = messagingSession.createProducer(chatTopic);
        MessageConsumer consumer = messagingSession.createConsumer(chatTopic);
        consumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message arg0) {
                try {
                    TextMessage textMessage = (TextMessage) arg0;
                    System.out.println("Received JMS message: "
                            + textMessage.getText());
                    incomingChatEvent.fire(new IncomingChatEvent(textMessage
                            .getText()));
                } catch (JMSException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public void onOutgoingMessage(@Observes
    OutgoingChatEvent chatEvent) {
        System.out.println("Event -> JMS for " + chatEvent.message);
        try {
            Message message = messagingSession
                    .createTextMessage(chatEvent.message);
            producer.send(message);
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}