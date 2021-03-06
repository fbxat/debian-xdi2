package xdi2.core.features.nodetypes;

import java.util.Iterator;

import xdi2.core.ContextNode;
import xdi2.core.constants.XDIConstants;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIXRef;
import xdi2.core.util.GraphUtil;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.util.iterators.NotNullIterator;

/**
 * An XDI entity collection, represented as a context node.
 * 
 * @author markus
 */
public class XdiEntityCollection extends XdiAbstractCollection<XdiEntityCollection, XdiEntity, XdiEntityCollection, XdiEntityInstanceUnordered, XdiEntityInstanceOrdered, XdiEntityInstance> implements XdiCollection<XdiEntityCollection, XdiEntity, XdiEntityCollection, XdiEntityInstanceUnordered, XdiEntityInstanceOrdered, XdiEntityInstance> {

	private static final long serialVersionUID = -8518618921427437445L;

	protected XdiEntityCollection(ContextNode contextNode) {

		super(contextNode, XdiEntityCollection.class, XdiEntityInstanceUnordered.class, XdiEntityInstanceOrdered.class, XdiEntityInstance.class);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a context node is a valid XDI entity collection.
	 * @param contextNode The context node to check.
	 * @return True if the context node is a valid XDI entity collection.
	 */
	public static boolean isValid(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (contextNode.getXDIArc() == null || ! isValidXDIArc(contextNode.getXDIArc())) return false;
		if (contextNode.getContextNode() != null && XdiAttributeCollection.isValid(contextNode.getContextNode())) return false;
		if (contextNode.getContextNode() != null && XdiAbstractAttribute.isValid(contextNode.getContextNode())) return false;

		return true;
	}

	/**
	 * Factory method that creates an XDI entity collection bound to a given context node.
	 * @param contextNode The context node that is an XDI entity collection.
	 * @return The XDI entity collection.
	 */
	public static XdiEntityCollection fromContextNode(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (! isValid(contextNode)) return null;

		if (contextNode.getXDIArc().isDefinition() && contextNode.getXDIArc().isVariable()) return new Definition.Variable(contextNode);
		if (contextNode.getXDIArc().isDefinition() && ! contextNode.getXDIArc().isVariable()) return new Definition(contextNode);
		if (! contextNode.getXDIArc().isDefinition() && contextNode.getXDIArc().isVariable()) return new Variable(contextNode);
		return new XdiEntityCollection(contextNode);
	}

	public static XdiEntityCollection fromXDIAddress(XDIAddress XDIaddress) {

		return fromContextNode(GraphUtil.contextNodeFromComponents(XDIaddress));
	}

	/*
	 * Instance methods
	 */

	@Override
	public XdiEntityInstanceUnordered setXdiInstanceUnordered() {

		return super.setXdiInstanceUnordered(false);
	}

	@Override
	public XdiEntityInstanceUnordered setXdiInstanceUnordered(String literal) {

		return super.setXdiInstanceUnordered(false, literal);
	}

	@Override
	public XdiEntityInstanceUnordered setXdiInstanceUnordered(boolean immutable, boolean relative) {

		return super.setXdiInstanceUnordered(false, immutable, relative);
	}

	@Override
	public XdiEntityInstanceUnordered setXdiInstanceUnordered(boolean immutable, boolean relative, String literal) {

		return super.setXdiInstanceUnordered(false, immutable, relative, literal);
	}

	@Override
	public XdiEntityInstanceUnordered getXdiInstanceUnordered(boolean immutable, boolean relative, String literal) {

		return super.getXdiInstanceUnordered(false, immutable, relative, literal);
	}

	@Override
	public XdiEntityInstanceOrdered setXdiInstanceOrdered() {

		return super.setXdiInstanceOrdered(false);
	}

	@Override
	public XdiEntityInstanceOrdered setXdiInstanceOrdered(long index) {

		return super.setXdiInstanceOrdered(false, index);
	}

	@Override
	public XdiEntityInstanceOrdered setXdiInstanceOrdered(boolean immutable, boolean relative) {

		return super.setXdiInstanceOrdered(false, immutable, relative);
	}

	@Override
	public XdiEntityInstanceOrdered setXdiInstanceOrdered(boolean immutable, boolean relative, long index) {

		return super.setXdiInstanceOrdered(false, immutable, relative, index);
	}

	@Override
	public XdiEntityInstanceOrdered getXdiInstanceOrdered(boolean immutable, boolean relative, long index) {

		return super.getXdiInstanceOrdered(false, immutable, relative, index);
	}

	/*
	 * Methods for arcs
	 */

	public static XDIArc createXDIArc(Character cs, boolean immutable, boolean relative, String literal, XDIXRef xref) {

		return XdiAbstractCollection.createXDIArc(cs, false, immutable, relative, literal, xref);
	}

	public static XDIArc createXDIArc(XDIArc XDIarc) {

		return createXDIArc(
				XDIarc.getCs(), 
				XDIarc.isImmutable(), 
				XDIarc.isRelative(), 
				XDIarc.getLiteral(), 
				XDIarc.getXRef());
	}

	public static boolean isValidXDIArc(XDIArc XDIarc) {

		if (XDIarc == null) throw new NullPointerException();

		if (! XDIarc.isCollection()) return false;
		if (XDIarc.isAttribute()) return false;

		if (XDIConstants.CS_CLASS_UNRESERVED.equals(XDIarc.getCs()) || XDIConstants.CS_CLASS_RESERVED.equals(XDIarc.getCs())) {

			if (! XDIarc.hasLiteral() && ! XDIarc.hasXRef()) return false;
		} else {

			return false;
		}

		return true;
	}

	/*
	 * Definition and Variable classes
	 */

	public static class Definition extends XdiEntityCollection implements XdiDefinition<XdiEntityCollection> {

		private static final long serialVersionUID = -8764054945300451833L;

		private Definition(ContextNode contextNode) {

			super(contextNode);
		}

		public static boolean isValid(ContextNode contextNode) {

			return XdiEntityCollection.isValid(contextNode) &&
					contextNode.getXDIArc().isDefinition() &&
					! contextNode.getXDIArc().isVariable();
		}

		public static Definition fromContextNode(ContextNode contextNode) {

			if (contextNode == null) throw new NullPointerException();

			if (! isValid(contextNode)) return null;

			return new Definition(contextNode);
		}

		public static class Variable extends Definition implements XdiVariable<XdiEntityCollection> {

			private static final long serialVersionUID = 2708216443425874389L;

			private Variable(ContextNode contextNode) {

				super(contextNode);
			}

			public static boolean isValid(ContextNode contextNode) {

				return XdiEntityCollection.isValid(contextNode) &&
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

	public static class Variable extends XdiEntityCollection implements XdiVariable<XdiEntityCollection> {

		private static final long serialVersionUID = -2507121175725369946L;

		private Variable(ContextNode contextNode) {

			super(contextNode);
		}

		public static boolean isValid(ContextNode contextNode) {

			return XdiEntityInstanceUnordered.isValid(contextNode) &&
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

	public static class MappingContextNodeXdiEntityCollectionIterator extends NotNullIterator<XdiEntityCollection> {

		public MappingContextNodeXdiEntityCollectionIterator(Iterator<ContextNode> contextNodes) {

			super(new MappingIterator<ContextNode, XdiEntityCollection> (contextNodes) {

				@Override
				public XdiEntityCollection map(ContextNode contextNode) {

					return XdiEntityCollection.fromContextNode(contextNode);
				}
			});
		}
	}
}
