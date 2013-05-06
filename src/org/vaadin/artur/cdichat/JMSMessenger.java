/**
 * 
 */
package org.vaadin.artur.cdichat;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import com.vaadin.cdi.UIScoped;

@UIScoped
public class JMSMessenger {

    @Resource(name = "jms/ChatTopic")
    private Topic chatTopic;

    @Resource
    private ConnectionFactory connectionFactory;

    private MessageProducer producer;

    private Session messagingSession;

    private MessageConsumer consumer;

    private Connection connection;

    @PostConstruct
    private void init() throws JMSException {
        connection = connectionFactory.createConnection();
        messagingSession = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);
        producer = messagingSession.createProducer(chatTopic);
        consumer = messagingSession.createConsumer(chatTopic);

        connection.start();
    }

    @PreDestroy
    private void destroy() throws JMSException {
        consumer.close();
        producer.close();
        messagingSession.close();
        connection.close();
    }

    /**
     * Sends the given message
     * 
     * @throws JMSException
     */
    public void sendMessage(String text) throws JMSException {
        producer.send(messagingSession.createTextMessage(text));

    }

    /**
     * Sets the message listener
     * 
     * @throws JMSException
     */
    public void setMessageListener(MessageListener listener)
            throws JMSException {
        consumer.setMessageListener(listener);
    }

}
