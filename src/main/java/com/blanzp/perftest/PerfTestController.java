package com.blanzp.perftest;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jms.*;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@Controller
@RequestMapping("/perftest")
public class PerfTestController {
    private final static Logger LOGGER = Logger
            .getLogger(PerfTestController.class);

    @Autowired
    private PerfService perfService;
    @Autowired
    private Destination defaultDestination;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    PerfTestResult runPerf(
            @RequestParam(value = "count", required = false, defaultValue = "10") int count,
            @RequestParam(value = "instance", required = false, defaultValue = "tcp://localhost:61616") String instance,
            @RequestParam(value = "batch", required = false, defaultValue = "false") boolean batch,
            @RequestParam(value = "batch_size", required = false, defaultValue = "10") int batch_size)
            throws InterruptedException, JMSException {

        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
//        ConnectionFactory cf = new CachingConnectionFactory(acf);
        Connection conn = cf.createConnection();
        Session session = conn.createSession(
                batch,
                Session.AUTO_ACKNOWLEDGE);

        conn.start();
        MessageProducer producer = session.createProducer(this.defaultDestination);

        //TestResult result;

        Date startTime = new Date();
        Future<TestResult> futureResult = perfService.sendPayload(producer, session, count, batch, batch_size);
        while (!(futureResult.isDone())) {
            Thread.sleep(10); // 10-millisecond pause between each check
        }
        try {
            TestResult result = futureResult.get();
            LOGGER.debug("Produced " + result.count + " of " + result.expectedCount);
        } catch (ExecutionException e) {
            LOGGER.debug("Failed to produce messages" + e);
            return new PerfTestResult(0, "Failed to produce: " + e.getMessage(), 0, batch, batch_size);
        }

        LOGGER.debug("Done producing messages");

        MessageConsumer consumer = session.createConsumer(this.defaultDestination);


        futureResult = perfService.consumePayLoad(consumer, session, count, batch, batch_size);

        while (!(futureResult.isDone())) {
            Thread.sleep(1000); // 10-millisecond pause between each check
        }
        LOGGER.debug("Done consuming messages");

        try {
            TestResult result = futureResult.get();
            LOGGER.debug("Consumed " + result.count + " of " + result.expectedCount);
        } catch (ExecutionException e) {
            LOGGER.debug("Failed to read response messages" + e);
            return new PerfTestResult(0, "Failed to consume: " + e.getMessage(), 0, batch, batch_size);
        }

        Date endTime = new Date();

        long testTimeInMillis = endTime.getTime() - startTime.getTime();

        conn.stop();
        return new PerfTestResult(count, "OK", testTimeInMillis, batch, batch_size);
    }

}
