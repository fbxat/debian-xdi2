package xdi2.messaging.operations;

import xdi2.core.Relation;
import xdi2.core.util.XDIAddressUtil;
import xdi2.messaging.MessageBase;
import xdi2.messaging.constants.XDIMessagingConstants;

/**
 * A $send XDI operation, represented as a relation.
 * 
 * @author markus
 */
public class SendOperation extends Operation {

	private static final long serialVersionUID = 3704383205563760034L;

	protected SendOperation(MessageBase<?> messageBase, Relation relation) {

		super(messageBase, relation);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if an relation is a valid XDI $send operation.
	 * @param relation The relation to check.
	 * @return True if the relation is a valid XDI $send operation.
	 */
	public static boolean isValid(Relation relation) {

		if (XDIAddressUtil.startsWithXDIAddress(relation.getXDIAddress(), XDIMessagingConstants.XDI_ADD_SEND) == null) return false;
		if (! XDIMessagingConstants.XDI_ARC_DO.equals(relation.getContextNode().getXDIArc())) return false;

		return true;
	}

	/**
	 * Factory method that creates an XDI $send operation bound to a given relation.
	 * @param relation The relation that is an XDI $send operation.
	 * @return The XDI $send operation.
	 */
	public static SendOperation fromMessageBaseAndRelation(MessageBase<?> messageBase, Relation relation) {

		if (! isValid(relation)) return null;

		return new SendOperation(messageBase, relation);
	}
}
