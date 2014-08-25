package xdi2.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.constants.XDIConstants;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.nodetypes.XdiAbstractContext;
import xdi2.core.features.nodetypes.XdiContext;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIXRef;

/**
 * Various utility methods for working with XDI addresses.
 * 
 * @author markus
 */
public final class XDIAddressUtil {

	private static final Logger log = LoggerFactory.getLogger(XDIAddressUtil.class);

	private XDIAddressUtil() { }

	/**
	 * Checks if an address starts with a certain other address.
	 */
	public static XDIAddress startsWithXDIAddress(final XDIAddress XDIaddress, final XDIAddress startXDIAddress, final boolean variablesinXDIAddress, final boolean variablesInStart) {

		if (XDIaddress == null) throw new NullPointerException();
		if (startXDIAddress == null) throw new NullPointerException();

		XDIAddress result = null;

		try {

			if (startXDIAddress.equals(XDIConstants.XDI_ADD_ROOT)) { result = XDIConstants.XDI_ADD_ROOT; return result; }
			if (XDIaddress.equals(XDIConstants.XDI_ADD_ROOT)) { result = null; return result; }

			int addressIndex = 0, startIndex = 0;

			while (true) {

				if (startIndex == startXDIAddress.getNumXDIArcs()) { result = XDIAddressUtil.parentXDIAddress(XDIaddress, addressIndex); return result; }
				if (addressIndex == XDIaddress.getNumXDIArcs()) { result = null; return result; }

				// check variables

				if (variablesinXDIAddress && VariableUtil.isVariable(XDIaddress.getXDIArc(addressIndex))) {

					if (VariableUtil.matches(XDIaddress.getXDIArc(addressIndex), startXDIAddress.getXDIArc(startIndex))) {

						startIndex++;

						if (VariableUtil.isMultiple(XDIaddress.getXDIArc(addressIndex))) {

							while (true) {

								if (startIndex == startXDIAddress.getNumXDIArcs()) break;
								if (! VariableUtil.matches(XDIaddress.getXDIArc(addressIndex), startXDIAddress.getXDIArc(startIndex))) break;
								if (addressIndex + 1 < XDIaddress.getNumXDIArcs() && XDIaddress.getXDIArc(addressIndex + 1).equals(startXDIAddress.getXDIArc(startIndex))) break;

								startIndex++;
							}
						}

						addressIndex++;

						continue;
					} else {

						{ result = null; return result; }
					}
				}

				if (variablesInStart && VariableUtil.isVariable(startXDIAddress.getXDIArc(startIndex))) {

					if (VariableUtil.matches(startXDIAddress.getXDIArc(startIndex), XDIaddress.getXDIArc(addressIndex))) {

						addressIndex++;

						if (VariableUtil.isMultiple(startXDIAddress.getXDIArc(startIndex))) {

							while (true) {

								if (addressIndex == XDIaddress.getNumXDIArcs()) break;
								if (! VariableUtil.matches(startXDIAddress.getXDIArc(startIndex), XDIaddress.getXDIArc(addressIndex))) break;
								if (startIndex + 1 < startXDIAddress.getNumXDIArcs() && XDIaddress.getXDIArc(addressIndex).equals(startXDIAddress.getXDIArc(startIndex + 1))) break;

								addressIndex++;
							}
						}

						startIndex++;

						continue;
					} else {

						{ result = null; return result; }
					}
				}

				// no variables? just match the arc

				if (! (XDIaddress.getXDIArc(addressIndex).equals(startXDIAddress.getXDIArc(startIndex)))) { result = null; return result; }

				addressIndex++;
				startIndex++;
			}
		} finally {

			if (log.isTraceEnabled()) log.trace("startsWithXDIAddress(" + XDIaddress + "," + startXDIAddress + "," + variablesinXDIAddress + "," + variablesInStart + ") --> " + result);
		}
	}

	/**
	 * Checks if an address starts with a certain other address.
	 */
	public static XDIAddress startsWithXDIAddress(XDIAddress XDIaddress, XDIAddress startXDIAddress) {

		return startsWithXDIAddress(XDIaddress, startXDIAddress, false, false);
	}

