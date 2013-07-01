package xdi2.messaging.target.contributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.constants.XDIConstants;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.StatementUtil;
import xdi2.core.util.XDI3Util;
import xdi2.core.util.iterators.DescendingIterator;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.messaging.MessageResult;
import xdi2.messaging.Operation;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.ExecutionContext;
import xdi2.messaging.target.Prototype;

public class ContributorMap extends LinkedHashMap<XDI3Segment, List<Contributor>> implements Iterable<Contributor>, Prototype<ContributorMap> {

	private static final long serialVersionUID = 1645889897751813459L;

	private static final Logger log = LoggerFactory.getLogger(ContributorMap.class);

	public ContributorMap() {

		super();
	}

	public void addContributor(XDI3Segment contributorXri, Contributor contributor) {

		log.debug("Adding contributor " + contributor.getClass().getSimpleName() + " under " + contributorXri);

		List<Contributor> contributors = this.get(contributorXri);

		if (contributors == null) {

			contributors = new ArrayList<Contributor> ();
			this.put(contributorXri, contributors);
		}

		contributors.add(contributor);
	}

	public void addContributor(Contributor contributor) {

		ContributorXri contributorCall = contributor.getClass().getAnnotation(ContributorXri.class);

		for (String address : contributorCall.addresses()) {

			this.addContributor(XDI3Segment.create(address), contributor);
		}
	}

	public void removeContributor(XDI3Segment contributorXri, Contributor contributor) {

		log.debug("Removing contributor " + contributor.getClass().getSimpleName() + " from " + contributorXri);

		List<Contributor> contributors = this.get(contributorXri);
		if (contributors == null) return;

		contributors.remove(contributor);

		if (contributors.isEmpty()) {

			this.remove(contributorXri);
		}
	}

	public void removeContributor(Contributor contributor) {

		for (Iterator<Map.Entry<XDI3Segment, List<Contributor>>> entries = this.entrySet().iterator(); entries.hasNext(); ) {

			Map.Entry<XDI3Segment, List<Contributor>> entry = entries.next();

			if (entry.getValue().contains(contributor)) entry.getValue().remove(contributor);
			if (entry.getValue().isEmpty()) entries.remove();
		}
	}

	@Override
	public Iterator<Contributor> iterator() {

		return new DescendingIterator<List<Contributor>, Contributor> (this.values().iterator()) {

			@Override
			public Iterator<Contributor> descend(List<Contributor> item) {

				return item.iterator();
			}
		};
	}

	public String stringList() {

		StringBuffer buffer = new StringBuffer();

		for (Contributor contributor : this) {

			if (buffer.length() > 0) buffer.append(",");
			buffer.append(contributor.getClass().getSimpleName());
		}

		return buffer.toString();
	}

	/*
	 * Methods for executing contributors
	 */

	public boolean executeContributorsAddress(XDI3Segment[] contributorChainXris, XDI3Segment relativeTargetAddress, Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		// find an address with contributors

		XDI3Segment relativeContextNodeXri = relativeTargetAddress;

		List<ContributorMatch> contributorMatches = new ArrayList<ContributorMatch> ();
		contributorMatches.addAll(this.findHigherContributorMatches(relativeContextNodeXri));
		contributorMatches.addAll(this.findLowerContributorMatches(relativeContextNodeXri));
		if (contributorMatches.size() == 0) return false;

		if (log.isDebugEnabled()) log.debug("For relative target address: " + relativeTargetAddress + " found contributor XRIs: " + contributorMatches);

		for (ContributorMatch contributorMatch : contributorMatches) {

			XDI3Segment nextRelativeTargetAddress = XDI3Util.reduceXri(relativeTargetAddress, contributorMatch, false, true);
			XDI3Segment nextRelativeContextNodeXri = nextRelativeTargetAddress;

			XDI3Segment[] nextContributorChainXris = Arrays.copyOf(contributorChainXris, contributorChainXris.length + 1);
			if (nextRelativeContextNodeXri == null) nextContributorChainXris[nextContributorChainXris.length - 1] = contributorMatch;
			else if (nextRelativeContextNodeXri.equals(XDIConstants.XRI_S_ROOT)) nextContributorChainXris[nextContributorChainXris.length - 1] = relativeContextNodeXri;
			else nextContributorChainXris[nextContributorChainXris.length - 1] = XDI3Util.parentXri(relativeContextNodeXri, - nextRelativeContextNodeXri.getNumSubSegments());

			XDI3Segment nextContributorChainXri = XDI3Util.concatXris(nextContributorChainXris);

			if (log.isDebugEnabled()) log.debug("Next contributor chain XRIs: " + Arrays.asList(nextContributorChainXris) + ", next contributor chain XRI: " + nextContributorChainXri + ", next relative target address: " + nextRelativeTargetAddress);

			// execute the contributors

			for (Iterator<Contributor> contributors = this.get(contributorMatch).iterator(); contributors.hasNext(); ) {

				Contributor contributor = contributors.next();

				if (log.isDebugEnabled()) log.debug("Executing contributor " + contributor.getClass().getSimpleName() + " (address).");

				try {

					executionContext.pushContributor(contributor, "Contributor: address");

					// execute sub-contributors (address)

					if (contributor.getContributors().executeContributorsAddress(nextContributorChainXris, nextRelativeTargetAddress, operation, operationMessageResult, executionContext)) {

						return true;
					}

					// execute contributor (address)

					MessageResult tempMessageResult = new MessageResult();

					boolean handled = contributor.executeOnAddress(nextContributorChainXris, nextContributorChainXri, nextRelativeTargetAddress, operation, tempMessageResult, executionContext);

					XDI3Segment tempContextNodeXri = XDI3Util.expandXri(nextRelativeContextNodeXri, nextContributorChainXri);
					ContextNode tempContextNode = tempMessageResult.getGraph().getDeepContextNode(tempContextNodeXri);

					if (tempContextNode != null) CopyUtil.copyContextNode(tempContextNode, operationMessageResult.getGraph(), null);

					if (handled) {

						if (log.isDebugEnabled()) log.debug("Address has been fully handled by contributor " + contributor.getClass().getSimpleName() + ".");
						return true;
					}
				} catch (Exception ex) {

					throw executionContext.processException(ex);
				} finally {

					executionContext.popContributor();
				}
			}
		}

		// done

		return false;
	}

