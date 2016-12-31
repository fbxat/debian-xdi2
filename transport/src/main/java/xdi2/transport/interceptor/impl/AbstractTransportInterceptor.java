package xdi2.transport.interceptor.impl;

import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.execution.ExecutionContext;
import xdi2.messaging.target.interceptor.impl.AbstractInterceptor;
import xdi2.transport.Transport;
import xdi2.transport.TransportRequest;
import xdi2.transport.TransportResponse;
import xdi2.transport.exceptions.Xdi2TransportException;
import xdi2.transport.interceptor.TransportInterceptor;

public abstract class AbstractTransportInterceptor extends AbstractInterceptor<Transport<?, ?>> implements TransportInterceptor {

	public AbstractTransportInterceptor(int initPriority, int shutdownPriority) {

		super(initPriority, shutdownPriority);
	}

	public AbstractTransportInterceptor() {

		super();
	}

	@Override
	public boolean before(Transport<?, ?> transport, TransportRequest request, TransportResponse response, MessagingTarget messagingTarget, MessageEnvelope messageEnvelope, ExecutionContext executionContext) throws Xdi2TransportException {

		return false;
	}

	@Override
	public boolean after(Transport<?, ?> transport, TransportRequest request, TransportResponse response, MessagingTarget messagingTarget, MessageEnvelope messageEnvelope, MessagingResponse messagingResponse, ExecutionContext executionContext) throws Xdi2TransportException {

		return false;
	}

	@Override
	public void exception(Transport<?, ?> transport, TransportRequest request, TransportResponse response, MessagingTarget messagingTarget, MessageEnvelope messageEnvelope, MessagingResponse messagingResponse, Exception ex, ExecutionContext executionContext) {

	}
}
