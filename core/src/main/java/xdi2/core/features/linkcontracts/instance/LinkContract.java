package xdi2.core.features.linkcontracts.instance;

import xdi2.core.Relation;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.features.linkcontracts.LinkContractBase;
import xdi2.core.features.nodetypes.XdiContext;
import xdi2.core.features.nodetypes.XdiEntity;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.iterators.MappingContextNodeXDIArcIterator;
import xdi2.core.util.iterators.MappingRelationTargetContextNodeIterator;
import xdi2.core.util.iterators.NotNullIterator;
import xdi2.core.util.iterators.ReadOnlyIterator;

/**
 * An XDI link contract, represented as an XDI entity.
 * 
 * @author markus
 */
public abstract class LinkContract extends LinkContractBase<XdiEntity> {

	private static final long serialVersionUID = 7780858453875071410L;

	private XdiEntity xdiEntity;

	protected LinkContract(XdiEntity xdiEntity) {

		this.xdiEntity = xdiEntity;
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if an XDI entity is a valid XDI link contract.
	 * @param xdiEntity The XDI entity to check.
	 * @return True if the XDI entity is a valid XDI link contract.
	 */
	public static boolean isValid(XdiEntity xdiEntity) {

		if (xdiEntity == null) return false;

		return
				RootLinkContract.isValid(xdiEntity) ||
				PublicLinkContract.isValid(xdiEntity) ||
				ConnectLinkContract.isValid(xdiEntity) ||
				SendLinkContract.isValid(xdiEntity) ||
				RelationshipLinkContract.isValid(xdiEntity);
	}

	/**
	 * Factory method that creates an XDI link contract bound to a given XDI entity.
	 * @param xdiEntity The XDI entity that is an XDI link contract.
	 * @return The XDI link contract.
	 */
	public static LinkContract fromXdiEntity(XdiEntity xdiEntity) {

		LinkContract linkContract = null;

		if ((linkContract = RootLinkContract.fromXdiEntity(xdiEntity)) != null) return linkContract;
		if ((linkContract = PublicLinkContract.fromXdiEntity(xdiEntity)) != null) return linkContract;
		if ((linkContract = ConnectLinkContract.fromXdiEntity(xdiEntity)) != null) return linkContract;
		if ((linkContract = SendLinkContract.fromXdiEntity(xdiEntity)) != null) return linkContract;
		if ((linkContract = RelationshipLinkContract.fromXdiEntity(xdiEntity)) != null) return linkContract;

		return null;
	}

	/*
	 * Instance methods
	 */

	/**
	 * Returns the underlying XDI entity to which this XDI link contract is bound.
	 * @return An XDI entity that represents the XDI link contract.
	 */
	public XdiEntity getXdiEntity() {

		return this.xdiEntity;
	}

	@Override
	public XdiEntity getXdiSubGraph() {

		return this.xdiEntity;
	}

	/*
	 * Specific to push link contracts
	 */

	public void setupPushPermissionInverseRelations() {

		ReadOnlyIterator<Relation> pushPermissionRelations = this.getContextNode().getRelations(XDILinkContractConstants.XDI_ADD_PUSH);

		for (Relation pushPermissionRelation : pushPermissionRelations) {

			pushPermissionRelation.followContextNode().setRelation(XDILinkContractConstants.XDI_ADD_IS_PUSH, pushPermissionRelation.getContextNode().getXDIAddress());
		}
	}

	public void setPushToPeerRootXDIArc(XDIArc toPeerRootXDIArc) {

		this.getContextNode().setRelation(XDILinkContractConstants.XDI_ADD_TO_PEER_ROOT_ARC, XDIAddress.fromComponent(toPeerRootXDIArc));
	}

	public ReadOnlyIterator<XDIArc> getPushToPeerRootXDIArcs() {

		return new NotNullIterator<XDIArc> (
				new MappingContextNodeXDIArcIterator (
						new XdiContext.MappingXdiContextContextNodeIterator(
								new XdiPeerRoot.MappingContextNodePeerRootIterator(
										new MappingRelationTargetContextNodeIterator(
												this.getContextNode().getRelations(XDILinkContractConstants.XDI_ADD_TO_PEER_ROOT_ARC))))));
	}
}
