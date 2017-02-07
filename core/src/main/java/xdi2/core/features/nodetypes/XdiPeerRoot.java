package xdi2.core.features.nodetypes;

import java.util.Iterator;

import xdi2.core.ContextNode;
import xdi2.core.constants.XDIConstants;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIXRef;
import xdi2.core.util.GraphUtil;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.util.iterators.NotNullIterator;

/**
 * An XDI peer root, represented as a context node.
 * 
 * @author markus
 */
public class XdiPeerRoot extends XdiAbstractRoot {

	private static final long serialVersionUID = -4689596452249483618L;

	protected XdiPeerRoot(ContextNode contextNode) {

		super(contextNode);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a context node is a valid XDI peer root.
	 * @param contextNode The context node to check.
	 * @return True if the context node is a valid XDI peer root.
	 */
	public static boolean isValid(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (contextNode.getXDIArc() == null || ! isValidXDIArc(contextNode.getXDIArc())) return false;
		if (contextNode.getContextNode() != null && ! XdiAbstractRoot.isValid(contextNode.getContextNode())) return false;

		return true;
	}

	/**
	 * Factory method that creates an XDI peer root bound to a given context node.
	 * @param contextNode The context node that is an XDI peer root.
	 * @return The XDI peer root.
	 */
	public static XdiPeerRoot fromContextNode(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (! isValid(contextNode)) return null;

		if (contextNode.getXDIArc().isDefinition() && contextNode.getXDIArc().isVariable()) return new Definition.Variable(contextNode);
		if (contextNode.getXDIArc().isDefinition() && ! contextNode.getXDIArc().isVariable()) return new Definition(contextNode);
		if (! contextNode.getXDIArc().isDefinition() && contextNode.getXDIArc().isVariable()) return new Variable(contextNode);
		return new XdiPeerRoot(contextNode);
	}

	public static XdiPeerRoot fromXDIAddress(XDIAddress XDIaddress) {

		return fromContextNode(GraphUtil.contextNodeFromComponents(XDIaddress));
	}

	/*
	 * Instance methods
	 */

	/**
	 * Checks if this XDI peer root is the self XDI peer root of the graph.
	 * @return True, if this is the self XDI peer root.
	 */
	public boolean isSelfPeerRoot() {

		XdiPeerRoot selfPeerRoot = this.findCommonRoot().getSelfPeerRoot();
		if (this.equals(selfPeerRoot)) return true;

		ContextNode refContextNode = Equivalence.getReferenceContextNode(this.getContextNode());
		XdiPeerRoot refPeerRoot = refContextNode == null ? null : XdiPeerRoot.fromContextNode(refContextNode);
		if (refPeerRoot != null && refPeerRoot.equals(selfPeerRoot)) return true;

		ContextNode repContextNode = Equivalence.getReplacementContextNode(this.getContextNode());
		XdiPeerRoot repPeerRoot = repContextNode == null ? null : XdiPeerRoot.fromContextNode(repContextNode);
		if (repPeerRoot != null && repPeerRoot.equals(selfPeerRoot)) return true;

		return false;
	}

	public XDIAddress getXDIAddressOfPeerRoot() {

		return getXDIAddressOfPeerRootXDIArc(this.getContextNode().getXDIArc());
	}

	/*
	 * Methods for arcs
	 */

	/**
	 * Returns the peer root arc of an address.
	 * @param address An address.
	 * @return The peer root arc of the address.
	 */
	public static XDIArc createPeerRootXDIArc(XDIAddress XDIaddress) {

		return XDIArc.create("" + XDIConstants.XS_ROOT.charAt(0) + XDIaddress + XDIConstants.XS_ROOT.charAt(1));
	}

	/**
	 * Returns the address of the peer root arc.
	 * @param arc A peer root arc.
	 * @return The address of the peer root arc.
	 */
	public static XDIAddress getXDIAddressOfPeerRootXDIArc(XDIArc XDIarc) {

		if (XDIarc == null) return null;

		if (XDIarc.hasCs()) return null;
		if (XDIarc.isCollection()) return null;
		if (XDIarc.isAttribute()) return null;
		if (! XDIarc.hasXRef()) return null;

		XDIXRef xref = XDIarc.getXRef();
		if (! XDIConstants.XS_ROOT.equals(xref.getXs())) return null;
		if (! xref.hasXDIArc()) return null;

		return XDIAddress.fromComponent(xref.getXDIArc());
	}

	/**
	 * Returns the IRI of the peer root arc.
	 * @param arc A peer root arc.
	 * @return The IRI of the peer root arc.
	 */
	public static String getIriOfPeerRootXDIArc(XDIArc XDIarc) {

		if (XDIarc == null) return null;

		if (XDIarc.hasCs()) return null;
		if (XDIarc.isCollection()) return null;
		if (XDIarc.isAttribute()) return null;
		if (! XDIarc.hasXRef()) return null;

		XDIXRef xref = XDIarc.getXRef();
		if (! XDIConstants.XS_ROOT.equals(xref.getXs())) return null;
		if (! xref.hasIri()) return null;

		return xref.getIri();
	}

	/**
	 * Returns the literal of the peer root arc.
	 * @param arc A peer root arc.
	 * @return The literal of the peer root arc.
	 */
	public static String getLiteralOfPeerRootXDIArc(XDIArc XDIarc) {

		if (XDIarc == null) return null;

		if (XDIarc.hasCs()) return null;
		if (XDIarc.isCollection()) return null;
		if (XDIarc.isAttribute()) return null;
		if (! XDIarc.hasXRef()) return null;

		XDIXRef xref = XDIarc.getXRef();
		if (! XDIConstants.XS_ROOT.equals(xref.getXs())) return null;
		if (! xref.hasLiteralNode()) return null;

		return xref.getLiteralNode();
	}

	/**
	 * Checks if a given arc is a peer root arc.
	 * @param arc A peer root arc.
	 * @return True, if the arc is a peer root arc.
	 */
	public static boolean isValidXDIArc(XDIArc XDIarc) {

		if (XDIarc == null) throw new NullPointerException();

		if (getXDIAddressOfPeerRootXDIArc(XDIarc) != null) return true;
		if (getIriOfPeerRootXDIArc(XDIarc) != null) return true;
		if (getLiteralOfPeerRootXDIArc(XDIarc) != null) return true;

		return false;
	}

	/*
	 * Definition and Variable classes
	 */

	public static class Definition extends XdiPeerRoot implements XdiDefinition<XdiRoot> {

		private static final long serialVersionUID = -7421993257285271303L;

		private Definition(ContextNode contextNode) {

			super(contextNode);
		}

		public static boolean isValid(ContextNode contextNode) {

			return XdiPeerRoot.isValid(contextNode) &&
					contextNode.getXDIArc().isDefinition() &&
					! contextNode.getXDIArc().isVariable();
		}

		public static Definition fromContextNode(ContextNode contextNode) {

			if (contextNode == null) throw new NullPointerException();

			if (! isValid(contextNode)) return null;

			return new Definition(contextNode);
		}

		public static class Variable extends Definition implements XdiVariable<XdiRoot> {

			private static final long serialVersionUID = -4287720287904718749L;

			private Variable(ContextNode contextNode) {

				super(contextNode);
			}

			public static boolean isValid(ContextNode contextNode) {

				return XdiPeerRoot.isValid(contextNode) &&
						contextNode.getXDIArc().isDefinition() &&
						contextNode.getXDIArc().isVariable();
			}

			public static Variable fromContextNode(ContextNode contextNode) {

				if (contextNode == null) throw new NullPointerException();

				if (! isValid(contextNode)) return null;

				return new Variable(contextNode);
			}
		}
	}

	public static class Variable extends XdiPeerRoot implements XdiVariable<XdiRoot> {

		private static final long serialVersionUID = -2044945343736353559L;

		private Variable(ContextNode contextNode) {

			super(contextNode);
		}

		public static boolean isValid(ContextNode contextNode) {

			return XdiPeerRoot.isValid(contextNode) &&
					! contextNode.getXDIArc().isDefinition() &&
					contextNode.getXDIArc().isVariable();
		}

		public static Variable fromContextNode(ContextNode contextNode) {

			if (contextNode == null) throw new NullPointerException();

			if (! isValid(contextNode)) return null;

			return new Variable(contextNode);
		}
	}

	/*
	 * Helper classes
	 */

	public static class MappingContextNodePeerRootIterator extends NotNullIterator<XdiPeerRoot> {

		public MappingContextNodePeerRootIterator(Iterator<ContextNode> contextNodes) {

			super(new MappingIterator<ContextNode, XdiPeerRoot> (contextNodes) {

				@Override
				public XdiPeerRoot map(ContextNode contextNode) {

					return XdiPeerRoot.fromContextNode(contextNode);
				}
			});
		}
	}

	public static class MappingXDIAddressPeerRootXDIArcIterator extends MappingIterator<XDIAddress, XDIArc> {

		public MappingXDIAddressPeerRootXDIArcIterator(Iterator<? extends XDIAddress> iterator) {

			super(iterator);
		}

		@Override
		public XDIArc map(XDIAddress XDIaddress) {

			return XdiPeerRoot.createPeerRootXDIArc(XDIaddress);
		}
	}

	public static class MappingPeerRootXDIArcXDIAddressIterator extends MappingIterator<XDIArc, XDIAddress> {

		public MappingPeerRootXDIArcXDIAddressIterator(Iterator<? extends XDIArc> iterator) {

			super(iterator);
		}

		@Override
		public XDIAddress map(XDIArc XDIarc) {

			return XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(XDIarc);
		}
	}

	public static class MappingPeerRootXDIArcIriIterator extends MappingIterator<XDIArc, String> {

		public MappingPeerRootXDIArcIriIterator(Iterator<? extends XDIArc> iterator) {

			super(iterator);
		}

		@Override
		public String map(XDIArc XDIarc) {

			return XdiPeerRoot.getIriOfPeerRootXDIArc(XDIarc);
		}
	}

	public static class MappingPeerRootXDIArcLiteralIterator extends MappingIterator<XDIArc, String> {

		public MappingPeerRootXDIArcLiteralIterator(Iterator<? extends XDIArc> iterator) {

			super(iterator);
		}

		@Override
		public String map(XDIArc XDIarc) {

			return XdiPeerRoot.getLiteralOfPeerRootXDIArc(XDIarc);
		}
	}
}
