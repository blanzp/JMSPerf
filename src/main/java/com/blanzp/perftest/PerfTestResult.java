package com.blanzp.perftest;

import com.ibm.mq.MQException;

import java.util.Date;

/**
 * Created by paul on 12/3/17.
 */
public class PerfTestResult {

    public int msgCount;
    public String message;
    public long executionInMillis;
    public float msgRate;
    public Date testDate;
    public String testDateString;
    public boolean batch;
    public int batch_size;
    public String instance;
    public MQError mqError;

    public PerfTestResult(int msgCount, String message, long executionInMillis, boolean batch, int batch_size, String instance,
                          MQException e){
        this.msgCount = msgCount;
        this.message = message;
        this.executionInMillis = executionInMillis;
        msgRate = (float)msgCount / (float)executionInMillis * (float)1000;
        this.testDate = new Date();
        this.testDateString = this.testDate.toString();
        this.batch = batch;
        this.batch_size = batch_size;
        this.instance = instance;
        this.mqError = new MQError(e);
    }
}