	public boolean executeContributorsStatement(XDI3Segment contributorChainXris[], XDI3Statement relativeTargetStatement, Operation operation, MessageResult operationMessageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		// find an address with contributors

		XDI3Segment relativeContextNodeXri = relativeTargetStatement.getContextNodeXri();

		List<XDI3Segment> contributorXris = new ArrayList<XDI3Segment> ();
		contributorXris.addAll(this.findHigherContributorMatches(relativeContextNodeXri));
		contributorXris.addAll(this.findLowerContributorMatches(relativeContextNodeXri));
		if (contributorXris.size() == 0) return false;

		if (log.isDebugEnabled()) log.debug("For relative target statement: " + relativeTargetStatement + " found contributor XRIs: " + contributorXris);

		for (XDI3Segment contributorXri : contributorXris) {

			XDI3Statement nextRelativeTargetStatement = StatementUtil.reduceStatement(relativeTargetStatement, contributorXri, false, true);
			XDI3Segment nextRelativeContextNodeXri = nextRelativeTargetStatement.getContextNodeXri();

			XDI3Segment[] nextContributorChainXris = Arrays.copyOf(contributorChainXris, contributorChainXris.length + 1);
			if (nextRelativeContextNodeXri == null) nextContributorChainXris[nextContributorChainXris.length - 1] = contributorXri;
			else if (nextRelativeContextNodeXri.equals(XDIConstants.XRI_S_ROOT)) nextContributorChainXris[nextContributorChainXris.length - 1] = relativeContextNodeXri;
			else nextContributorChainXris[nextContributorChainXris.length - 1] = XDI3Util.parentXri(relativeContextNodeXri, - nextRelativeContextNodeXri.getNumSubSegments());

			XDI3Segment nextContributorChainXri = XDI3Util.concatXris(nextContributorChainXris);

			if (log.isDebugEnabled()) log.debug("Next contributor chain XRIs: " + Arrays.asList(nextContributorChainXris) + ", next contributor chain XRI: " + nextContributorChainXri + ", next relative target statement: " + nextRelativeTargetStatement);

			// execute the contributors

			for (Iterator<Contributor> contributors = this.get(contributorXri).iterator(); contributors.hasNext(); ) {

				Contributor contributor = contributors.next();

				if (log.isDebugEnabled()) log.debug("Executing contributor " + contributor.getClass().getSimpleName() + " (statement).");

				try {

					executionContext.pushContributor(contributor, "Contributor: statement");

					// execute sub-contributors (statement)

					if (contributor.getContributors().executeContributorsStatement(nextContributorChainXris, nextRelativeTargetStatement, operation, operationMessageResult, executionContext)) {

						return true;
					}

					// execute contributor (statement)

					MessageResult tempMessageResult = new MessageResult();

					boolean handled = contributor.executeOnStatement(nextContributorChainXris, nextContributorChainXri, nextRelativeTargetStatement, operation, operationMessageResult, executionContext);

					XDI3Segment tempContextNodeXri = XDI3Util.expandXri(nextRelativeContextNodeXri, nextContributorChainXri);
					ContextNode tempContextNode = tempMessageResult.getGraph().getDeepContextNode(tempContextNodeXri);

					if (tempContextNode != null) CopyUtil.copyContextNode(tempContextNode, operationMessageResult.getGraph(), null);

					if (handled) {

						if (log.isDebugEnabled()) log.debug("Statement has been fully handled by contributor " + contributor.getClass().getSimpleName() + ".");
						return true;
					}
				} catch (Exception ex) {

					throw executionContext.processException(ex);
				} finally {

					executionContext.popContributor();
				}
			}
		}

		// done

		return false;
	}

