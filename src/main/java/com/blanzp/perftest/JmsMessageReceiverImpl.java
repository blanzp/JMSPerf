package com.blanzp.perftest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class JmsMessageReceiverImpl implements JmsMessageReceiver {
	private final static Logger LOGGER = Logger
			.getLogger(JmsMessageReceiverImpl.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Override
	public Object receive() {
		return (Object) jmsTemplate.receiveAndConvert("Send2Recv");
	}
}
