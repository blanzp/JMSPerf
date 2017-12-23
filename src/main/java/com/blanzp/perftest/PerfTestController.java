package com.blanzp.perftest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.jms.*;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.JMSC;
import com.ibm.msg.client.wmq.WMQConstants;
import com.ibm.mq.MQException;

@Controller
@RequestMapping("/perftest")
public class PerfTestController {
    private final static Logger LOGGER = Logger
            .getLogger(PerfTestController.class);

    @Autowired
    private PerfService perfService;
    @Autowired
    private Destination defaultDestination;

    private MQException e;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<PerfTestResult> runPerf(
            @RequestParam(value = "count", required = false, defaultValue = "10") int count,
            @RequestParam(value = "instance", required = false, defaultValue = "tcp://localhost:61616") String instance,
            @RequestParam(value = "batch", required = false, defaultValue = "false") boolean batch,
            @RequestParam(value = "batch_size", required = false, defaultValue = "10") int batch_size)
            throws InterruptedException, JMSException {

        try {
            Date startTime = new Date();

            MQQueueConnectionFactory mqcf = new MQQueueConnectionFactory();

            mqcf.setHostName("localhost");
            mqcf.setPort(1414);
            mqcf.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
            mqcf.setQueueManager(instance);
            mqcf.setChannel("SYSTEM.DEF.SVRCONN");

            Connection conn = mqcf.createConnection();
            Session session = conn.createSession(
                    batch,
                    Session.AUTO_ACKNOWLEDGE);

            conn.start();

            MessageProducer producer = session.createProducer(this.defaultDestination);
            Future<TestResult> futureResult = perfService.sendPayload(producer, session, count, batch, batch_size);
            while (!(futureResult.isDone())) {
                Thread.sleep(10); // 10-millisecond pause between each check
            }
            TestResult result = futureResult.get();
            LOGGER.debug("Produced " + result.count + " of " + result.expectedCount);

            MessageConsumer consumer = session.createConsumer(this.defaultDestination);
            futureResult = perfService.consumePayLoad(consumer, session, count, batch, batch_size);

            while (!(futureResult.isDone())) {
                Thread.sleep(1000); // 10-millisecond pause between each check
            }
            LOGGER.debug("Done consuming messages");

            result = futureResult.get();
            LOGGER.debug("Consumed " + result.count + " of " + result.expectedCount);

            Date endTime = new Date();
            long testTimeInMillis = endTime.getTime() - startTime.getTime();

            conn.stop();

            PerfTestResult pr = new PerfTestResult(count, "SUCCESS", testTimeInMillis, batch, batch_size, instance,
                    null);
            return new ResponseEntity<PerfTestResult>(pr, HttpStatus.OK);

        } catch (ExecutionException e) {
            System.err.println(e.getCause());

            PerfTestResult pr = new PerfTestResult(0,  e.getMessage(), 0, batch, batch_size, instance,
                    null);
            return new ResponseEntity<PerfTestResult>(pr, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (JMSException je) {
            MQException mqe = (MQException) je.getLinkedException();
            if (mqe != null) {
                System.err.println("cause:" + mqe.getCause());
                PerfTestResult pr = new PerfTestResult(0, je.getMessage(), 0, batch, batch_size, instance, mqe);
                return new ResponseEntity<PerfTestResult>(pr, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                PerfTestResult pr = new PerfTestResult(0, je.getMessage(), 0, batch, batch_size, instance, null);
            }
        }
        return new ResponseEntity<PerfTestResult>(new PerfTestResult(0, "Unknown" + e.getMessage(), 0, batch, batch_size, instance,
                null), HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
