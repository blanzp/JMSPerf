package com.blanzp.perftest;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
public class PerfService {
    private final static Logger LOGGER = Logger
            .getLogger(PerfService.class);

    @Autowired
    private Destination defaultDestination;

    //@Override
    @Async
    public Future<TestResult> sendPayload(MessageProducer producer, Session session, int sendCount, boolean batch, int batch_size) throws JMSException {

        TextMessage message = session.createTextMessage();

        TestResult t = new TestResult();
        LOGGER.debug("Starting producer");
        for (int i = 0; i < sendCount; i++) {

            if (batch && i % batch_size == 0) {
                LOGGER.debug("Committing producer at " + i);
                session.commit();
            }
            message.setText("This is message " + (i + 1) + " from producer");
            // System.out.println("Sending message: " + message.getText());
            producer.send(message);
            t.count = i + 1;

        }
        producer.send(session.createMessage());
        if (batch) {
            session.commit();
        }

        t.expectedCount = sendCount;

        return new AsyncResult<TestResult>(t);

    }

    //@Override
    @Async
    public Future<TestResult> consumePayLoad(MessageConsumer consumer, Session session, int expectedCount, boolean batch, int batch_size) throws JMSException {

        TestResult t = new TestResult();
        t.expectedCount = expectedCount;

        int i = 0;
        LOGGER.debug("Starting consumer");

        while (true) {
            Message m = consumer.receive(100);
            if (m != null) {
                if (m instanceof TextMessage) {
                    TextMessage message = (TextMessage) m;
                    // System.out.println("Reading message: " + message.getText());
                } else {
                    LOGGER.debug("Found last message");
                    break;
                }
                if (batch && i % batch_size == 0) {
                    session.commit();
                    LOGGER.debug("Committing consumer at " + i);
                }
                t.count = i + 1;
                i += 1;
            } else {
                LOGGER.debug("No more messages");
                break;
            }

        }
        if (batch) {
            session.commit();
        }
        consumer.close();
        return new AsyncResult<TestResult>(t);
    }

}