	/**
	 * Checks if an address ends with a certain other address.
	 */
	public static XDIAddress endsWithXDIAddress(final XDIAddress XDIaddress, final XDIAddress endXDIAddress, final boolean variablesinXDIAddress, final boolean variablesInEnd) {

		if (XDIaddress == null) throw new NullPointerException();
		if (endXDIAddress == null) throw new NullPointerException();

		XDIAddress result = null;

		try {

			if (endXDIAddress.equals(XDIConstants.XDI_ADD_ROOT)) { result = XDIConstants.XDI_ADD_ROOT; return result; }
			if (XDIaddress.equals(XDIConstants.XDI_ADD_ROOT)) { result = null; return result; }

			int addressIndex = XDIaddress.getNumXDIArcs() - 1, endIndex = endXDIAddress.getNumXDIArcs() - 1;

			while (true) {

				if (endIndex == -1) { result = XDIAddressUtil.localXDIAddress(XDIaddress, - addressIndex - 1); return result; }
				if (addressIndex == -1) { result = null; return result; }

				// check variables

				if (variablesinXDIAddress && VariableUtil.isVariable(XDIaddress.getXDIArc(addressIndex))) {

					if (VariableUtil.matches(XDIaddress.getXDIArc(addressIndex), endXDIAddress.getXDIArc(endIndex))) {

						endIndex--;

						if (VariableUtil.isMultiple(XDIaddress.getXDIArc(addressIndex))) {

							while (true) {

								if (endIndex == -1) break;
								if (! VariableUtil.matches(XDIaddress.getXDIArc(addressIndex), endXDIAddress.getXDIArc(endIndex))) break;
								if (addressIndex - 1 > -1 && XDIaddress.getXDIArc(addressIndex - 1).equals(endXDIAddress.getXDIArc(endIndex))) break;

								endIndex--;
							}
						}

						addressIndex--;

						continue;
					} else {

						{ result = null; return result; }
					}
				}

				if (variablesInEnd && VariableUtil.isVariable(endXDIAddress.getXDIArc(endIndex))) {

					if (VariableUtil.matches(endXDIAddress.getXDIArc(endIndex), XDIaddress.getXDIArc(addressIndex))) {

						addressIndex--;

						if (VariableUtil.isMultiple(endXDIAddress.getXDIArc(endIndex))) {

							while (true) {

								if (addressIndex == -1) break;
								if (! VariableUtil.matches(endXDIAddress.getXDIArc(endIndex), XDIaddress.getXDIArc(addressIndex))) break;
								if (endIndex - 1 > -1 && XDIaddress.getXDIArc(addressIndex).equals(endXDIAddress.getXDIArc(endIndex - 1))) break;

								addressIndex--;
							}
						}

						endIndex--;

						continue;
					} else {

						{ result = null; return result; }
					}
				}

				// no variables? just match the arc

				if (! (XDIaddress.getXDIArc(addressIndex).equals(endXDIAddress.getXDIArc(endIndex)))) { result = null; return result; }

				addressIndex--;
				endIndex--;
			}
		} finally {

			if (log.isTraceEnabled()) log.trace("endsWithXDIAddress(" + XDIaddress + "," + endXDIAddress + "," + variablesinXDIAddress + "," + variablesInEnd + ") --> " + result);
		}
	}

	/**
	 * Checks if an address ends with a certain other address.
	 */
	public static XDIAddress endsWithXDIAddress(final XDIAddress XDIaddress, final XDIAddress endXDIAddress) {

		return endsWithXDIAddress(XDIaddress, endXDIAddress, false, false);
	}

	/**
	 * Extracts an address's parent arc(s), counting either from the start or the end.
	 * For =a*b*c*d and 1, this returns =a
	 * For =a*b*c*d and -1, this returns =a*b*c
	 */
	public static XDIAddress parentXDIAddress(final XDIAddress XDIaddress, final int numArcs) {

		if (XDIaddress == null) throw new NullPointerException();

		XDIAddress result = null;

		try {

			List<XDIArc> arcs = new ArrayList<XDIArc> ();

			if (numArcs > 0) {

				for (int i = 0; i < numArcs; i++) arcs.add(XDIaddress.getXDIArc(i));
			} else if (numArcs < 0) {

				for (int i = 0; i < XDIaddress.getNumXDIArcs() - (- numArcs); i++) arcs.add(XDIaddress.getXDIArc(i));
			} else {

				{ result = XDIaddress; return result; }
			}

			if (arcs.size() == 0) { result = null; return result; }

			{ result = XDIAddress.fromComponents(arcs); return result; }
		} finally {

			if (log.isTraceEnabled()) log.trace("parentXDIAddress(" + XDIaddress + "," + numArcs + ") --> " + result);
		}
	}

