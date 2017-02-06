package xdi2.core.features.policy.operator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xdi2.core.Relation;
import xdi2.core.constants.XDIConstants;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.nodetypes.XdiAbstractEntity;
import xdi2.core.features.nodetypes.XdiInnerRoot;
import xdi2.core.features.policy.Policy;
import xdi2.core.features.policy.condition.Condition;
import xdi2.core.features.policy.evaluation.PolicyEvaluationContext;

/**
 * An XDI $false operator, represented as a relation.
 * 
 * @author markus
 */
public class FalseOperator extends ConditionOperator {

	private static final long serialVersionUID = -7397004800836677763L;

	protected FalseOperator(Relation relation) {

		super(relation);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a relation is a valid XDI $false operator.
	 * @param relation The relation to check.
	 * @return True if the relation is a valid XDI $false operator.
	 */
	public static boolean isValid(Relation relation) {

		if (! XdiAbstractEntity.isValid(relation.getContextNode())) return false;
		if (! Policy.isValid(XdiAbstractEntity.fromContextNode(relation.getContextNode()))) return false;
		if (! XDIConstants.XDI_ADD_FALSE.equals(relation.getXDIAddress())) return false;

		return true;
	}

	/**
	 * Factory method that creates an XDI $false operator bound to a given relation.
	 * @param relation The relation that is an XDI $false operator.
	 * @return The XDI $false operator.
	 */
	public static FalseOperator fromRelation(Relation relation) {

		if (! isValid(relation)) return null;

		return new FalseOperator(relation);
	}

	public static FalseOperator createFalseOperator(Policy policy, Condition condition) {

		if (policy == null) throw new NullPointerException();

		XdiInnerRoot xdiInnerRoot = policy.getXdiEntity().getXdiInnerRoot(XDIConstants.XDI_ADD_FALSE, true);
		xdiInnerRoot.getContextNode().setStatement(condition.getXDIStatement());

		return fromRelation(xdiInnerRoot.getPredicateRelation());
	}

	/*
	 * Instance methods
	 */

	@Override
	public boolean[] evaluateInternal(PolicyEvaluationContext policyEvaluationContext) {

		Iterator<Condition> conditions = this.getConditions();
		if (conditions == null) throw new Xdi2RuntimeException("Missing or invalid condition in $false operator.");

		List<Boolean> values = new ArrayList<Boolean> ();
		while (conditions.hasNext()) values.add(Boolean.valueOf(false == conditions.next().evaluate(policyEvaluationContext)));

		boolean[] result = new boolean[values.size()];
		for (int i=0; i<values.size(); i++) result[i] = values.get(i).booleanValue();

		return result;
	}
}
