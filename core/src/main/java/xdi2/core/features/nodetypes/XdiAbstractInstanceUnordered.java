package xdi2.core.features.nodetypes;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.UUID;

import xdi2.core.ContextNode;
import xdi2.core.constants.XDIConstants;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.util.iterators.NotNullIterator;
import xdi2.core.xri3.XDI3SubSegment;

public abstract class XdiAbstractInstanceUnordered extends XdiAbstractInstance implements XdiInstanceUnordered {

	private static final long serialVersionUID = -8496645644143069191L;

	protected XdiAbstractInstanceUnordered(ContextNode contextNode) {

		super(contextNode);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a context node is a valid XDI unordered instance.
	 * @param contextNode The context node to check.
	 * @return True if the context node is a valid XDI unordered instance.
	 */
	public static boolean isValid(ContextNode contextNode) {

		return XdiEntityInstanceUnordered.isValid(contextNode) || 
				XdiAttributeInstanceUnordered.isValid(contextNode);
	}

	/**
	 * Factory method that creates an XDI unordered instance bound to a given context node.
	 * @param contextNode The context node that is an XDI unordered instance.
	 * @return The XDI unordered instance.
	 */
	public static XdiAbstractInstanceUnordered fromContextNode(ContextNode contextNode) {

		XdiAbstractInstanceUnordered xdiInstance;

		if ((xdiInstance = XdiEntityInstanceUnordered.fromContextNode(contextNode)) != null) return xdiInstance;
		if ((xdiInstance = XdiAttributeInstanceUnordered.fromContextNode(contextNode)) != null) return xdiInstance;

		return null;
	}

	/*
	 * Methods for XRIs
	 */

	public static XDI3SubSegment createArcXri(String identifier, boolean mutable) {

		Character cs = mutable ? XDIConstants.CS_STAR : XDIConstants.CS_BANG;

		return XDI3SubSegment.create("" + cs + identifier);
	}

	public static XDI3SubSegment createArcXriFromUuid(String uuid, boolean mutable) {

		return createArcXri(":uuid:" + uuid, mutable);
	}

	public static XDI3SubSegment createArcXriFromRandom(boolean mutable) {

		String uuid = UUID.randomUUID().toString();

		return createArcXriFromUuid(uuid, mutable);
	}

	public static XDI3SubSegment createArcXriFromHash(String string, boolean mutable) {

		byte[] output;

		try {

			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			digest.update(string.getBytes("UTF-8"));
			output = digest.digest();
		} catch (Exception ex) {

			throw new Xdi2RuntimeException(ex.getMessage(), ex);
		}

		String uuid = UUID.nameUUIDFromBytes(output).toString();

		return createArcXriFromUuid(uuid, mutable);
	}

	public static boolean isValidArcXri(XDI3SubSegment arcXri) {

		if (arcXri == null) return false;

		if (arcXri.isClassXs()) return false;
		if (arcXri.isAttributeXs()) return false;
		if (arcXri.hasXRef()) return false;

		if (! XDIConstants.CS_STAR.equals(arcXri.getCs()) && ! XDIConstants.CS_BANG.equals(arcXri.getCs())) return false;

		if (! arcXri.hasLiteral()) return false;

		return true;
	}

	/*
	 * Helper classes
	 */

	public static class MappingContextNodeXdiInstanceUnorderedIterator extends NotNullIterator<XdiInstanceUnordered> {

		public MappingContextNodeXdiInstanceUnorderedIterator(Iterator<ContextNode> contextNodes) {

			super(new MappingIterator<ContextNode, XdiInstanceUnordered> (contextNodes) {

				@Override
				public XdiInstanceUnordered map(ContextNode contextNode) {

					return XdiAbstractInstanceUnordered.fromContextNode(contextNode);
				}
			});
		}
	}
}
