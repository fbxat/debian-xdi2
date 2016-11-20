package xdi2.messaging;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.LiteralNode;
import xdi2.core.Relation;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.constants.XDIPolicyConstants;
import xdi2.core.constants.XDISecurityConstants;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.dictionary.Dictionary;
import xdi2.core.features.digests.Digest;
import xdi2.core.features.digests.Digests;
import xdi2.core.features.linkcontracts.instance.ConnectLinkContract;
import xdi2.core.features.linkcontracts.instance.LinkContract;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.features.linkcontracts.instance.RootLinkContract;
import xdi2.core.features.linkcontracts.instance.SendLinkContract;
import xdi2.core.features.nodetypes.XdiAbstractContext;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.features.nodetypes.XdiEntity;
import xdi2.core.features.nodetypes.XdiEntitySingleton;
import xdi2.core.features.nodetypes.XdiInnerRoot;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.features.nodetypes.XdiSubGraph;
import xdi2.core.features.policy.PolicyRoot;
import xdi2.core.features.signatures.Signature;
import xdi2.core.features.signatures.Signatures;
import xdi2.core.features.timestamps.Timestamps;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.iterators.IteratorCounter;
import xdi2.core.util.iterators.IteratorListMaker;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.util.iterators.MappingXDIStatementIterator;
import xdi2.core.util.iterators.NotNullIterator;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.core.util.iterators.SelectingIterator;
import xdi2.core.util.iterators.SelectingNotImpliedStatementIterator;
import xdi2.core.util.iterators.SingleItemIterator;
import xdi2.messaging.constants.XDIMessagingConstants;
import xdi2.messaging.operations.ConnectOperation;
import xdi2.messaging.operations.DelOperation;
import xdi2.messaging.operations.DoOperation;
import xdi2.messaging.operations.GetOperation;
import xdi2.messaging.operations.Operation;
import xdi2.messaging.operations.PushOperation;
import xdi2.messaging.operations.SendOperation;
import xdi2.messaging.operations.SetOperation;
import xdi2.messaging.util.MessagingCloneUtil;

/**
 * An XDI message, represented as a context node.
 * 
 * @author markus
 */
public abstract class MessageBase <N extends XdiSubGraph<? super N>> implements Serializable, Comparable<MessageBase<N>> {

	private static final long serialVersionUID = 7063040731631258931L;

	/*
	 * Instance methods
	 */

	public abstract N getXdiSubGraph();

	/**
	 * Returns the underlying context node to which this XDI message (template) is bound.
	 * @return A context node that represents the XDI message (template).
	 */
	public ContextNode getContextNode() {

		return this.getXdiSubGraph().getContextNode();
	}

	/**
	 * Return the FROM peer root arc of the message (template).
	 */
	public XDIArc getFromPeerRootXDIArc() {

		Relation fromPeerRootXDIArcRelation = this.getContextNode().getRelation(XDIMessagingConstants.XDI_ADD_FROM_PEER_ROOT_ARC);
		if (fromPeerRootXDIArcRelation == null) return null;

		XDIAddress fromPeerRootXDIAddress = fromPeerRootXDIArcRelation.getTargetXDIAddress();
		if (fromPeerRootXDIAddress.getNumXDIArcs() > 1 || ! XdiPeerRoot.isValidXDIArc(fromPeerRootXDIAddress.getFirstXDIArc())) return null;

		return fromPeerRootXDIAddress.getFirstXDIArc();
	}

	/**
	 * Return the FROM address of the message (template).
	 */
	public XDIAddress getFromXDIAddress() {

		XDIArc fromPeerRootXDIArc = this.getFromPeerRootXDIArc();
		if (fromPeerRootXDIArc == null) return null;

		return XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(fromPeerRootXDIArc);
	}

	/**
	 * Set the FROM peer root arc of the message (template).
	 */
	public void setFromPeerRootXDIArc(XDIArc fromPeerRootXDIArc) {

		this.getContextNode().delRelations(XDIMessagingConstants.XDI_ADD_FROM_PEER_ROOT_ARC);

		if (fromPeerRootXDIArc != null) {

			this.getContextNode().setRelation(XDIMessagingConstants.XDI_ADD_FROM_PEER_ROOT_ARC, XDIAddress.fromComponent(fromPeerRootXDIArc));
		}
	}

	/**
	 * Set the FROM address of the message (template).
	 */
	public void setFromXDIAddress(XDIAddress fromXDIAddress) {

		XDIArc fromPeerRootXDIArc = XdiPeerRoot.createPeerRootXDIArc(fromXDIAddress);

		this.setFromPeerRootXDIArc(fromPeerRootXDIArc);
	}

	/**
	 * Return the TO peer root arc of the message (template).
	 */
	public XDIArc getToPeerRootXDIArc() {

		Relation toPeerRootXDIArcRelation = this.getContextNode().getRelation(XDIMessagingConstants.XDI_ADD_TO_PEER_ROOT_ARC);
		if (toPeerRootXDIArcRelation == null) return null;

		XDIAddress toPeerRootXDIAddress = toPeerRootXDIArcRelation.getTargetXDIAddress();
		if (toPeerRootXDIAddress.getNumXDIArcs() > 1 || ! XdiPeerRoot.isValidXDIArc(toPeerRootXDIAddress.getFirstXDIArc())) return null;

		return toPeerRootXDIAddress.getFirstXDIArc();
	}

	/**
	 * Return the TO address of the message (template).
	 */
	public XDIAddress getToXDIAddress() {

		XDIArc toPeerRootXDIArc = this.getToPeerRootXDIArc();
		if (toPeerRootXDIArc == null) return null;

		return XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(toPeerRootXDIArc);
	}