	/*
	 * Methods for finding contributors
	 */

	/*	public XDI3Segment[] findMatchingContributorXri(XDI3Segment contextNodeXri) {

		if (contextNodeXri == null) throw new NullPointerException();
		if (this.isEmpty()) return new XDI3Segment[0];

		XDI3Segment matchingContributorXri = this.containsKey(contextNodeXri) ? contextNodeXri : null;

		if (matchingContributorXri == null) {

			if (log.isDebugEnabled()) log.debug("Finding matching contributor XRI for " + contextNodeXri + ": No match.");
		} else {

			if (log.isDebugEnabled()) log.debug("Finding matching contributor XRI for " + contextNodeXri + ": Match at " + matchingContributorXri);
		}

		return matchingContributorXri != null ? new XDI3Segment[] { matchingContributorXri } : new XDI3Segment[0];
	}*/

	public List<ContributorMatch> findHigherContributorMatches(XDI3Segment contextNodeXri) {

		if (this.isEmpty()) return new ArrayList<ContributorMatch> ();

		if (contextNodeXri == null) return new ArrayList<ContributorMatch> ();

		XDI3Segment higherContributorXri = null;
		List<ContributorMatch> higherContributorMatches = new ArrayList<ContributorMatch> ();

		for (Map.Entry<XDI3Segment, List<Contributor>> contributorEntry : this.entrySet()) {

			XDI3Segment contributorXri = XDI3Util.startsWith(contextNodeXri, contributorEntry.getKey(), false, true);

			if (contributorXri != null) {

				if (higherContributorXri == null || contributorXri.getNumSubSegments() > higherContributorXri.getNumSubSegments()) {

					higherContributorXri = contributorXri;
				}
			}
		}

		if (higherContributorMatches.isEmpty()) {

			if (log.isDebugEnabled()) log.debug("Finding higher contributors for " + contextNodeXri + ": No matches.");
		} else {

			if (log.isDebugEnabled()) log.debug("Finding higher contributors for " + contextNodeXri + ": Matches at " + higherContributorMatches);
		}

		return higherContributorMatches;
	}

	public List<ContributorMatch> findLowerContributorMatches(XDI3Segment contextNodeXri) {

		if (this.isEmpty()) return new ArrayList<ContributorMatch> ();

		if (contextNodeXri == null) contextNodeXri = XDIConstants.XRI_S_ROOT;

		List<XDI3Segment> lowerContributorXris = new ArrayList<XDI3Segment> ();

		if (XDIConstants.XRI_S_ROOT.equals(contextNodeXri)) {

			lowerContributorXris.addAll(this.keySet());
		} else {

			for (XDI3Segment contributorXri : this.keySet()) {

				if (contributorXri.equals(contextNodeXri)) continue;

				if (XDI3Util.startsWith(contributorXri, contextNodeXri, true, false)) {

					lowerContributorXris.add(contributorXri);
				}
			}
		}

		if (lowerContributorXris.isEmpty()) {

			if (log.isDebugEnabled()) log.debug("Finding lower contributor XRI for " + contextNodeXri + ": No matches.");
		} else {

			if (log.isDebugEnabled()) log.debug("Finding lower contributor XRI for " + contextNodeXri + ": Matches at " + lowerContributorXris);
		}

		return lowerContributorXris;
	}

	/*
	 * Prototype
	 */

	@SuppressWarnings("unchecked")
	@Override
	public ContributorMap instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// create new contributor map

		ContributorMap contributorMap = new ContributorMap();

		// add contributors

		for (Map.Entry<XDI3Segment, List<Contributor>> entry : this.entrySet()) {

			XDI3Segment contributorXri = entry.getKey();
			List<Contributor> contributors = entry.getValue();

			for (Contributor contributor : contributors) {

				if (! (contributor instanceof Prototype<?>)) {

					throw new Xdi2MessagingException("Cannot use contributor " + contributor.getClass().getSimpleName() + " as prototype.", null, null);
				}

				try {

					Prototype<? extends Contributor> contributorPrototype = (Prototype<? extends Contributor>) contributor;
					Contributor prototypedContributor = prototypingContext.instanceFor(contributorPrototype);

					contributorMap.addContributor(contributorXri, prototypedContributor);
				} catch (Xdi2MessagingException ex) {

					throw new Xdi2MessagingException("Cannot instantiate interceptor for prototype " + contributor.getClass().getSimpleName() + ": " + ex.getMessage(), ex, null);
				}
			}
		}

		// done

		return contributorMap;
	}

	/*
	 * Helper classes
	 */

	private static class ContributorMatch {

		private XDI3Segment contributorXri;
		private Contributor contributor;

		public ContributorMatch(XDI3Segment contributorXri, Contributor contributor) {

			this.contributorXri = contributorXri;
			this.contributor = contributor;
		}

		public XDI3Segment getContributorXri() {

			return this.contributorXri;
		}

		public Contributor getContributor() {

			return this.contributor;
		}

		@Override
		public String toString() {

			return this.contributorXri;
		}
	}
}
