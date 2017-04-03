package xdi2.client;

import java.util.Collection;

import xdi2.client.exceptions.Xdi2AgentException;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.manipulator.Manipulator;
import xdi2.core.ContextNode;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;

public interface XDIClientRoute <CLIENT extends XDIClient<? extends MessagingResponse>> {

	public XDIArc getToPeerRootXDIArc();
	public CLIENT constructXDIClient();

	public MessageEnvelope createMessageEnvelope();
	public Message createMessage(MessageEnvelope messageEnvelope, XDIAddress senderXDIAddress, long index);
	public Message createMessage(MessageEnvelope messageEnvelope, XDIAddress senderXDIAddress);
	public Message createMessage(MessageEnvelope messageEnvelope, long index);
	public Message createMessage(MessageEnvelope messageEnvelope);
	public Message createMessage(XDIAddress senderXDIAddress, long index);
	public Message createMessage(XDIAddress senderXDIAddress);
	public Message createMessage(long index);
	public Message createMessage();

	/*
	 * $get helper methods
	 */

	public ContextNode get(XDIAddress XDIaddress, XDIAddress senderXDIAddress, Collection<Manipulator> manipulators) throws Xdi2AgentException, Xdi2ClientException;
	public ContextNode get(XDIAddress XDIaddress, XDIAddress senderXDIAddress, Manipulator[] manipulators) throws Xdi2AgentException, Xdi2ClientException;
	public ContextNode get(XDIAddress XDIaddress, XDIAddress senderXDIAddress, Manipulator manipulator) throws Xdi2AgentException, Xdi2ClientException;
	public ContextNode get(XDIAddress XDIaddress, XDIAddress senderXDIAddress) throws Xdi2AgentException, Xdi2ClientException;
	public ContextNode get(XDIAddress XDIaddress, Collection<Manipulator> manipulators) throws Xdi2AgentException, Xdi2ClientException;
	public ContextNode get(XDIAddress XDIaddress, Manipulator[] manipulators) throws Xdi2AgentException, Xdi2ClientException;
	public ContextNode get(XDIAddress XDIaddress, Manipulator manipulator) throws Xdi2AgentException, Xdi2ClientException;
	public ContextNode get(XDIAddress XDIaddress) throws Xdi2AgentException, Xdi2ClientException;
}
