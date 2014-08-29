package xdi2.client.agent.impl;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.XDIClient;
import xdi2.client.agent.XDIAgent;
import xdi2.client.exceptions.Xdi2AgentException;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.GraphUtil;
import xdi2.core.util.XDIAddressUtil;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.GetOperation;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.Operation;

public class XDIBasicAgent implements XDIAgent {

	private static final Logger log = LoggerFactory.getLogger(XDIBasicAgent.class);

	private XDIDiscoveryClient xdiDiscoveryClient;
	private XDIClient xdiClient;

	public XDIBasicAgent(XDIDiscoveryClient xdiDiscoveryClient, XDIClient xdiClient) {

		this.xdiDiscoveryClient = xdiDiscoveryClient;
		this.xdiClient = xdiClient;
	}

	public XDIBasicAgent(XDIDiscoveryClient xdiDiscoveryClient) {

		this(xdiDiscoveryClient, null);
	}

	public XDIBasicAgent() {

		this(null, null);
	}

	@Override
	public ContextNode get(XDIAddress XDIaddress, Graph localGraph) throws Xdi2AgentException, Xdi2ClientException {

		// let's look for the address in the local graph

		if (localGraph != null) {

			ContextNode contextNode = localGraph.getDeepContextNode(XDIaddress);

			if (contextNode != null) {

				if (log.isDebugEnabled()) log.debug("Found context node in local graph for address " + XDIaddress);
				return contextNode;
			}
		}

		// let's find out the owner peer root of the local graph

		XDIArc ownerPeerRootXDIArc = localGraph == null ? null : GraphUtil.getOwnerPeerRootXDIArc(localGraph);

		if (log.isDebugEnabled()) log.debug("Determined owner peer root: " + ownerPeerRootXDIArc);

		// let's find out the target peer root of the address

		XDIAddress targetPeerRootXDIAddress = XDIAddressUtil.findXDIAddress(XDIaddress, XdiPeerRoot.class);
		CloudNumber targetCloudNumber = CloudNumber.fromXDIAddress(XDIaddress);
		CloudName targetCloudName = CloudName.fromXDIAddress(XDIaddress);

		if (log.isDebugEnabled()) log.debug("Peer root: " + targetPeerRootXDIAddress + ", Cloud Number: " + targetCloudNumber + ", Cloud Name: " + targetCloudName);

		XDIArc targetPeerRootXDIArc = null;
		if (targetPeerRootXDIAddress != null) targetPeerRootXDIArc = targetPeerRootXDIAddress.getLastXDIArc();
		if (targetCloudNumber != null) targetPeerRootXDIArc = XdiPeerRoot.createPeerRootXDIArc(targetCloudNumber.getXDIAddress());
		if (targetCloudName != null) targetPeerRootXDIArc = XdiPeerRoot.createPeerRootXDIArc(targetCloudName.getXDIAddress());

		if (log.isDebugEnabled()) log.debug("Determined target peer root: " + targetPeerRootXDIArc);

		// time to give up?

		if (targetPeerRootXDIArc == null) {

			if (log.isDebugEnabled()) log.debug("No local graph, and unable to determine target peer root for address " + XDIaddress);
			return null;
		}

		// let's find out the target address

		XDIAddress targetXDIAddress = null;

		if (targetPeerRootXDIAddress != null) 
			targetXDIAddress = XDIAddressUtil.localXDIAddress(XDIaddress, targetPeerRootXDIAddress.getNumXDIArcs());
		else
			targetXDIAddress = XDIaddress;

		if (log.isDebugEnabled()) log.debug("Determined target address: " + targetXDIAddress);

		// discovery step

		XDIDiscoveryClient xdiDiscoveryClient = this.getXdiDiscoveryClient();
		if (xdiDiscoveryClient == null) xdiDiscoveryClient = this.constructXdiDiscoveryClient();
		if (xdiDiscoveryClient == null) throw new Xdi2AgentException("Unable to obtain an XDI discovery client for address " + XDIaddress);

		XDIDiscoveryResult xdiDiscoveryResult = xdiDiscoveryClient.discoverFromRegistry(XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(targetPeerRootXDIArc), null);
		if (xdiDiscoveryResult == null) throw new Xdi2AgentException("Unable to discover for address " + XDIaddress + " and discovery client " + xdiDiscoveryClient);

		CloudNumber cloudNumber = xdiDiscoveryResult.getCloudNumber();
		URL xdiEndpointUrl = xdiDiscoveryResult.getXdiEndpointUrl();
		if (cloudNumber == null) throw new Xdi2AgentException("Unable to discover cloud number for address " + XDIaddress + " and discovery client " + xdiDiscoveryClient);
		if (xdiEndpointUrl == null) throw new Xdi2AgentException("Unable to discover XDI endpoint URI for address " + XDIaddress + " and discovery client " + xdiDiscoveryClient);

		// adjust target peer root

		targetPeerRootXDIArc = cloudNumber.getPeerRootXDIArc();

		// message construction step

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		Message message = messageEnvelope.createMessage(this.constructSenderXDIAddress());
		message.setToPeerRootXDIArc(targetPeerRootXDIArc);
		message.setLinkContract(PublicLinkContract.class);
		Operation operation = message.createGetOperation(targetXDIAddress);
		operation.setParameter(GetOperation.XDI_ADD_PARAMETER_DEREF, Boolean.TRUE);

		// client step

		XDIClient xdiClient = this.getXdiClient();
		if (xdiClient == null) xdiClient = this.constructXdiClient(xdiEndpointUrl);
		if (xdiClient == null) throw new Xdi2AgentException("Unable to obtain an XDI client for address " + XDIaddress);

		MessageResult messageResult = xdiClient.send(messageEnvelope, null);

		// let's look for our target address in the message result

		ContextNode contextNode = messageResult.getGraph().getDeepContextNode(targetXDIAddress);

		if (contextNode != null) {

			if (log.isDebugEnabled()) log.debug("Found context node in message result for address " + XDIaddress);
			return contextNode;
		}

		// give up

		if (log.isDebugEnabled()) log.debug("Unable to find context. Giving up for address " + XDIaddress);
		return null;
	}

	/*
	 * XDI client methods
	 */

	protected XDIDiscoveryClient constructXdiDiscoveryClient() {

		XDIDiscoveryClient xdiDiscoveryClient = XDIDiscoveryClient.DEFAULT_DISCOVERY_CLIENT;

		return xdiDiscoveryClient;
	}

	protected XDIClient constructXdiClient(URL xdiEndpointUrl) {

		XDIHttpClient xdiClient = new XDIHttpClient();
		xdiClient.setXdiEndpointUrl(xdiEndpointUrl);

		return xdiClient;
	}

	protected XDIAddress constructSenderXDIAddress() {

		return XDIAuthenticationConstants.XDI_ADD_ANONYMOUS;
	}

	/*
	 * Getters and setters
	 */

	public XDIClient getXdiClient() {

		return this.xdiClient;
	}

	public void setXdiClient(XDIClient xdiClient) {

		this.xdiClient = xdiClient;
	}

	public XDIDiscoveryClient getXdiDiscoveryClient() {

		return this.xdiDiscoveryClient;
	}

	public void setXdiDiscoveryClient(XDIDiscoveryClient xdiDiscoveryClient) {

		this.xdiDiscoveryClient = xdiDiscoveryClient;
	}
}