	/**
	 * Set the TO peer root arc of the message (template).
	 */
	public void setToPeerRootXDIArc(XDIArc toPeerRootXDIArc) {

		this.getContextNode().delRelations(XDIMessagingConstants.XDI_ADD_TO_PEER_ROOT_ARC);

		if (toPeerRootXDIArc != null) {

			this.getContextNode().setRelation(XDIMessagingConstants.XDI_ADD_TO_PEER_ROOT_ARC, XDIAddress.fromComponent(toPeerRootXDIArc));
		}
	}

	/**
	 * Set the TO address of the message (template).
	 */
	public void setToXDIAddress(XDIAddress toXDIAddress) {

		XDIArc toPeerRootXDIArc = XdiPeerRoot.createPeerRootXDIArc(toXDIAddress);

		this.setToPeerRootXDIArc(toPeerRootXDIArc);
	}

	/**
	 * Returns the timestamp.
	 * @return The timestamp.
	 */
	public Date getTimestamp() {

		return Timestamps.getTimestamp(XdiAbstractContext.fromContextNode(this.getContextNode()));
	}

	/**
	 * Set the timestamp.
	 */
	public void setTimestamp(Date timestamp) {

		Timestamps.setTimestamp(XdiAbstractContext.fromContextNode(this.getContextNode()), timestamp);
	}

	/**
	 * Returns the link contract address.
	 * @return The link contract address.
	 */
	public XDIAddress getLinkContractXDIAddress() {

		Relation linkContractRelation = this.getContextNode().getRelation(XDILinkContractConstants.XDI_ADD_CONTRACT);
		if (linkContractRelation == null) return null;

		return linkContractRelation.getTargetXDIAddress();
	}

	/**
	 * Set the link contract address.
	 */
	public void setLinkContractXDIAddress(XDIAddress linkContractXDIAddress) {

		this.getContextNode().delRelations(XDILinkContractConstants.XDI_ADD_CONTRACT);

		if (linkContractXDIAddress != null) {

			this.getContextNode().setRelation(XDILinkContractConstants.XDI_ADD_CONTRACT, linkContractXDIAddress);
		}
	}

	/**
	 * Set the link contract.
	 */
	public void setLinkContract(LinkContract linkContract) {

		this.setLinkContractXDIAddress(linkContract == null ? null : linkContract.getXdiEntity().getXDIAddress());
	}

	/**
	 * Set the link contract class.
	 */
	public void setLinkContractClass(Class<? extends LinkContract> clazz) {

		XDIAddress ownerXDIAddress = XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(this.getToPeerRootXDIArc());
		if (ownerXDIAddress == null) throw new Xdi2RuntimeException("No TO peer root arc has been set yet.");

		if (clazz == null) {

			this.setLinkContractXDIAddress(null);
		} else if (RootLinkContract.class.isAssignableFrom(clazz)) {

			this.setLinkContractXDIAddress(RootLinkContract.createRootLinkContractXDIAddress(ownerXDIAddress));
		} else if (PublicLinkContract.class.isAssignableFrom(clazz)) {

			this.setLinkContractXDIAddress(PublicLinkContract.createPublicLinkContractXDIAddress(ownerXDIAddress));
		} else if (ConnectLinkContract.class.isAssignableFrom(clazz)) {

			this.setLinkContractXDIAddress(ConnectLinkContract.createConnectLinkContractXDIAddress(ownerXDIAddress));
		} else if (SendLinkContract.class.isAssignableFrom(clazz)) {

			this.setLinkContractXDIAddress(SendLinkContract.createSendLinkContractXDIAddress(ownerXDIAddress));
		} else {

			throw new Xdi2RuntimeException("Cannot automatically set link contract of type " + clazz.getSimpleName());
		}
	}

	/**
	 * Returns an existing XDI root policy in this XDI message (template), or creates a new one.
	 * @param create Whether to create an XDI root policy if it does not exist.
	 * @return The existing or newly created XDI root policy.
	 */
	public PolicyRoot getPolicyRoot(boolean create) {

		XdiEntitySingleton xdiEntitySingleton = this.getXdiSubGraph().getXdiEntitySingleton(XDIPolicyConstants.XDI_ARC_IF, create);
		if (xdiEntitySingleton == null) return null;

		return PolicyRoot.fromXdiEntity(xdiEntitySingleton);
	}

	/**
	 * Set this message (template)'s correlation to another message (template).
	 */
	public void setCorrelationXDIAddress(XDIAddress correlationXDIAddress) {

		this.getContextNode().setRelation(XDIMessagingConstants.XDI_ADD_CORRELATION, correlationXDIAddress);
	}

	/**
	 * Get this message (template)'s correlation to another message (template).
	 */
	public XDIAddress getCorrelationXDIAddress() {

		Relation relation = this.getContextNode().getRelation(XDIMessagingConstants.XDI_ADD_CORRELATION);
		if (relation == null) return null;

		return relation.getTargetXDIAddress();
	}

	/*
	 * Message parameters
	 */

	/**
	 * Sets a parameter value of this message (template).
	 * @param parameterXDIAddress The parameter identifier.
	 * @param parameterValue The parameter value.
	 */
	public void setParameter(XDIAddress parameterXDIAddress, Object parameterValue) {

		XdiAttributeSingleton parameterXdiAttribute = this.getXdiSubGraph().getXdiAttributeSingleton(parameterXDIAddress, true);

		parameterXdiAttribute.setLiteralData(parameterValue);
	}

