package xdi2.core.features.index;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.aggregation.Aggregation;
import xdi2.core.features.nodetypes.XdiAbstractAttribute;
import xdi2.core.features.nodetypes.XdiAbstractEntity;
import xdi2.core.features.nodetypes.XdiAttribute;
import xdi2.core.features.nodetypes.XdiAttributeCollection;
import xdi2.core.features.nodetypes.XdiEntity;
import xdi2.core.features.nodetypes.XdiEntityCollection;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.iterators.ReadOnlyIterator;

public class Index {

	private Index() { }

	public static XdiEntityCollection getEntityIndex(Graph graph, XDIArc indexXDIArc, boolean create) {

		XDIArc xdiEntityCollectionXDIArc = XdiEntityCollection.createXDIArc(indexXDIArc);

		ContextNode contextNode = create ? graph.getRootContextNode().setContextNode(xdiEntityCollectionXDIArc) : graph.getRootContextNode().getContextNode(xdiEntityCollectionXDIArc);
		if (contextNode == null) return null;

		return XdiEntityCollection.fromContextNode(contextNode);
	}

	public static XdiAttributeCollection getAttributeIndex(Graph graph, XDIArc indexXDIArc, boolean create) {

		XDIArc xdiAttributeCollectionXDIArc = XdiAttributeCollection.createXDIArc(indexXDIArc);

		ContextNode contextNode = create ? graph.getRootContextNode().getContextNode(xdiAttributeCollectionXDIArc) : graph.getRootContextNode().getContextNode(xdiAttributeCollectionXDIArc);
		if (contextNode == null) return null;

		return XdiAttributeCollection.fromContextNode(contextNode);
	}

	public static void setEntityIndexAggregation(XdiEntityCollection xdiEntityCollection, XdiEntity xdiEntity) {

		Aggregation.setAggregationContextNode(xdiEntityCollection.getContextNode(), xdiEntity.getContextNode());
	}

	public static void setEntityIndexAggregation(XdiEntityCollection xdiEntityCollection, XDIAddress XDIaddress) {

		Aggregation.setAggregationContextNode(xdiEntityCollection.getContextNode(), XDIaddress);
	}

	public static void setAttributeIndexAggregation(XdiAttributeCollection xdiAttributeCollection, XdiAttribute xdiAttribute) {

		Aggregation.setAggregationContextNode(xdiAttributeCollection.getContextNode(), xdiAttribute.getContextNode());
	}

	public static void setAttributeIndexAggregation(XdiAttributeCollection xdiAttributeCollection, XDIAddress XDIaddress) {

		Aggregation.setAggregationContextNode(xdiAttributeCollection.getContextNode(), XDIaddress);
	}

	public static ReadOnlyIterator<XdiEntity> getEntityIndexAggregations(XdiEntityCollection xdiEntityCollection) {

		return new XdiAbstractEntity.MappingContextNodeXdiEntityIterator(Aggregation.getAggregationContextNodes(xdiEntityCollection.getContextNode()));
	}

	public static ReadOnlyIterator<XdiAttribute> getAttributeIndexAggregations(XdiAttributeCollection xdiAttributeCollection) {

		return new XdiAbstractAttribute.MappingContextNodeXdiAttributeIterator(Aggregation.getAggregationContextNodes(xdiAttributeCollection.getContextNode()));
	}
}
