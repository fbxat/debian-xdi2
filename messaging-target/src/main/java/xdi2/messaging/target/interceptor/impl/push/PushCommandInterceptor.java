package xdi2.messaging.target.interceptor.impl.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.nodetypes.XdiAbstractContext;
import xdi2.core.features.push.PushCommand;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIAddressUtil;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.operations.Operation;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.execution.ExecutionContext;
import xdi2.messaging.target.execution.ExecutionResult;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.messaging.target.interceptor.InterceptorResult;
import xdi2.messaging.target.interceptor.MessageEnvelopeInterceptor;
import xdi2.messaging.target.interceptor.OperationInterceptor;
import xdi2.messaging.target.interceptor.impl.AbstractInterceptor;

/**
 * This interceptor executes push commands while a message is executed.
 */
public class PushCommandInterceptor extends AbstractInterceptor<MessagingTarget> implements MessageEnvelopeInterceptor, OperationInterceptor, Prototype<PushCommandInterceptor> {

	private static final Logger log = LoggerFactory.getLogger(PushCommandInterceptor.class);

	private Graph pushCommandsGraph;
	private PushCommandExecutor pushCommandExecutor;

	public PushCommandInterceptor(Graph pushCommandsGraph, PushCommandExecutor pushCommandExecutor) {

		this.pushCommandsGraph = pushCommandsGraph;
		this.pushCommandExecutor = pushCommandExecutor;
	}

	public PushCommandInterceptor() {

		this.pushCommandsGraph = null;
		this.pushCommandExecutor = new BasicPushCommandExecutor();
	}

	/*
	 * Prototype
	 */

	@Override
	public PushCommandInterceptor instanceFor(PrototypingContext prototypingContext) {

		// create new interceptor

		PushCommandInterceptor interceptor = new PushCommandInterceptor();

		// set the graph

		interceptor.setPushCommandsGraph(this.getPushCommandsGraph());
		interceptor.setPushCommandExecutor(this.getPushCommandExecutor());

		// done

		return interceptor;
	}

	/*
	 * Init and shutdown
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		super.init(messagingTarget);

		if (this.getPushCommandsGraph() == null && messagingTarget instanceof GraphMessagingTarget) this.setPushCommandsGraph(((GraphMessagingTarget) messagingTarget).getGraph()); 
		if (this.getPushCommandsGraph() == null) throw new Xdi2MessagingException("No push commands graph.", null, null);
	}

	/*
	 * MessageEnvelopeInterceptor
	 */

	@Override
	public InterceptorResult before(MessageEnvelope messageEnvelope, ExecutionResult executionResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		resetWriteOperationsPerMessageEnvelope(executionContext);

		return InterceptorResult.DEFAULT;
	}

	@Override
	public InterceptorResult after(MessageEnvelope messageEnvelope, ExecutionResult executionResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		// create the push maps

		List<Operation> writeOperations = getWriteOperationsPerMessageEnvelope(executionContext);

		Map<PushCommand, Map<Operation, XDIAddress>> pushCommandsXDIAddressMap = new HashMap<PushCommand, Map<Operation, XDIAddress>> ();
		Map<PushCommand, Map<Operation, List<XDIStatement>>> pushCommandsXDIStatementMap = new HashMap<PushCommand, Map<Operation, List<XDIStatement>>> ();

		for (Operation writeOperation : writeOperations) {

			XDIAddress targetXDIAddress = writeOperation.getTargetXDIAddress();
			Iterator<XDIStatement> targetXDIStatements = writeOperation.getTargetXDIStatements();

			// look for push command for the operation's target address

			if (targetXDIAddress != null) {

				if (log.isDebugEnabled()) log.debug("For write operation " + writeOperation + " found target address " + targetXDIAddress);

				List<PushCommand> pushCommands = findPushCommands(this.getPushCommandsGraph(), targetXDIAddress);
				if (pushCommands == null || pushCommands.isEmpty()) continue;

				for (PushCommand pushCommand : pushCommands) {

					if (log.isDebugEnabled()) log.debug("For push command " + pushCommand + " processing target address " + targetXDIAddress);

					Map<Operation, XDIAddress> pushCommandXDIAddressMap = pushCommandsXDIAddressMap.get(pushCommand);
					if (pushCommandXDIAddressMap == null) { pushCommandXDIAddressMap = new HashMap<Operation, XDIAddress> (); pushCommandsXDIAddressMap.put(pushCommand, pushCommandXDIAddressMap); }

					pushCommandXDIAddressMap.put(writeOperation, targetXDIAddress);
				}
			}

			// look for push commands for the operation's target statements

			if (targetXDIStatements != null) {

				while (targetXDIStatements.hasNext()) {

					XDIStatement targetXDIStatement = targetXDIStatements.next();

					if (log.isDebugEnabled()) log.debug("For write operation " + writeOperation + " found target statement " + targetXDIStatement);

					List<PushCommand> pushCommands = findPushCommands(this.getPushCommandsGraph(), targetXDIStatement);
					if (pushCommands == null || pushCommands.isEmpty()) continue;

					for (PushCommand pushCommand : pushCommands) {

						if (log.isDebugEnabled()) log.debug("For push command " + pushCommand + " processing target statement " + targetXDIStatement);

						Map<Operation, List<XDIStatement>> pushCommandXDIStatementMap = pushCommandsXDIStatementMap.get(pushCommand);
						if (pushCommandXDIStatementMap == null) { pushCommandXDIStatementMap = new HashMap<Operation, List<XDIStatement>> (); pushCommandsXDIStatementMap.put(pushCommand, pushCommandXDIStatementMap); }

						List<XDIStatement> pushCommandXDIStatementList = pushCommandXDIStatementMap.get(writeOperation);
						if (pushCommandXDIStatementList == null) { pushCommandXDIStatementList = new ArrayList<XDIStatement> (); pushCommandXDIStatementMap.put(writeOperation, pushCommandXDIStatementList); }

						pushCommandXDIStatementList.add(targetXDIStatement);
					}
				}
			}
		}

		// execute the pushes

		Set<PushCommand> pushCommands = new HashSet<PushCommand> ();
		pushCommands.addAll(pushCommandsXDIAddressMap.keySet());
		pushCommands.addAll(pushCommandsXDIStatementMap.keySet());

		for (PushCommand pushCommand : pushCommands) {

			Map<Operation, XDIAddress> pushCommandXDIAddressMap = pushCommandsXDIAddressMap.get(pushCommand);
			Map<Operation, List<XDIStatement>> pushCommandXDIStatementMap = pushCommandsXDIStatementMap.get(pushCommand);

			Set<Operation> pushCommandOperations = new HashSet<Operation> ();
			if (pushCommandXDIAddressMap != null) pushCommandOperations.addAll(pushCommandXDIAddressMap.keySet());
			if (pushCommandXDIStatementMap != null) pushCommandOperations.addAll(pushCommandXDIStatementMap.keySet());

			try {

				if (log.isDebugEnabled()) log.debug("Executing push command " + pushCommand);

				this.getPushCommandExecutor().executePush(pushCommand, pushCommandOperations, pushCommandXDIAddressMap, pushCommandXDIStatementMap);
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Problem while executing push: " + ex.getMessage(), ex, executionContext);
			}
		}

		// done

		return InterceptorResult.DEFAULT;
	}

