package xdi2.core.features.policy.operator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xdi2.core.ContextNode;
import xdi2.core.Relation;
import xdi2.core.constants.XDIConstants;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.nodetypes.XdiAbstractEntity;
import xdi2.core.features.nodetypes.XdiInnerRoot;
import xdi2.core.features.policy.Policy;
import xdi2.core.features.policy.condition.Condition;
import xdi2.core.features.policy.evaluation.PolicyEvaluationContext;

/**
 * An XDI $true operator, represented as a relation.
 * 
 * @author markus
 */
public class TrueOperator extends ConditionOperator {

	private static final long serialVersionUID = 4296419491079293469L;

	protected TrueOperator(Relation relation) {

		super(relation);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a relation is a valid XDI $true operator.
	 * @param relation The relation to check.
	 * @return True if the relation is a valid XDI $true operator.
	 */
	public static boolean isValid(Relation relation) {

		if (! XdiAbstractEntity.isValid(relation.getContextNode())) return false;
		if (! Policy.isValid(XdiAbstractEntity.fromContextNode(relation.getContextNode()))) return false;
		if (! XDIConstants.XDI_ADD_TRUE.equals(relation.getXDIAddress())) return false;

		ContextNode targetContextNode = relation.followContextNode();

		if (targetContextNode == null) return false;
		if (! XdiInnerRoot.isValid(targetContextNode)) return false;

		return true;
	}

	/**
	 * Factory method that creates an XDI $true operator bound to a given relation.
	 * @param relation The relation that is an XDI $true operator.
	 * @return The XDI $true operator.
	 */
	public static TrueOperator fromRelation(Relation relation) {

		if (! isValid(relation)) return null;

		return new TrueOperator(relation);
	}

	public static TrueOperator createTrueOperator(Policy policy, Condition condition) {

		if (policy == null) throw new NullPointerException();

		XdiInnerRoot xdiInnerRoot = policy.getXdiEntity().getXdiInnerRoot(XDIConstants.XDI_ADD_TRUE, true);
		xdiInnerRoot.getContextNode().setStatement(condition.getXDIStatement());

		return fromRelation(xdiInnerRoot.getPredicateRelation());
	}

	/*
	 * Instance methods
	 */

	@Override
	public boolean[] evaluateInternal(PolicyEvaluationContext policyEvaluationContext) {

		Iterator<Condition> conditions = this.getConditions();
		if (conditions == null) throw new Xdi2RuntimeException("Missing or invalid condition in $true operator.");

		List<Boolean> values = new ArrayList<Boolean> ();
		while (conditions.hasNext()) values.add(Boolean.valueOf(true == conditions.next().evaluate(policyEvaluationContext)));

		boolean[] result = new boolean[values.size()];
		for (int i=0; i<values.size(); i++) result[i] = values.get(i).booleanValue();

		return result;
	}
}
