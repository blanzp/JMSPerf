package com.blanzp.perftest;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
public class PerfService {
    private final static Logger LOGGER = Logger
            .getLogger(PerfService.class);

    @Autowired
    private JmsMessageSender jmsMessageSender;

    @Autowired
    private JmsMessageReceiver jmsMessageReceiver;

    //@Override
    @Async
    public Future<String> sendPayload(int sendCount) {
        for (int i = 0; i <= sendCount; i++) {
            jmsMessageSender.sendMessage("Test Msg " + i);
        }
        return new AsyncResult<String>("Done");

    }

    //@Override
    @Async
    public Future<String> consumePayLoad(int expectedCount) {
        for (int i = 0; i <= expectedCount; i++) {
            String message = jmsMessageReceiver.receive().toString();
            //System.out.println("Got" + message);
        }
        return new AsyncResult<String>("Processed " + Integer.toString(expectedCount));
    }

}
