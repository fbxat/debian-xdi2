package xdi2.tests.core.features.nodetypes;

import junit.framework.TestCase;
import xdi2.core.Graph;
import xdi2.core.features.nodetypes.XdiCommonRoot;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;

public class PeerRootsTest extends TestCase {

	public void testPeerRootAddresses() throws Exception {
		
		assertFalse(XdiPeerRoot.isValidXDIArc(XDIArc.create("")));
		assertTrue(XdiPeerRoot.isValidXDIArc(XDIArc.create("([=]!1111!23)")));
		assertFalse(XdiPeerRoot.isValidXDIArc(XDIArc.create("(=a*b/+c*d)")));

		assertFalse(XdiPeerRoot.isValidXDIArc(XDIArc.create("[<+c>]")));
		assertFalse(XdiPeerRoot.isValidXDIArc(XDIArc.create("{1}")));
		assertFalse(XdiPeerRoot.isValidXDIArc(XDIArc.create("{[<+(name)>]}")));
		assertFalse(XdiPeerRoot.isValidXDIArc(XDIArc.create("<+(name)>")));
		assertFalse(XdiPeerRoot.isValidXDIArc(XDIArc.create("[+(name)]")));
		assertFalse(XdiPeerRoot.isValidXDIArc(XDIArc.create("+(name)")));

		assertEquals(XdiPeerRoot.createPeerRootXDIArc(XDIAddress.create("[=]!1111!23")), XDIArc.create("([=]!1111!23)"));
		assertEquals(XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(XDIArc.create("([=]!1111!23)")), XDIAddress.create("[=]!1111!23"));
	}

	public void testPeerRoots() throws Exception {

		Graph graph = MemoryGraphFactory.getInstance().openGraph();

		assertEquals(XdiCommonRoot.findCommonRoot(graph).getPeerRoot(XDIAddress.create("[=]!1111!23"), true).getContextNode().getXDIAddress(), XDIArc.create("([=]!1111!23)"));
		
		graph.close();
	}

	public void testSelfPeerRoots() throws Exception {

		Graph graph = MemoryGraphFactory.getInstance().openGraph();
		XdiCommonRoot.findCommonRoot(graph).setSelfPeerRoot(XDIAddress.create("[=]!1111!23"));

		XdiPeerRoot selfPeerRoot = XdiCommonRoot.findCommonRoot(graph).getSelfPeerRoot();

		assertEquals(selfPeerRoot.getContextNode().getXDIAddress(), XDIAddress.create("([=]!1111!23)"));
		assertEquals(XdiCommonRoot.findCommonRoot(graph).getPeerRoot(XDIAddress.create("[=]!1111!23"), false), selfPeerRoot);
		assertTrue(selfPeerRoot.isSelfPeerRoot());

		graph.close();
	}
}
