package xdi2.core.features.policy.condition;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.features.policy.evaluation.PolicyEvaluationContext;
import xdi2.core.syntax.XDIStatement;

/**
 * An XDI condition, represented as a statement.
 * 
 * @author markus
 */
public abstract class Condition implements Serializable, Comparable<Condition> {

	private static final long serialVersionUID = 78354454331218804L;

	private static final Logger log = LoggerFactory.getLogger(Condition.class);

	private XDIStatement statementXri;

	protected Condition(XDIStatement statementXri) {

		if (statementXri == null) throw new NullPointerException();

		this.statementXri = statementXri;
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a statement is a valid XDI condition.
	 * @param statement The statement to check.
	 * @return True if the statement is a valid XDI condition.
	 */
	public static boolean isValid(XDIStatement statement) {

		return
				EqualsCondition.isValid(statement) ||
				MatchesCondition.isValid(statement) ||
				GreaterCondition.isValid(statement) ||
				LesserCondition.isValid(statement) ||
				IsCondition.isValid(statement) ||
				GenericCondition.isValid(statement);
	}

	/**
	 * Factory method that creates an XDI condition bound to a given statement.
	 * @param statement The statement that is an XDI condition.
	 * @return The XDI condition.
	 */
	public static Condition fromStatement(XDIStatement statement) {

		if (EqualsCondition.isValid(statement)) return EqualsCondition.fromStatement(statement);
		if (MatchesCondition.isValid(statement)) return MatchesCondition.fromStatement(statement);
		if (GreaterCondition.isValid(statement)) return GreaterCondition.fromStatement(statement);
		if (LesserCondition.isValid(statement)) return LesserCondition.fromStatement(statement);
		if (IsCondition.isValid(statement)) return IsCondition.fromStatement(statement);
		if (GenericCondition.isValid(statement)) return GenericCondition.fromStatement(statement);

		return null;
	}

	/**
	 * Factory method that casts a Condition to the right subclass, e.g. to a EqualsCondition.
	 * @param condition The Condition to be cast.
	 * @return The casted Condition.
	 */
	public static Condition castCondition(Condition condition) {

		if (condition == null) return null;

		return fromStatement(condition.getStatementXri());
	}

	/*
	 * Instance methods
	 */

	/**
	 * Returns the underlying statement to which this XDI condition is bound.
	 * @return A statement that represents the XDI condition.
	 */
	public XDIStatement getStatementXri() {

		return this.statementXri;
	}

	/**
	 * Checks if the XDI condition evaluates to true or false.
	 * @param policyEvaluationContext A context for evaluating an XDI policy.
	 * @return True or false.
	 */
	public final Boolean evaluate(PolicyEvaluationContext policyEvaluationContext) {

		if (log.isDebugEnabled()) log.debug("Evaluating " + this.getClass().getSimpleName() + ": " + this.getStatementXri());
		Boolean result = this.evaluateInternal(policyEvaluationContext);
		if (log.isDebugEnabled()) log.debug("Evaluated " + this.getClass().getSimpleName() + ": " + this.getStatementXri() + ": " + result);

		return result;
	}

	protected abstract Boolean evaluateInternal(PolicyEvaluationContext policyEvaluationContext);

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		return this.getStatementXri().toString();
	}

	@Override
	public boolean equals(Object object) {

		if (object == null || ! (object instanceof Condition)) return(false);
		if (object == this) return(true);

		Condition other = (Condition) object;

		return this.getStatementXri().equals(other.getStatementXri());
	}

	@Override
	public int hashCode() {

		int hashCode = 1;

		hashCode = (hashCode * 31) + this.getStatementXri().hashCode();

		return hashCode;
	}

	@Override
	public int compareTo(Condition other) {

		if (other == this || other == null) return(0);

		return this.getStatementXri().compareTo(other.getStatementXri());
	}
}