	/**
	 * Returns a parameter value of this message (template).
	 * @param parameterXDIAddress The parameter identifier.
	 * @return The parameter value.
	 */
	public Object getParameter(XDIAddress parameterXDIAddress) {

		LiteralNode parameterLiteralNode = this.getParameterLiteral(parameterXDIAddress);
		if (parameterLiteralNode == null) return null;

		return parameterLiteralNode.getLiteralData();
	}

	/**
	 * Returns a parameter value string of this message (template).
	 * @param parameterXDIAddress The parameter identifier.
	 * @return The parameter value string.
	 */
	public String getParameterString(XDIAddress parameterXDIAddress) {

		LiteralNode parameterLiteralNode = this.getParameterLiteral(parameterXDIAddress);
		if (parameterLiteralNode == null) return null;

		return parameterLiteralNode.getLiteralDataString();
	}

	/**
	 * Returns a parameter value number of this message (template).
	 * @param parameterXDIAddress The parameter identifier.
	 * @return The parameter value number.
	 */
	public Number getParameterNumber(XDIAddress parameterXDIAddress) {

		LiteralNode parameterLiteralNode = this.getParameterLiteral(parameterXDIAddress);
		if (parameterLiteralNode == null) return null;

		return parameterLiteralNode.getLiteralDataNumber();
	}

	/**
	 * Returns a parameter value boolean of this message (template).
	 * @param parameterXDIAddress The parameter identifier.
	 * @return The parameter value boolean.
	 */
	public Boolean getParameterBoolean(XDIAddress parameterXDIAddress) {

		LiteralNode parameterLiteralNode = this.getParameterLiteral(parameterXDIAddress);
		if (parameterLiteralNode == null) return null;

		return parameterLiteralNode.getLiteralDataBoolean();
	}

	private LiteralNode getParameterLiteral(XDIAddress parameterXDIAddress) {

		XdiAttributeSingleton parameterXdiAttribute = this.getXdiSubGraph().getXdiAttributeSingleton(parameterXDIAddress, false);
		if (parameterXdiAttribute == null) return null;

		LiteralNode parameterLiteralNode = parameterXdiAttribute.getLiteralNode();
		if (parameterLiteralNode == null) return null;

		return parameterLiteralNode;
	}

	/*
	 * Methods releated to message authentication
	 */

	/**
	 * Set a secret token on the message (template).
	 * @param secretToken The secret token to set.
	 */
	public void setSecretToken(String secretToken) {

		if (secretToken != null) {

			XdiAttributeSingleton xdiAttribute = XdiAttributeSingleton.fromContextNode(this.getContextNode().setDeepContextNode(XDISecurityConstants.XDI_ADD_SECRET_TOKEN));
			xdiAttribute.setLiteralData(secretToken);
		} else {

			XdiAttributeSingleton xdiAttribute = XdiAttributeSingleton.fromContextNode(this.getContextNode().getDeepContextNode(XDISecurityConstants.XDI_ADD_SECRET_TOKEN, true));
			LiteralNode literalNode = xdiAttribute == null ? null : xdiAttribute.getLiteralNode();
			if (literalNode != null) literalNode.delete();
		}
	}

	/**
	 * Returns the secret token from the message (template).
	 * @return The secret token.
	 */
	public String getSecretToken() {

		ContextNode contextNode = this.getContextNode().getDeepContextNode(XDISecurityConstants.XDI_ADD_SECRET_TOKEN, true);
		if (contextNode == null) return null;

		XdiAttributeSingleton xdiAttribute = XdiAttributeSingleton.fromContextNode(contextNode);
		if (xdiAttribute == null) return null;

		LiteralNode literalNode = xdiAttribute.getLiteralNode();
		if (literalNode == null) return null;

		return literalNode.getLiteralDataString();
	}

	/**
	 * Returns the signatures from the message (template).
	 * @return The signatures.
	 */
	public ReadOnlyIterator<Signature> getSignatures() {

		return Signatures.getSignatures(this.getContextNode());
	}

	/**
	 * Returns the digests from the message (template).
	 * @return The digests.
	 */
	public ReadOnlyIterator<Digest> getDigests() {

		return Digests.getDigests(this.getContextNode());
	}

	/**
	 * Sets a signature on the message (template).
	 * @return The signature.
	 */
	public Signature createSignature(String digestAlgorithm, Integer digestLength, String keyAlgorithm, Integer keyLength, boolean singleton) {

		return Signatures.createSignature(this.getContextNode(), digestAlgorithm, digestLength, keyAlgorithm, keyLength, singleton);
	}

	/*
	 * Methods related to message types
	 */

	public Iterator<XDIAddress> getMessageTypes() {

		return Dictionary.getContextNodeTypes(this.getContextNode());
	}

	public XDIAddress getMessageType() {

		return Dictionary.getContextNodeType(this.getContextNode());
	}

	public boolean isMessageType(XDIAddress messageType) {

		return Dictionary.isContextNodeType(this.getContextNode(), messageType);
	}

	public void setMessageType(XDIAddress messageType) {

		Dictionary.setContextNodeType(this.getContextNode(), messageType);
	}

	public void delMessageType(XDIAddress messageType) {

		Dictionary.delContextNodeType(this.getContextNode(), messageType);
	}

	public void delMessageTypes() {

		Dictionary.delContextNodeTypes(this.getContextNode());
	}

	public void replaceMessageType(XDIAddress messageType) {

		Dictionary.replaceContextNodeType(this.getContextNode(), messageType);
	}

	/*
	 * Methods related to operations
	 */

