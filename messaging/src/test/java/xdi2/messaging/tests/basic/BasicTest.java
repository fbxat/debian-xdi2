package xdi2.messaging.tests.basic;

import junit.framework.TestCase;
import xdi2.core.ContextNode;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.iterators.SingleItemIterator;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.constants.XDIMessagingConstants;
import xdi2.messaging.operations.DelOperation;
import xdi2.messaging.operations.GetOperation;
import xdi2.messaging.operations.Operation;
import xdi2.messaging.operations.SetOperation;

public class BasicTest extends TestCase {

	private static final XDIAddress SENDER = XDIAddress.create("=sender");

	private static final XDIAddress TARGET_ADDRESS = XDIAddress.create("=markus");
	private static final XDIStatement TARGET_STATEMENT = XDIStatement.create("=markus/+friend/=giovanni");

	private static final XDIAddress contextNodeXDIAddressS[] = new XDIAddress[] {
			XDIAddress.create("=markus#email"),
			XDIAddress.create("=markus"),
			XDIAddress.create("=markus+friends"),
			XDIAddress.create("=markus#name#last")
	};

	public void testMessagingOverview() throws Exception {

		// create a message envelope

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		assertTrue(MessageEnvelope.isValid(messageEnvelope.getGraph()));

		assertFalse(messageEnvelope.getMessageCollections().hasNext());
		assertNull(messageEnvelope.getMessageCollection(SENDER, false));
		assertEquals(messageEnvelope.getMessageCollectionCount(), 0);
		assertFalse(messageEnvelope.getMessages().hasNext());
		assertFalse(messageEnvelope.getMessages(SENDER).hasNext());
		assertEquals(messageEnvelope.getMessageCount(), 0);

		// create a message collection

		MessageCollection messageCollection = messageEnvelope.getMessageCollection(SENDER, true);

		assertTrue(MessageCollection.isValid(messageCollection.getXdiEntityCollection()));

		assertTrue(messageEnvelope.getMessageCollections().hasNext());
		assertNotNull(messageEnvelope.getMessageCollection(SENDER, false));
		assertEquals(messageEnvelope.getMessageCollectionCount(), 1);
		assertFalse(messageEnvelope.getMessages().hasNext());
		assertFalse(messageEnvelope.getMessages(SENDER).hasNext());
		assertEquals(messageEnvelope.getMessageCount(), 0);

		assertFalse(messageCollection.getMessages().hasNext());
		assertEquals(messageCollection.getMessageCount(), 0);

		// create a message

		Message message = messageCollection.createMessage();

		assertTrue(Message.isValid(message.getXdiEntity()));

		assertTrue(messageEnvelope.getMessageCollections().hasNext());
		assertNotNull(messageEnvelope.getMessageCollection(SENDER, false));
		assertEquals(messageEnvelope.getMessageCollectionCount(), 1);
		assertTrue(messageEnvelope.getMessages().hasNext());
		assertTrue(messageEnvelope.getMessages(SENDER).hasNext());
		assertEquals(messageEnvelope.getMessageCount(), 1);

		assertTrue(messageCollection.getMessages().hasNext());
		assertEquals(messageCollection.getMessageCount(), 1);

		assertFalse(message.getOperations().hasNext());
		assertEquals(message.getOperationCount(), 0);

		// create some operations

		ContextNode[] contextNodes = new ContextNode[contextNodeXDIAddressS.length]; 
		for (int i=0; i<contextNodeXDIAddressS.length; i++) contextNodes[i] = messageEnvelope.getGraph().setDeepContextNode(contextNodeXDIAddressS[i]);

		Operation setOperation = message.createSetOperation(contextNodes[0].getXDIAddress());
		Operation getOperation = message.createGetOperation(contextNodes[1].getXDIAddress());
		Operation delOperation = message.createDelOperation(contextNodes[2].getXDIAddress());

		assertTrue(messageCollection.equals(messageEnvelope.getMessageCollection(SENDER, false)));
		assertTrue(message.equals(messageCollection.getMessages().next()));
		assertTrue(setOperation.equals(message.getSetOperations().next()));
		assertTrue(getOperation.equals(message.getGetOperations().next()));
		assertTrue(delOperation.equals(message.getDelOperations().next()));

		assertEquals(messageEnvelope.getMessageCount(), 1);
		assertEquals(messageEnvelope.getOperationCount(), 3);
		assertEquals(messageCollection.getMessageCount(), 1);
		assertEquals(messageCollection.getOperationCount(), 3);
		assertEquals(message.getOperationCount(), 3);
		assertEquals(messageCollection.getSenderXDIAddress(), SENDER);
		assertEquals(message.getSenderXDIAddress(), SENDER);
		assertEquals(setOperation.getMessage().getSenderXDIAddress(), SENDER);
		assertEquals(getOperation.getMessage().getSenderXDIAddress(), SENDER);
		assertEquals(delOperation.getMessage().getSenderXDIAddress(), SENDER);
		assertTrue(setOperation instanceof SetOperation);
		assertTrue(getOperation instanceof GetOperation);
		assertTrue(delOperation instanceof DelOperation);
	}

