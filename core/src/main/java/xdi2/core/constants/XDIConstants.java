package xdi2.core.constants;

import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;

/**
 * General XDI syntax and graph constants.
 * 
 * @author markus
 */
public class XDIConstants {

	public static final Character CS_AUTHORITY_PERSONAL = Character.valueOf('='); 
	public static final Character CS_AUTHORITY_LEGAL = Character.valueOf('+'); 
	public static final Character CS_CLASS_RESERVED = Character.valueOf('$'); 
	public static final Character CS_CLASS_UNRESERVED = Character.valueOf('#'); 
	public static final Character CS_INSTANCE_ORDERED = Character.valueOf('@');
	public static final Character CS_INSTANCE_UNORDERED = Character.valueOf('*'); 
	public static final Character CS_LITERAL = Character.valueOf('&');

	public static final XDIArc XDI_ARC_CS_AUTHORITY_PERSONAL = XDIArc.fromComponents(CS_AUTHORITY_PERSONAL, false, false, false, false, false, false, null, null);
	public static final XDIArc XDI_ARC_CS_AUTHORITY_LEGAL = XDIArc.fromComponents(CS_AUTHORITY_LEGAL, false, false, false, false, false, false, null, null);
	public static final XDIArc XDI_ARC_CS_CLASS_RESERVED = XDIArc.fromComponents(CS_CLASS_RESERVED, false, false, false, false, false, false, null, null);
	public static final XDIArc XDI_ARC_CS_CLASS_UNRESERVED = XDIArc.fromComponents(CS_CLASS_UNRESERVED, false, false, false, false, false, false, null, null);
	public static final XDIArc XDI_ARC_CS_INSTANCE_ORDERED = XDIArc.fromComponents(CS_INSTANCE_ORDERED, false, false, false, false, false, false, null, null);
	public static final XDIArc XDI_ARC_CS_INSTANCE_UNORDERED = XDIArc.fromComponents(CS_INSTANCE_UNORDERED, false, false, false, false, false, false, null, null);
	public static final XDIArc XDI_ARC_CS_LITERAL = XDIArc.fromComponents(CS_LITERAL, false, false, false, false, false, false, null, null);

	public static final XDIAddress XDI_ADD_CS_AUTHORITY_PERSONAL = XDIAddress.fromComponent(XDI_ARC_CS_AUTHORITY_PERSONAL);
	public static final XDIAddress XDI_ADD_CS_AUTHORITY_LEGAL = XDIAddress.fromComponent(XDI_ARC_CS_AUTHORITY_LEGAL);
	public static final XDIAddress XDI_ADD_CS_CLASS_RESERVED = XDIAddress.fromComponent(XDI_ARC_CS_CLASS_RESERVED);
	public static final XDIAddress XDI_ADD_CS_CLASS_UNRESERVED = XDIAddress.fromComponent(XDI_ARC_CS_CLASS_UNRESERVED);
	public static final XDIAddress XDI_ADD_CS_INSTANCE_ORDERED = XDIAddress.fromComponent(XDI_ARC_CS_INSTANCE_ORDERED);
	public static final XDIAddress XDI_ADD_CS_INSTANCE_UNORDERED = XDIAddress.fromComponent(XDI_ARC_CS_INSTANCE_UNORDERED);
	public static final XDIAddress XDI_ADD_CS_LITERAL = XDIAddress.fromComponent(XDI_ARC_CS_LITERAL);

	public static final Character[] CS_ARRAY = new Character[] { CS_AUTHORITY_PERSONAL, CS_AUTHORITY_LEGAL, CS_CLASS_RESERVED, CS_CLASS_UNRESERVED, CS_INSTANCE_ORDERED, CS_INSTANCE_UNORDERED, CS_LITERAL };

	public static final Character S_IMMUTABLE = Character.valueOf('!');
	public static final Character S_RELATIVE = Character.valueOf('~');

	public static final String XS_ROOT = "()";
	public static final String XS_VARIABLE = "{}";
	public static final String XS_DEFINITION = "||";
	public static final String XS_COLLECTION = "[]";
	public static final String XS_ATTRIBUTE = "<>";

	public static final XDIAddress XDI_ADD_ROOT = XDIAddress.create("");

	public static final String STRING_CONTEXT = "";

	public static final XDIArc XDI_ARC_LITERAL = XDIArc.create(CS_LITERAL.toString());

	public static final XDIArc XDI_ARC_COMMON_VARIABLE = XDIArc.create(XDIConstants.XS_VARIABLE);
	public static final XDIArc XDI_ARC_COMMON_DEFINITION = XDIArc.create(XDIConstants.XS_DEFINITION);
	public static final XDIAddress XDI_ADD_COMMON_VARIABLE = XDIAddress.fromComponent(XDI_ARC_COMMON_VARIABLE);
	public static final XDIAddress XDI_ADD_COMMON_DEFINITION = XDIAddress.fromComponent(XDI_ARC_COMMON_DEFINITION);

	public static final XDIArc XDI_ARC_LITERAL_VARIABLE = XDIArc.create("" + XS_VARIABLE.charAt(0) + CS_LITERAL.charValue() + XS_VARIABLE.charAt(1));
	public static final XDIAddress XDI_ADD_LITERAL_VARIABLE = XDIAddress.fromComponent(XDI_ARC_LITERAL_VARIABLE);

	public static final XDIArc XDI_ARC_TRUE = XDIArc.create("$true");
	public static final XDIArc XDI_ARC_FALSE = XDIArc.create("$false");

	public static final XDIAddress XDI_ADD_TRUE = XDIAddress.fromComponent(XDI_ARC_TRUE);
	public static final XDIAddress XDI_ADD_FALSE = XDIAddress.fromComponent(XDI_ARC_FALSE);

	private XDIConstants() { }
}