	/**
	 * Returns the XDI entity with XDI operations.
	 * @return A XDI entity with XDI operations.
	 */
	public XdiEntity getOperationsXdiEntity() {

		return this.getXdiSubGraph().getXdiEntitySingleton(XDIMessagingConstants.XDI_ARC_DO, true);
	}

	/**
	 * Returns the context node with XDI operations.
	 * @return A context node with XDI operations.
	 */
	public ContextNode getOperationsContextNode() {

		return this.getOperationsXdiEntity().getContextNode();
	}

	/**
	 * Creates a new operation and adds it to this XDI message (template).
	 * @param operationXDIAddress The operation address to use for the new operation.
	 * @param targetXDIAddress The target address to which the operation applies.
	 * @return The newly created, empty operation, or null if the operation address is not valid.
	 */
	public Operation createOperation(XDIAddress operationXDIAddress, XDIAddress targetXDIAddress) {

		Relation relation = this.getOperationsContextNode().setRelation(operationXDIAddress, targetXDIAddress);

		return Operation.fromMessageBaseAndRelation(this, relation);
	}

	/**
	 * Creates a new operation and adds it to this XDI message (template).
	 * @param operationXDIAddress The operation address to use for the new operation.
	 * @param targetXDIStatements The target statements to which the operation applies.
	 * @return The newly created, empty operation, or null if the operation address is not valid.
	 */
	public Operation createOperation(XDIAddress operationXDIAddress, Iterator<XDIStatement> targetXDIStatements) {

		XdiInnerRoot xdiInnerRoot = XdiEntitySingleton.fromContextNode(this.getOperationsContextNode()).getXdiInnerRoot(operationXDIAddress, true);
		if (targetXDIStatements != null) while (targetXDIStatements.hasNext()) xdiInnerRoot.getContextNode().setStatement(targetXDIStatements.next());

		return Operation.fromMessageBaseAndRelation(this, xdiInnerRoot.getPredicateRelation());
	}

	/**
	 * Creates a new operation and adds it to this XDI message (template).
	 * @param operationXDIAddress The operation address to use for the new operation.
	 * @param targetXDIStatementAddress The target statement to which the operation applies.
	 * @return The newly created, empty operation, or null if the operation address is not valid.
	 */
	public Operation createOperation(XDIAddress operationXDIAddress, XDIStatement targetXDIStatementAddress) {

		return this.createOperation(operationXDIAddress, new SingleItemIterator<XDIStatement> (targetXDIStatementAddress));
	}

