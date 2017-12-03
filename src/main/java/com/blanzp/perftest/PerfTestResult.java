package com.blanzp.perftest;

/**
 * Created by paul on 12/3/17.
 */
public class PerfTestResult {

    public int msgCount;
    public String message;
    public long executionInMillis;
    public float msgRate;

    public PerfTestResult(int msgCount, String message, long executionInMillis ){
        this.msgCount = msgCount;
        this.message = message;
        this.executionInMillis = executionInMillis;
        msgRate = (float)msgCount / (float)executionInMillis * (float)1000;
    }
}
