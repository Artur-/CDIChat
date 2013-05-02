package org.vaadin.artur.cdichat;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.vaadin.event.EventRouter;

public class JMSMessenger implements Messenger {

    @Resource
    private Topic chatTopic;

    @Resource
    private ConnectionFactory connectionFactory;

    private MessageProducer producer;

    private Session messagingSession;

    private EventRouter eventRouter = new EventRouter();

    @PostConstruct
    private void setupMessaging() throws JMSException {
        Connection connection = connectionFactory.createConnection();
        connection.start();
        messagingSession = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        producer = messagingSession.createProducer(chatTopic);
        MessageConsumer consumer = messagingSession.createConsumer(chatTopic);
        consumer.setMessageListener(new javax.jms.MessageListener() {

            @Override
            public void onMessage(Message arg0) {
                try {
                    TextMessage textMessage = (TextMessage) arg0;
                    System.out.println("Received JMS message: "
                            + textMessage.getText());
                    eventRouter.fireEvent(new MessageEvent(this, textMessage
                            .getText()));
                } catch (JMSException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void sendMessage(String message) {
        System.out.println("Sending JMS message " + message);
        try {
            Message msg = messagingSession.createTextMessage(message);
            producer.send(msg);
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void addMessageListener(MessageListener messageListener) {
        eventRouter.addListener(MessageEvent.class, messageListener,
                MessageListener.METHOD);
    }

    @Override
    public void removeMessageListener(MessageListener messageListener) {
        eventRouter.removeListener(MessageEvent.class, messageListener);

    }

}