	/**
	 * Creates a new operation and adds it to this XDI message (template).
	 * @param operationXDIAddress The operation address to use for the new operation.
	 * @param targetGraph The target graph with statements to which this operation applies.
	 * @return The newly created, empty operation, or null if the operation address is not valid.
	 */
	public Operation createOperation(XDIAddress operationXDIAddress, Graph targetGraph) {

		return this.createOperation(operationXDIAddress, new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(targetGraph.getAllStatements())));
	}

	/**
	 * Creates a new operation and adds it to this XDI message (template).
	 * @param operationXDIAddress The operation address to use for the new operation.
	 * @param target The target address or target statement to which the operation applies.
	 * @return The newly created, empty operation, or null if the operation address is not valid.
	 */
	public Operation createOperation(XDIAddress operationXDIAddress, String target) {

		try {

			return this.createOperation(operationXDIAddress, XDIAddress.create(target));
		} catch (Exception ex) {

			return this.createOperation(operationXDIAddress, XDIStatement.create(target));
		}
	}

	/**
	 * Creates a new $get operation and adds it to this XDI message (template).
	 * @param targetXDIAddress The target address to which the operation applies.
	 * @return The newly created $get operation.
	 */
	public GetOperation createGetOperation(XDIAddress targetXDIAddress) {

		Relation relation = this.getOperationsContextNode().setRelation(XDIMessagingConstants.XDI_ADD_GET, targetXDIAddress);

		return GetOperation.fromMessageBaseAndRelation(this, relation);
	}

	/**
	 * Creates a new $get operation and adds it to this XDI message (template).
	 * @param targetXDIStatements The target statements to which the operation applies.
	 * @return The newly created $get operation.
	 */
	public GetOperation createGetOperation(Iterator<XDIStatement> targetXDIStatements) {

		XdiInnerRoot xdiInnerRoot = XdiEntitySingleton.fromContextNode(this.getOperationsContextNode()).getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_GET, true);
		if (targetXDIStatements != null) while (targetXDIStatements.hasNext()) xdiInnerRoot.getContextNode().setStatement(targetXDIStatements.next());

		return GetOperation.fromMessageBaseAndRelation(this, xdiInnerRoot.getPredicateRelation());
	}

	/**
	 * Creates a new $get operation and adds it to this XDI message (template).
	 * @param targetXDIStatement The target statement to which the operation applies.
	 * @return The newly created $get operation.
	 */
	public GetOperation createGetOperation(XDIStatement targetXDIStatement) {

		return this.createGetOperation(new SingleItemIterator<XDIStatement> (targetXDIStatement));
	}

	/**
	 * Creates a new $get operation and adds it to this XDI message (template).
	 * @param targetGraph The target graph with statements to which this operation applies.
	 * @return The newly created $get operation.
	 */
	public GetOperation createGetOperation(Graph targetGraph) {

		return this.createGetOperation(new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(targetGraph.getAllStatements())));
	}

	/**
	 * Creates a new $set operation and adds it to this XDI message (template).
	 * @param targetXDIAddress The target address to which the operation applies.
	 * @return The newly created $set operation.
	 */
	public SetOperation createSetOperation(XDIAddress targetXDIAddress) {

		Relation relation = this.getOperationsContextNode().setRelation(XDIMessagingConstants.XDI_ADD_SET, targetXDIAddress);

		return SetOperation.fromMessageBaseAndRelation(this, relation);
	}

	/**
	 * Creates a new $set operation and adds it to this XDI message (template).
	 * @param targetXDIStatements The target statements to which the operation applies.
	 * @return The newly created $set operation.
	 */
	public SetOperation createSetOperation(Iterator<XDIStatement> targetXDIStatements) {

		XdiInnerRoot xdiInnerRoot = XdiEntitySingleton.fromContextNode(this.getOperationsContextNode()).getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_SET, true);
		if (targetXDIStatements != null) while (targetXDIStatements.hasNext()) xdiInnerRoot.getContextNode().setStatement(targetXDIStatements.next());

		return SetOperation.fromMessageBaseAndRelation(this, xdiInnerRoot.getPredicateRelation());
	}

	/**
	 * Creates a new $set operation and adds it to this XDI message (template).
	 * @param targetXDIStatement The target statement to which the operation applies.
	 * @return The newly created $set operation.
	 */
	public SetOperation createSetOperation(XDIStatement targetXDIStatement) {

		return this.createSetOperation(new SingleItemIterator<XDIStatement> (targetXDIStatement));
	}

	/**
	 * Creates a new $set operation and adds it to this XDI message (template).
	 * @param targetGraph The target graph with statements to which this operation applies.
	 * @return The newly created $set operation.
	 */
	public SetOperation createSetOperation(Graph targetGraph) {

		return this.createSetOperation(new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(targetGraph.getAllStatements())));
	}

	/**
	 * Creates a new $del operation and adds it to this XDI message (template).
	 * @param targetXDIAddress The target address to which the operation applies.
	 * @return The newly created $del operation.
	 */
	public DelOperation createDelOperation(XDIAddress targetXDIAddress) {

		Relation relation = this.getOperationsContextNode().setRelation(XDIMessagingConstants.XDI_ADD_DEL, targetXDIAddress);

		return DelOperation.fromMessageBaseAndRelation(this, relation);
	}

	/**
	 * Creates a new $del operation and adds it to this XDI message (template).
	 * @param targetXDIStatements The target statements to which the operation applies.
	 * @return The newly created $del operation.
	 */
	public DelOperation createDelOperation(Iterator<XDIStatement> targetXDIStatements) {

		XdiInnerRoot xdiInnerRoot = XdiEntitySingleton.fromContextNode(this.getOperationsContextNode()).getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_DEL, true);
		if (targetXDIStatements != null) while (targetXDIStatements.hasNext()) xdiInnerRoot.getContextNode().setStatement(targetXDIStatements.next());

		return DelOperation.fromMessageBaseAndRelation(this, xdiInnerRoot.getPredicateRelation());
	}

	/**
	 * Creates a new $del operation and adds it to this XDI message (template).
	 * @param targetXDIStatement The target statement to which the operation applies.
	 * @return The newly created $del operation.
	 */
	public DelOperation createDelOperation(XDIStatement targetXDIStatement) {

		return this.createDelOperation(new SingleItemIterator<XDIStatement> (targetXDIStatement));
	}

	/**
	 * Creates a new $del operation and adds it to this XDI message (template).
	 * @param targetGraph The target graph with statements to which this operation applies.
	 * @return The newly created $del operation.
	 */
	public DelOperation createDelOperation(Graph targetGraph) {

		return this.createDelOperation(new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(targetGraph.getAllStatements())));
	}

	/**
	 * Creates a new $connect operation and adds it to this XDI message (template).
	 * @param targetXDIAddress The target address to which the operation applies.
	 * @return The newly created $connect operation.
	 */
	public ConnectOperation createConnectOperation(XDIAddress targetXDIAddress) {

		Relation relation = this.getOperationsContextNode().setRelation(XDIMessagingConstants.XDI_ADD_CONNECT, targetXDIAddress);

		return ConnectOperation.fromMessageBaseAndRelation(this, relation);
	}

	/**
	 * Creates a new $connect operation and adds it to this XDI message (template).
	 * @param targetXDIStatements The target statements to which the operation applies.
	 * @return The newly created $connect operation.
	 */
	public ConnectOperation createConnectOperation(Iterator<XDIStatement> targetXDIStatements) {

		XdiInnerRoot xdiInnerRoot = XdiEntitySingleton.fromContextNode(this.getOperationsContextNode()).getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_CONNECT, true);
		if (targetXDIStatements != null) while (targetXDIStatements.hasNext()) xdiInnerRoot.getContextNode().setStatement(targetXDIStatements.next());

		return ConnectOperation.fromMessageBaseAndRelation(this, xdiInnerRoot.getPredicateRelation());
	}

	/**
	 * Creates a new $connect operation and adds it to this XDI message (template).
	 * @param targetXDIStatement The target statement to which the operation applies.
	 * @return The newly created $connect operation.
	 */
	public ConnectOperation createConnectOperation(XDIStatement targetXDIStatement) {

		return this.createConnectOperation(new SingleItemIterator<XDIStatement> (targetXDIStatement));
	}

	/**
	 * Creates a new $connect operation and adds it to this XDI message (template).
	 * @param targetGraph The target graph with statements to which this operation applies.
	 * @return The newly created $connect operation.
	 */
	public ConnectOperation createConnectOperation(Graph targetGraph) {

		return this.createConnectOperation(new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(targetGraph.getAllStatements())));
	}

	/**
	 * Creates a new $send operation and adds it to this XDI message (template).
	 * @param targetXDIAddress The target address to which the operation applies.
	 * @return The newly created $send operation.
	 */
	public SendOperation createSendOperation(XDIAddress targetXDIAddress) {

		Relation relation = this.getOperationsContextNode().setRelation(XDIMessagingConstants.XDI_ADD_SEND, targetXDIAddress);

		return SendOperation.fromMessageBaseAndRelation(this, relation);
	}

	/**
	 * Creates a new $send operation and adds it to this XDI message (template).
	 * @param targetXDIStatements The target statements to which the operation applies.
	 * @return The newly created $send operation.
	 */
	public SendOperation createSendOperation(Iterator<XDIStatement> targetXDIStatements) {

		XdiInnerRoot xdiInnerRoot = XdiEntitySingleton.fromContextNode(this.getOperationsContextNode()).getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_SEND, true);
		if (targetXDIStatements != null) while (targetXDIStatements.hasNext()) xdiInnerRoot.getContextNode().setStatement(targetXDIStatements.next());

		return SendOperation.fromMessageBaseAndRelation(this, xdiInnerRoot.getPredicateRelation());
	}

	/**
	 * Creates a new $send operation and adds it to this XDI message (template).
	 * @param targetXDIStatement The target statement to which the operation applies.
	 * @return The newly created $send operation.
	 */
	public SendOperation createSendOperation(XDIStatement targetXDIStatement) {

		return this.createSendOperation(new SingleItemIterator<XDIStatement> (targetXDIStatement));
	}

	/**
	 * Creates a new $send operation and adds it to this XDI message (template).
	 * @param targetGraph The target graph with statements to which this operation applies.
	 * @return The newly created $send operation.
	 */
	public SendOperation createSendOperation(Graph targetGraph) {

		return this.createSendOperation(new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(targetGraph.getAllStatements())));
	}

	/**
	 * Creates a new $send operation and adds it to this XDI message (template).
	 * @param message The inner message of this operation.
	 * @return The newly created $send operation.
	 */
	public SendOperation createSendOperation(Message message) {

		Message clonedMessage = MessagingCloneUtil.cloneMessage(message, false);

		return this.createSendOperation(clonedMessage.getMessageEnvelope().getGraph());
	}

	/**
	 * Creates a new $push operation and adds it to this XDI message (template).
	 * @param targetXDIAddress The target address to which the operation applies.
	 * @return The newly created $push operation.
	 */
	public PushOperation createPushOperation(XDIAddress targetXDIAddress) {

		Relation relation = this.getOperationsContextNode().setRelation(XDIMessagingConstants.XDI_ADD_PUSH, targetXDIAddress);

		return PushOperation.fromMessageBaseAndRelation(this, relation);
	}

	/**
	 * Creates a new $push operation and adds it to this XDI message (template).
	 * @param targetXDIStatements The target statements to which the operation applies.
	 * @return The newly created $push operation.
	 */
	public PushOperation createPushOperation(Iterator<XDIStatement> targetXDIStatements) {

		XdiInnerRoot xdiInnerRoot = XdiEntitySingleton.fromContextNode(this.getOperationsContextNode()).getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_PUSH, true);
		if (targetXDIStatements != null) while (targetXDIStatements.hasNext()) xdiInnerRoot.getContextNode().setStatement(targetXDIStatements.next());

		return PushOperation.fromMessageBaseAndRelation(this, xdiInnerRoot.getPredicateRelation());
	}

	/**
	 * Creates a new $push operation and adds it to this XDI message (template).
	 * @param targetXDIStatement The target statement to which the operation applies.
	 * @return The newly created $push operation.
	 */
	public PushOperation createPushOperation(XDIStatement targetXDIStatement) {

		return this.createPushOperation(new SingleItemIterator<XDIStatement> (targetXDIStatement));
	}

	/**
	 * Creates a new $push operation and adds it to this XDI message (template).
	 * @param targetGraph The target graph with statements to which this operation applies.
	 * @return The newly created $push operation.
	 */
	public PushOperation createPushOperation(Graph targetGraph) {

		return this.createPushOperation(new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(targetGraph.getAllStatements())));
	}

	/**
	 * Creates a new $push operation and adds it to this XDI message (template).
	 * @param message The inner message of this operation.
	 * @return The newly created $push operation.
	 */
	public PushOperation createPushOperation(Message message) {

		Message clonedMessage = MessagingCloneUtil.cloneMessage(message, false);

		return this.createPushOperation(clonedMessage.getMessageEnvelope().getGraph());
	}

	/**
	 * Creates a new $do operation and adds it to this XDI message (template).
	 * @param targetXDIAddress The target address to which the operation applies.
	 * @return The newly created $do operation.
	 */
	public DoOperation createDoOperation(XDIAddress targetXDIAddress) {

		Relation relation = this.getOperationsContextNode().setRelation(XDIMessagingConstants.XDI_ADD_DO, targetXDIAddress);

		return DoOperation.fromMessageBaseAndRelation(this, relation);
	}

	/**
	 * Creates a new $do operation and adds it to this XDI message (template).
	 * @param targetXDIStatements The target statements to which the operation applies.
	 * @return The newly created $do operation.
	 */
	public DoOperation createDoOperation(Iterator<XDIStatement> targetXDIStatements) {

		XdiInnerRoot xdiInnerRoot = XdiEntitySingleton.fromContextNode(this.getOperationsContextNode()).getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_DO, true);
		if (targetXDIStatements != null) while (targetXDIStatements.hasNext()) xdiInnerRoot.getContextNode().setStatement(targetXDIStatements.next());

		return DoOperation.fromMessageBaseAndRelation(this, xdiInnerRoot.getPredicateRelation());
	}

	/**
	 * Creates a new $do operation and adds it to this XDI message (template).
	 * @param targetXDIStatement The target statement to which the operation applies.
	 * @return The newly created $do operation.
	 */
	public DoOperation createDoOperation(XDIStatement targetXDIStatement) {

		return this.createDoOperation(new SingleItemIterator<XDIStatement> (targetXDIStatement));
	}

	/**
	 * Creates a new $do operation and adds it to this XDI message (template).
	 * @param targetGraph The target graph with statements to which this operation applies.
	 * @return The newly created $do operation.
	 */
	public DoOperation createDoOperation(Graph targetGraph) {

		return this.createDoOperation(new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(targetGraph.getAllStatements())));
	}

	/**
	 * Creates a new message push result and adds it to this XDI message (template).
	 * @param messagePushResultGraph The message push result graph to add to this XDI message (template).
	 * @return The newly created message push result represented as an XDI inner root.
	 */
	public XdiInnerRoot createMessageDeferredPushResult(Graph messagePushResultGraph) {

		if (messagePushResultGraph == null) throw new NullPointerException();

		Iterator<XDIStatement> statements = new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(messagePushResultGraph.getAllStatements()));
		XdiInnerRoot xdiInnerRoot = this.getXdiSubGraph().getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_DEFER_PUSH, true);
		while (statements.hasNext()) xdiInnerRoot.getContextNode().setStatement(statements.next());

		return xdiInnerRoot;
	}

	public XdiInnerRoot getMessageDeferredPushResult() {

		XdiInnerRoot xdiInnerRoot = this.getXdiSubGraph().getXdiInnerRoot(XDIMessagingConstants.XDI_ADD_DEFER_PUSH, false);
		if (xdiInnerRoot == null) return null;

		return xdiInnerRoot;
	}

	/**
	 * Creates a new operation result and adds it to this XDI message (template).
	 * @param operationXDIAddress The operation address to use for the new operation result.
	 * @param resultGraph The result graph to add to this XDI message (template).
	 * @return The newly created operation result represented as an XDI inner root.
	 */
	public XdiInnerRoot createOperationResult(XDIAddress operationXDIAddress, Graph resultGraph) {

		if (resultGraph == null) throw new NullPointerException();

		Iterator<XDIStatement> statements = new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(resultGraph.getAllStatements()));
		XdiInnerRoot xdiInnerRoot = this.getXdiSubGraph().getXdiInnerRoot(operationXDIAddress, true);
		while (statements.hasNext()) xdiInnerRoot.getContextNode().setStatement(statements.next());

		return xdiInnerRoot;
	}

	public ReadOnlyIterator<XdiInnerRoot> getOperationResults() {

		return new XdiInnerRoot.MappingRelationInnerRootIterator(
				new SelectingIterator<Relation> (this.getContextNode().getRelations()) {

					@Override
					public boolean select(Relation relation) {

						return Operation.isValidOperationXDIAddress(relation.getXDIAddress());
					}
				});
	}

	/**
	 * Returns all XDI operations in this XDI message (template).
	 * @return An iterator over all XDI operations.
	 */
	public ReadOnlyIterator<Operation> getOperations() {

		// get all relations that are valid XDI operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations();

		return new MappingRelationOperationIterator(this, relations);
	}

	/**
	 * Returns all XDI operations with a given operation address in this XDI message (template).
	 * @return An iterator over all XDI operations.
	 */
	public ReadOnlyIterator<Operation> getOperations(XDIAddress operationXDIAddress) {

		// get all relations that are valid XDI operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations(operationXDIAddress);

		return new MappingRelationOperationIterator(this, relations);
	}

	/**
	 * Returns all XDI $get operations in this XDI message (template).
	 * @return An iterator over all XDI $get operations.
	 */
	public ReadOnlyIterator<GetOperation> getGetOperations() {

		// get all relations that are valid XDI $get operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations(XDIMessagingConstants.XDI_ADD_GET);

		return new MappingRelationGetOperationIterator(this, relations);
	}

	/**
	 * Returns all XDI $set operations in this XDI message (template).
	 * @return An iterator over all XDI $set operations.
	 */
	public ReadOnlyIterator<SetOperation> getSetOperations() {

		// get all relations that are valid XDI $set operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations(XDIMessagingConstants.XDI_ADD_SET);

		return new MappingRelationSetOperationIterator(this, relations);
	}

	/**
	 * Returns all XDI $del operations in this XDI message (template).
	 * @return An iterator over all XDI $del operations.
	 */
	public ReadOnlyIterator<DelOperation> getDelOperations() {

		// get all relations that are valid XDI $del operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations(XDIMessagingConstants.XDI_ADD_DEL);

		return new MappingRelationDelOperationIterator(this, relations);
	}

	/**
	 * Returns all XDI $connect operations in this XDI message (template).
	 * @return An iterator over all XDI $connect operations.
	 */
	public ReadOnlyIterator<ConnectOperation> getConnectOperations() {

		// get all relations that are valid XDI $connect operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations(XDIMessagingConstants.XDI_ADD_CONNECT);

		return new MappingRelationConnectOperationIterator(this, relations);
	}

	/**
	 * Returns all XDI $send operations in this XDI message (template).
	 * @return An iterator over all XDI $send operations.
	 */
	public ReadOnlyIterator<SendOperation> getSendOperations() {

		// get all relations that are valid XDI $send operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations(XDIMessagingConstants.XDI_ADD_SEND);

		return new MappingRelationSendOperationIterator(this, relations);
	}

	/**
	 * Returns all XDI $push operations in this XDI message (template).
	 * @return An iterator over all XDI $push operations.
	 */
	public ReadOnlyIterator<PushOperation> getPushOperations() {

		// get all relations that are valid XDI $push operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations(XDIMessagingConstants.XDI_ADD_PUSH);

		return new MappingRelationPushOperationIterator(this, relations);
	}

	/**
	 * Returns all XDI $do operations in this XDI message (template).
	 * @return An iterator over all XDI $do operations.
	 */
	public ReadOnlyIterator<DoOperation> getDoOperations() {

		// get all relations that are valid XDI $do operations

		Iterator<Relation> relations = this.getOperationsContextNode().getRelations(XDIMessagingConstants.XDI_ADD_DO);

		return new MappingRelationDoOperationIterator(this, relations);
	}

	/**
	 * Deletes all operations from this message.
	 */
	public void deleteOperations() {

		for (Operation operation : new IteratorListMaker<Operation> (this.getOperations()).list()) {

			XdiInnerRoot targetInnerRoot = operation.getTargetXdiInnerRoot();

			if (targetInnerRoot != null) {

				targetInnerRoot.getContextNode().delete();
			} else {

				operation.getRelation().delete();
			}
		}
	}

	/**
	 * Returns the number of XDI operations in this XDI message (template).
	 */
	public long getOperationCount() {

		Iterator<Operation> iterator = this.getOperations();

		return new IteratorCounter(iterator).count();
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		return this.getContextNode().toString();
	}

	@Override
	public boolean equals(Object object) {

		if (object == null || ! (object instanceof Message)) return false;
		if (object == this) return true;

		Message other = (Message) object;

		return this.getContextNode().equals(other.getContextNode());
	}

	@Override
	public int hashCode() {

		int hashCode = 1;

		hashCode = (hashCode * 31) + this.getContextNode().hashCode();

		return hashCode;
	}

	@Override
	public int compareTo(MessageBase<N> other) {

		if (other == this || other == null) return 0;

		return this.getContextNode().compareTo(other.getContextNode());
	}

	/*
	 * Helper classes
	 */

	public static class MappingRelationOperationIterator extends NotNullIterator<Operation> {

		public MappingRelationOperationIterator(final MessageBase<?> messageBase, Iterator<Relation> relations) {

			super(new MappingIterator<Relation, Operation> (relations) {

				@Override
				public Operation map(Relation relation) {

					return Operation.fromMessageBaseAndRelation(messageBase, relation);
				}
			});
		}
	}

	public static class MappingRelationGetOperationIterator extends NotNullIterator<GetOperation> {

		public MappingRelationGetOperationIterator(final MessageBase<?> messageBase, Iterator<Relation> relations) {

			super(new MappingIterator<Relation, GetOperation> (relations) {

				@Override
				public GetOperation map(Relation relation) {

					return GetOperation.fromMessageBaseAndRelation(messageBase, relation);
				}
			});
		}
	}

	public static class MappingRelationSetOperationIterator extends NotNullIterator<SetOperation> {

		public MappingRelationSetOperationIterator(final MessageBase<?> messageBase, Iterator<Relation> relations) {

			super(new MappingIterator<Relation, SetOperation> (relations) {

				@Override
				public SetOperation map(Relation relation) {

					return SetOperation.fromMessageBaseAndRelation(messageBase, relation);
				}
			});
		}
	}

	public static class MappingRelationDelOperationIterator extends NotNullIterator<DelOperation> {

		public MappingRelationDelOperationIterator(final MessageBase<?> messageBase, Iterator<Relation> relations) {

			super(new MappingIterator<Relation, DelOperation> (relations) {

				@Override
				public DelOperation map(Relation relation) {

					return DelOperation.fromMessageBaseAndRelation(messageBase, relation);
				}
			});
		}
	}

	public static class MappingRelationConnectOperationIterator extends NotNullIterator<ConnectOperation> {

		public MappingRelationConnectOperationIterator(final MessageBase<?> messageBase, Iterator<Relation> relations) {

			super(new MappingIterator<Relation, ConnectOperation> (relations) {

				@Override
				public ConnectOperation map(Relation relation) {

					return ConnectOperation.fromMessageBaseAndRelation(messageBase, relation);
				}
			});
		}
	}

	public static class MappingRelationSendOperationIterator extends NotNullIterator<SendOperation> {

		public MappingRelationSendOperationIterator(final MessageBase<?> messageBase, Iterator<Relation> relations) {

			super(new MappingIterator<Relation, SendOperation> (relations) {

				@Override
				public SendOperation map(Relation relation) {

					return SendOperation.fromMessageBaseAndRelation(messageBase, relation);
				}
			});
		}
	}

	public static class MappingRelationPushOperationIterator extends NotNullIterator<PushOperation> {

		public MappingRelationPushOperationIterator(final MessageBase<?> messageBase, Iterator<Relation> relations) {

			super(new MappingIterator<Relation, PushOperation> (relations) {

				@Override
				public PushOperation map(Relation relation) {

					return PushOperation.fromMessageBaseAndRelation(messageBase, relation);
				}
			});
		}
	}

	public static class MappingRelationDoOperationIterator extends NotNullIterator<DoOperation> {

		public MappingRelationDoOperationIterator(final MessageBase<?> messageBase, Iterator<Relation> relations) {

			super(new MappingIterator<Relation, DoOperation> (relations) {

				@Override
				public DoOperation map(Relation relation) {

					return DoOperation.fromMessageBaseAndRelation(messageBase, relation);
				}
			});
		}
	}

}