	/**
	 * Extracts an address's local arc(s).
	 * For =a*b*c*d and 1, this returns *d
	 * For =a*b*c*d and -1, this returns *b*c*d
	 */
	public static XDIAddress localXDIAddress(final XDIAddress XDIaddress, final int numArcs) {

		if (XDIaddress == null) throw new NullPointerException();

		XDIAddress result = null;

		try {

			List<XDIArc> arcs = new ArrayList<XDIArc> ();

			if (numArcs > 0) {

				for (int i = XDIaddress.getNumXDIArcs() - numArcs; i < XDIaddress.getNumXDIArcs(); i++) arcs.add(XDIaddress.getXDIArc(i));
			} else if (numArcs < 0) {

				for (int i = (- numArcs); i < XDIaddress.getNumXDIArcs(); i++) arcs.add(XDIaddress.getXDIArc(i));
			} else {

				{ result = XDIaddress; return XDIaddress; }
			}

			if (arcs.size() == 0) { result = null; return result; }

			{ result = XDIAddress.fromComponents(arcs); return result; }
		} finally {

			if (log.isTraceEnabled()) log.trace("localXDIAddress(" + XDIaddress + "," + numArcs + ") --> " + result);
		}
	}

	/**
	 * Extracts an address from an address.
	 * For =a*b*c*d and 1, this returns *d
	 * For =a*b*c*d and -1, this returns *b*c*d
	 */
	public static XDIAddress subXDIAddress(final XDIAddress XDIaddress, final int startIndex, final int endIndex) {

		if (XDIaddress == null) throw new NullPointerException();

		return XDIAddressUtil.localXDIAddress(XDIAddressUtil.parentXDIAddress(XDIaddress, endIndex), - startIndex);
	}

