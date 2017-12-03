package com.blanzp.perftest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    PerfTestResult runPerf(
            @RequestParam(value = "count", required = false, defaultValue = "10") int count)
            throws InterruptedException {

        int testCount = count;

        Date startTime = new Date();
        Future<String> result = perfService.sendPayload(testCount);
        while (!(result.isDone())) {
            Thread.sleep(1); // 10-millisecond pause between each check
//            System.out.println("Waiting for Long Process...");
        }
        String pubResp= "";
        try {
            pubResp = result.get();
        } catch (ExecutionException e) {
            System.out.println("Failed to produce messages" + e);
            return new PerfTestResult(0, "Failed to produce: " + e.getMessage(), 0);
        }

        System.out.println("Done producing messages");
        Future<String> s = perfService.consumePayLoad(testCount);
        while (!(s.isDone())) {
            Thread.sleep(1); // 10-millisecond pause between each check
//            System.out.println("Waiting for reading...");
        }
        System.out.println("Done consuming messages");

        String readResult = "";

        try {
            readResult = s.get();
        } catch (ExecutionException e) {
            System.out.println("Failed to read response messages" + e);
            return new PerfTestResult(0, "Failed to consume: " + e.getMessage(), 0);
        }

        Date endTime = new Date();

        long testTimeInMillis = endTime.getTime() - startTime.getTime();
        return new PerfTestResult(count, "OK", testTimeInMillis);
    }

}