	@Override
	public void exception(MessageEnvelope messageEnvelope, ExecutionResult executionResult, ExecutionContext executionContext, Exception ex) {

	}

	/*
	 * OperationInterceptor
	 */

	@Override
	public InterceptorResult before(Operation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		// only care about write operations

		if (operation.isReadOnlyOperation()) return InterceptorResult.DEFAULT;

		// add the write address

		addWriteOperationPerMessageEnvelope(executionContext, operation);

		// done

		return InterceptorResult.DEFAULT;
	}

	@Override
	public InterceptorResult after(Operation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		// done

		return InterceptorResult.DEFAULT;
	}

	/*
	 * Getters and setters
	 */

	public Graph getPushCommandsGraph() {

		return this.pushCommandsGraph;
	}

	public void setPushCommandsGraph(Graph pushCommandsGraph) {

		this.pushCommandsGraph = pushCommandsGraph;
	}

	public PushCommandExecutor getPushCommandExecutor() {

		return this.pushCommandExecutor;
	}

	public void setPushCommandExecutor(PushCommandExecutor pushCommandExecutor) {

		this.pushCommandExecutor = pushCommandExecutor;
	}

	/*
	 * Helper methods
	 */

	private static List<PushCommand> findPushCommands(Graph pushCommandsGraph, XDIAddress XDIaddress) {

		List<PushCommand> pushCommands = new ArrayList<PushCommand> ();

		while (true) {

			ContextNode contextNode = pushCommandsGraph.getDeepContextNode(XDIaddress);
			PushCommand pushCommand = contextNode == null ? null : PushCommand.findPushCommand(XdiAbstractContext.fromContextNode(contextNode), false);

			if (pushCommand != null) pushCommands.add(pushCommand);

			if (XDIaddress.getNumXDIArcs() == 0) break;
			XDIaddress = XDIAddressUtil.parentXDIAddress(XDIaddress, -1);
		}

		return pushCommands;
	}

	private static List<PushCommand> findPushCommands(Graph pushCommandsGraph, XDIStatement XDIstatement) {

		return findPushCommands(pushCommandsGraph, XDIstatement.getContextNodeXDIAddress());
	}

	/*
	 * ExecutionContext helper methods
	 */

	private static final String EXECUTIONCONTEXT_KEY_WRITEOPERATIONS_PER_MESSAGEENVELOPE = PushCommandInterceptor.class.getCanonicalName() + "#writeoperationspermessageenvelope";

	@SuppressWarnings("unchecked")
	private static List<Operation> getWriteOperationsPerMessageEnvelope(ExecutionContext executionContext) {

		return (List<Operation>) executionContext.getMessageEnvelopeAttribute(EXECUTIONCONTEXT_KEY_WRITEOPERATIONS_PER_MESSAGEENVELOPE);
	}

	private static void addWriteOperationPerMessageEnvelope(ExecutionContext executionContext, Operation operation) {

		List<Operation> operations = getWriteOperationsPerMessageEnvelope(executionContext);

		operations.add(operation);

		if (log.isDebugEnabled()) log.debug("Set operation: " + operation);
	}

	private static void resetWriteOperationsPerMessageEnvelope(ExecutionContext executionContext) {

		executionContext.putMessageEnvelopeAttribute(EXECUTIONCONTEXT_KEY_WRITEOPERATIONS_PER_MESSAGEENVELOPE, new ArrayList<Operation> ());
	}

	/*
	 * WriteListener
	 */

	public interface WriteListener {

		public void onWrite(List<XDIAddress> targetAddresses);
	}
}