	public void testMessagingFromOperationAddressAndTargetAddress() throws Exception {

		MessageEnvelope messageEnvelope = MessageEnvelope.fromOperationXDIAddressAndTargetXDIAddress(XDIMessagingConstants.XDI_ADD_SET, TARGET_ADDRESS);
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(XDIMessagingConstants.XDI_ADD_ANONYMOUS, false);
		Message message = messageCollection.getMessages().next();
		Operation operation = message.getSetOperations().next();

		assertEquals(messageEnvelope.getMessageCount(), 1);
		assertEquals(messageEnvelope.getOperationCount(), 1);
		assertEquals(messageCollection.getMessageCount(), 1);
		assertEquals(messageCollection.getOperationCount(), 1);
		assertEquals(message.getOperationCount(), 1);
		assertEquals(messageCollection.getSenderXDIAddress(), XDIMessagingConstants.XDI_ADD_ANONYMOUS);
		assertEquals(message.getSenderXDIAddress(), XDIMessagingConstants.XDI_ADD_ANONYMOUS);
		assertEquals(operation.getMessage().getSenderXDIAddress(), XDIMessagingConstants.XDI_ADD_ANONYMOUS);
		assertEquals(operation.getOperationXDIAddress(), XDIMessagingConstants.XDI_ADD_SET);
		assertEquals(operation.getTargetXDIAddress(), TARGET_ADDRESS);
		assertTrue(operation instanceof SetOperation);
	}

	public void testMessagingFromOperationAddressAndTargetStatement() throws Exception {

		MessageEnvelope messageEnvelope = MessageEnvelope.fromOperationXDIAddressAndTargetXDIStatements(XDIMessagingConstants.XDI_ADD_SET, new SingleItemIterator<XDIStatement> (TARGET_STATEMENT));
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(XDIMessagingConstants.XDI_ADD_ANONYMOUS, false);
		Message message = messageCollection.getMessages().next();
		Operation operation = message.getSetOperations().next();

		assertEquals(messageEnvelope.getMessageCount(), 1);
		assertEquals(messageEnvelope.getOperationCount(), 1);
		assertEquals(messageCollection.getMessageCount(), 1);
		assertEquals(messageCollection.getOperationCount(), 1);
		assertEquals(message.getOperationCount(), 1);
		assertEquals(messageCollection.getSenderXDIAddress(), XDIMessagingConstants.XDI_ADD_ANONYMOUS);
		assertEquals(message.getSenderXDIAddress(), XDIMessagingConstants.XDI_ADD_ANONYMOUS);
		assertEquals(operation.getMessage().getSenderXDIAddress(), XDIMessagingConstants.XDI_ADD_ANONYMOUS);
		assertEquals(operation.getOperationXDIAddress(), XDIMessagingConstants.XDI_ADD_SET);
		assertEquals(operation.getTargetXDIStatements().next(), TARGET_STATEMENT);
		assertTrue(operation instanceof SetOperation);
	}

	public void testSenderAndRecipientAddress() throws Exception {

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		Message message = messageEnvelope.createMessage(XDIAddress.create("=sender"));
		message.setFromPeerRootXDIArc(XDIArc.create("(=!1111)"));
		message.setToPeerRootXDIArc(XDIArc.create("(=!2222)"));
		assertEquals(message.getFromPeerRootXDIArc(), XDIAddress.create("(=!1111)"));
		assertEquals(message.getToPeerRootXDIArc(), XDIAddress.create("(=!2222)"));
	}
}