	/**
	 * Finds a part of an XDI address that matches a certain node type.
	 */
	public static <X extends XdiContext<?>> XDIAddress findXDIAddress(XDIAddress XDIaddress, Class<X> clazz) {

		try {

			ContextNode contextNode = GraphUtil.contextNodeFromComponents(XDIaddress);
			XdiContext<?> xdiContext = null;

			while (contextNode != null) {

				xdiContext = XdiAbstractContext.fromContextNode(contextNode);
				if (clazz.isAssignableFrom(xdiContext.getClass())) return contextNode.getXDIAddress();

				contextNode = contextNode.getContextNode();
			}

			return null;
		} catch (Exception ex) {

			throw new Xdi2RuntimeException("Unexpected reflect error: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Get the index of an arc inside an address.
	 * For =a*b*c*d and *b, this returns =a*b
	 * For =a*b*c*d and *c, this returns =a*b*c
	 * For =a*b*c*d and *x, this returns null
	 */
	public static int indexOfXDIArc(final XDIAddress XDIaddress, final XDIArc search) {

		if (XDIaddress == null) throw new NullPointerException();
		if (search == null) throw new NullPointerException();

		for (int i=0; i<XDIaddress.getNumXDIArcs(); i++) {

			XDIArc XDIarc = XDIaddress.getXDIArc(i);

			if (XDIarc.equals(search)) return i;
		}

		return -1;
	}

	/**
	 * Get the last index of an arc inside an address.
	 * For =a*b*c*d and *b, this returns *b*c*d
	 * For =a*b*c*d and *c, this returns *c*d
	 * For =a*b*c*d and *x, this returns null
	 */
	public static int lastIndexOfXDIArc(final XDIAddress XDIaddress, final XDIArc search) {

		if (XDIaddress == null) throw new NullPointerException();
		if (search == null) throw new NullPointerException();

		for (int i=XDIaddress.getNumXDIArcs()-1; i>=0; i--) {

			XDIArc XDIarc = XDIaddress.getXDIArc(i);

			if (XDIarc.equals(search)) return i;
		}

		return -1;
	}

	/**
	 * Removes a start address from an address.
	 * E.g. for =a*b*c*d and =a*b, this returns *c*d
	 * E.g. for =a*b*c*d and (empty address), this returns =a*b*c*d
	 * E.g. for =a*b*c*d and =a*b*c*d, this returns (empty address)
	 * E.g. for =a*b*c*d and =x, this returns null
	 */
	public static XDIAddress removeStartXDIAddress(final XDIAddress XDIaddress, final XDIAddress start, final boolean variablesinXDIAddress, final boolean variablesInStart) {

		if (XDIaddress == null) throw new NullPointerException();
		if (start == null) throw new NullPointerException();

		XDIAddress result = null;

		try {

			if (start.equals(XDIConstants.XDI_ADD_ROOT)) { result = XDIaddress; return result; }
			if (XDIaddress.equals(XDIConstants.XDI_ADD_ROOT)) { result = null; return result; }

			XDIAddress startXDIAddress = XDIAddressUtil.startsWithXDIAddress(XDIaddress, start, variablesinXDIAddress, variablesInStart);
			if (startXDIAddress == null) { result = null; return result; }

			if (XDIaddress.equals(startXDIAddress)) { result = XDIConstants.XDI_ADD_ROOT; return result; }

			{ result = XDIAddressUtil.localXDIAddress(XDIaddress, - startXDIAddress.getNumXDIArcs()); return result; }
		} finally {

			if (log.isTraceEnabled()) log.trace("removeStartXDIAddress(" + XDIaddress + "," + start + "," + variablesinXDIAddress + "," + variablesInStart + ") --> " + result);
		}
	}

	/**
	 * Removes a start address from an address.
	 * E.g. for =a*b*c*d and =a*b, this returns *c*d
	 * E.g. for =a*b*c*d and (), this returns =a*b*c*d
	 * E.g. for =a*b*c*d and =a*b*c*d, this returns ()
	 * E.g. for =a*b*c*d and =x, this returns null
	 */
	public static XDIAddress removeStartXDIAddress(final XDIAddress XDIaddress, final XDIAddress start) {

		return removeStartXDIAddress(XDIaddress, start, false, false);
	}

	/**
	 * Removes an end address from an address.
	 * E.g. for =a*b*c*d and *c*d, this returns =a*b
	 * E.g. for =a*b*c*d and (), this returns =a*b*c*d
	 * E.g. for =a*b*c*d and =a*b*c*d, this returns (empty address)
	 * E.g. for =a*b*c*d and *y, this returns null
	 */
	public static XDIAddress removeEndXDIAddress(final XDIAddress XDIaddress, final XDIAddress end, final boolean variablesinXDIAddress, final boolean variablesInEnd) {

		if (XDIaddress == null) throw new NullPointerException();
		if (end == null) throw new NullPointerException();

		XDIAddress result = null;

		try {

			if (end.equals(XDIConstants.XDI_ADD_ROOT)) { result = XDIaddress; return result; }
			if (XDIaddress.equals(XDIConstants.XDI_ADD_ROOT)) { result = null; return result; }

			XDIAddress endXDIAddress = XDIAddressUtil.endsWithXDIAddress(XDIaddress, end, variablesinXDIAddress, variablesInEnd);
			if (endXDIAddress == null) { result = null; return result; }

			if (XDIaddress.equals(endXDIAddress)) { result = XDIConstants.XDI_ADD_ROOT; return result; }

			{ result = XDIAddressUtil.parentXDIAddress(XDIaddress, - endXDIAddress.getNumXDIArcs()); return result; }
		} finally {

			if (log.isTraceEnabled()) log.trace("removeEndXDIAddress(" + XDIaddress + "," + end + "," + variablesinXDIAddress + "," + variablesInEnd + ") --> " + result);
		}
	}

	/**
	 * Removes an end address from an address.
	 * E.g. for =a*b*c*d and *c*d, this returns =a*b
	 * E.g. for =a*b*c*d and (), this returns =a*b*c*d
	 * E.g. for =a*b*c*d and =a*b*c*d, this returns ()
	 * E.g. for =a*b*c*d and *y, this returns null
	 */
	public static XDIAddress removeEndXDIAddress(final XDIAddress XDIaddress, final XDIAddress end) {

		return removeEndXDIAddress(XDIaddress, end, false, false);
	}

	/**
	 * Replaces all occurences of an arc with an address.
	 */
	public static XDIAddress replaceXDIAddress(final XDIAddress XDIaddress, final XDIArc oldXDIArc, final XDIAddress newAddress) {

		if (XDIaddress == null) throw new NullPointerException();
		if (oldXDIArc == null) throw new NullPointerException();
		if (newAddress == null) throw new NullPointerException();

		XDIAddress result = null;

		try {

			List<XDIArc> arcs = new ArrayList<XDIArc> ();

			for (XDIArc XDIarc : XDIaddress.getXDIArcs()) {

				if (XDIarc.equals(oldXDIArc)) {

					arcs.addAll(newAddress.getXDIArcs());

					continue;
				}

				if (XDIarc.hasXRef() && XDIarc.getXRef().hasXDIAddress()) {

					XDIAddress xRefAddress = XDIarc.getXRef().getXDIAddress();

					xRefAddress = replaceXDIAddress(xRefAddress, oldXDIArc, newAddress);

					arcs.add(XDIArc.fromComponents(XDIarc.getCs(), XDIarc.isClassXs(), XDIarc.isAttributeXs(), null, XDIXRef.fromComponents(XDIarc.getXRef().getXs(), xRefAddress, null, null, null, null)));

					continue;
				}

				if (XDIarc.hasXRef() && XDIarc.getXRef().hasPartialSubjectAndPredicate()) {

					XDIAddress xRefPartialSubject = XDIarc.getXRef().getPartialSubject();
					XDIAddress xRefPartialPredicate = XDIarc.getXRef().getPartialPredicate();

					xRefPartialSubject = replaceXDIAddress(xRefPartialSubject, oldXDIArc, newAddress);
					xRefPartialPredicate = replaceXDIAddress(xRefPartialPredicate, oldXDIArc, newAddress);

					arcs.add(XDIArc.fromComponents(XDIarc.getCs(), XDIarc.isClassXs(), XDIarc.isAttributeXs(), null, XDIXRef.fromComponents(XDIarc.getXRef().getXs(), null, xRefPartialSubject, xRefPartialPredicate, null, null)));

					continue;
				}

				arcs.add(XDIarc);
			}

			{ result = XDIAddress.fromComponents(arcs); return result; }
		} finally {

			if (log.isTraceEnabled()) log.trace("replaceAddress(" + XDIaddress + "," + oldXDIArc + "," + newAddress + ") --> " + result);
		}
	}

	/**
	 * Concats all addresses into a new address.
	 */
	public static XDIAddress concatXDIAddresses(final XDIAddress... addresses) {

		XDIAddress result = null;

		try {

			List<XDIArc> arcs = new ArrayList<XDIArc> ();

			if (addresses != null) {

				for (XDIAddress XDIaddress : addresses) {

					if (XDIaddress != null) arcs.addAll(XDIaddress.getXDIArcs());
				}
			}

			if (arcs.size() == 0) arcs.addAll(XDIConstants.XDI_ADD_ROOT.getXDIArcs());

			{ result = XDIAddress.fromComponents(arcs); return result; }
		} finally {

			if (log.isTraceEnabled()) log.trace("contactXDIAddresses(" + Arrays.asList(addresses) + ") --> " + result);
		}
	}

	/**
	 * Concats address(es) and arc(s) into a new address.
	 */
	public static XDIAddress concatXDIAddresses(final XDIArc... arcs) {

		XDIAddress[] addresses = new XDIAddress[arcs.length];
		for (int i=0; i<arcs.length; i++) addresses[i] = XDIAddress.fromComponent(arcs[i]);

		return concatXDIAddresses(addresses);
	}

	/**
	 * Concats address(es) and arc(s) into a new address.
	 */
	public static XDIAddress concatXDIAddresses(final XDIAddress XDIaddress, final XDIArc XDIarc) {

		return concatXDIAddresses(XDIaddress, XDIarc == null ? null : XDIAddress.fromComponent(XDIarc));
	}

	/**
	 * Concats address(es) and arc(s) into a new address.
	 */
	public static XDIAddress concatXDIAddresses(final XDIArc XDIarc, final XDIAddress XDIaddress) {

		return concatXDIAddresses(XDIarc == null ? null : XDIAddress.fromComponent(XDIarc), XDIaddress);
	}

	/*
	 * Helper classes
	 */

	public static final Comparator<? super XDIAddress> XDIADDRESS_ASCENDING_COMPARATOR = new Comparator<XDIAddress>() {

		@Override
		public int compare(XDIAddress o1, XDIAddress o2) {

			if (o1.getNumXDIArcs() < o2.getNumXDIArcs()) return -1;
			if (o1.getNumXDIArcs() > o2.getNumXDIArcs()) return 1;

			return o1.compareTo(o2);
		}
	};

	public static final Comparator<? super XDIAddress> XDIADDRESS_DESCENDING_COMPARATOR = new Comparator<XDIAddress>() {

		@Override
		public int compare(XDIAddress o1, XDIAddress o2) {

			if (o1.getNumXDIArcs() > o2.getNumXDIArcs()) return -1;
			if (o1.getNumXDIArcs() < o2.getNumXDIArcs()) return 1;

			return o1.compareTo(o2);
		}
	};
}
