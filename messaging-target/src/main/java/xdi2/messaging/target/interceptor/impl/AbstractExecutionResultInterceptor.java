package xdi2.messaging.target.interceptor.impl;

import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.execution.ExecutionContext;
import xdi2.messaging.target.execution.ExecutionResult;
import xdi2.messaging.target.interceptor.ExecutionResultInterceptor;

public abstract class AbstractExecutionResultInterceptor extends AbstractInterceptor<MessagingTarget> implements ExecutionResultInterceptor {

	public AbstractExecutionResultInterceptor(int initPriority, int shutdownPriority) {

		super(initPriority, shutdownPriority);
	}

	public AbstractExecutionResultInterceptor() {

		super();
	}

	@Override
	public void finish(MessagingTarget messagingTarget, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

	}
}
