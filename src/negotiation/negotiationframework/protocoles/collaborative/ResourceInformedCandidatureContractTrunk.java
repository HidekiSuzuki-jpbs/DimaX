package negotiation.negotiationframework.protocoles.collaborative;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

import negotiation.negotiationframework.SimpleNegotiatingAgent;
import negotiation.negotiationframework.contracts.AbstractActionSpecif;
import negotiation.negotiationframework.contracts.ContractTrunk;
import negotiation.negotiationframework.contracts.InformedCandidature;
import negotiation.negotiationframework.contracts.MatchingCandidature;
import negotiation.negotiationframework.contracts.ReallocationContract;
import negotiation.negotiationframework.contracts.UnknownContractException;
import negotiation.negotiationframework.protocoles.AbstractCommunicationProtocol;
import negotiation.negotiationframework.rationality.AgentState;
import dimaxx.tools.mappedcollections.HashedHashSet;
import dimaxx.tools.mappedcollections.HashedTreeSet;

public class ResourceInformedCandidatureContractTrunk<
Contract extends MatchingCandidature>
extends ContractTrunk<InformedCandidature<Contract>>{
	private static final long serialVersionUID = -5058077493662331641L;

	/**
	 *
	 */

	//	private final ContractTrunk<ReallocationContract<Contract, ActionSpec>> myLocalOptimisations;
	HashedTreeSet<InformedCandidature<Contract>, ReallocationContract<Contract>> upgradingContracts;
	final Collection<InformedCandidature<Contract>> toCancel=
			new ArrayList<InformedCandidature<Contract>>();


	/*
	 *
	 */

	public void setMyAgent(final SimpleNegotiatingAgent<?, InformedCandidature<Contract>> agent){		
		super.setMyAgent(agent);
		upgradingContracts=
				new HashedTreeSet<InformedCandidature<Contract>, ReallocationContract<Contract>>(
						((InformedCandidatureRationality<?, Contract>) this.getMyAgent().
								getMyCore()).getReferenceAllocationComparator());
	}

	//
	// Methods
	//


	@Override
	public Collection<InformedCandidature<Contract>> getLockedContracts(){
		Collection<InformedCandidature<Contract>>  lock = new ArrayList<InformedCandidature<Contract>>(upgradingContracts.keySet());
		lock.addAll(toCancel);
		return lock;
	}

	public Collection<InformedCandidature<Contract>> getContractToCancel() {
		return toCancel;
	}

	public void addReallocContract(final ReallocationContract<Contract> realloc){
		assert this.containsAllKey(realloc.getIdentifiers()):this+"\n ---> "+realloc;
		for (final Contract c : realloc) {
			try {
				this.upgradingContracts.add(this.getContract(c.getIdentifier()),realloc);
			} catch (final UnknownContractException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 *
	 */

	public ReallocationContract<Contract> getBestReallocationContract(
			final InformedCandidature<Contract> c){
		if (this.upgradingContracts.get(c).isEmpty()) {

			return null;
		} else {
			return this.upgradingContracts.get(c).last();
		}
	}

	public ReallocationContract<Contract> getBestRequestableReallocationContract() {
		ReallocationContract<Contract> finalValue = null;
		Comparator<ReallocationContract<Contract>> myComp = 
				((InformedCandidatureRationality<?, Contract>) this.getMyAgent().
						getMyCore()).getReferenceAllocationComparator();
		for (final InformedCandidature<Contract> key : this.upgradingContracts.keySet()){
			Iterator<ReallocationContract<Contract>> itValue = this.upgradingContracts.get(key).descendingIterator();
			while (itValue.hasNext()){
				ReallocationContract<Contract> sol = itValue.next();
				if (this.isRequestable(sol)){
					if (finalValue==null)
						finalValue=sol;
					else {
						finalValue = myComp.compare(sol,finalValue)>0?sol:finalValue;
					}
					break;
				}
			}
		}
		return finalValue;
	}




	public Collection<ReallocationContract<Contract>> getReallocationContracts(){
		return this.upgradingContracts.getAllValuesUnsorted();
	}

	public boolean hasReallocationContracts(){
		return !this.upgradingContracts.isEmpty();
	}

	//
	// Primitive
	//

	private boolean isRequestable(final ReallocationContract<Contract> r){
		assert this.containsAllKey(r.getIdentifiers());
		for (final Contract c : r){
			assert this.getAllContracts().contains(c);
			try {
				if (!c.isMatchingCreation()) {
					if (!this.isRequestable(this.getContract(c.getIdentifier()))) {
						//						logMonologue(r + " is not requestable !! =( because of "+c, AbstractCommunicationProtocol.log_selectionStep);
						return false;
					}
				}
			} catch (final UnknownContractException e) {
				throw new RuntimeException();
			}
		}
		this.logMonologue("CONTRACT TRUNK say \n"+r + "\n ----------------------------------- is requestable yoooouhouuu!! =)", AbstractCommunicationProtocol.log_selectionStep);
		return true;
	}

	//
	// Overrided
	//


	@Override
	public void remove(final  InformedCandidature<Contract> c) {
		assert verifyIntegrity("\n\\\\ bb "+c+"\\\\ \n"+upgradingContracts.containsKey(c));
		
		super.remove(c);
		toCancel.remove(c);
		final Collection<ReallocationContract<Contract>> realloctoRemove = this.upgradingContracts.remove(c);
		
		//removing the other occurence of c reallocation contracts
		Collection<InformedCandidature<Contract>> concernedKeys = new HashSet<InformedCandidature<Contract>>();
		for (ReallocationContract<Contract> realloc : realloctoRemove){
			concernedKeys.addAll(this.upgradingContracts.removeAvalue(realloc));
		}		
		assert !upgradingContracts.containsKey(c);
		
		assert verifyIntegrity("\n\\\\ bb "+c+"\\\\ \n"+upgradingContracts.containsKey(c));
		
		//adding lost keys to cancel
		concernedKeys.remove(c);
		for (InformedCandidature<Contract> k : concernedKeys){
			if (k.getInitiator().equals(getMyAgentIdentifier()) && !upgradingContracts.containsKey(k)){
				assert !k.isMatchingCreation();
				toCancel.add(k);
			}
		}			
		assert !toCancel.contains(c);
	}

	@Override
	public String toString(){
		return super.toString()+"\n current upgrading contract are : \n "+this.upgradingContracts;
	}

	//
	// Assert
	//



	private boolean verifyIntegrity(String contextError){	
		Collection<ReallocationContract<Contract>> Allrealloc = this.upgradingContracts.getAllValuesUnsorted();
		//all individual contract of realloc exist as key in the base
		for (ReallocationContract<Contract> r : Allrealloc){
			for (Contract c : r)
				assert upgradingContracts.containsKey(c):toCancel.contains(c)+"\n\\\\ aa "+c+"\\\\ \n"+contextError;
		}
		//all realloc contract are mapped to their individual contracts
		for (InformedCandidature<Contract> c : upgradingContracts.keySet()){
			Collection<ReallocationContract<Contract>> notReallocOfC = new ArrayList<ReallocationContract<Contract>>(Allrealloc);
			notReallocOfC.removeAll(upgradingContracts.get(c));
			for (ReallocationContract<Contract> r : notReallocOfC){
				assert !notReallocOfC.contains(c):c+" "+r+" \n"+contextError;
			}
			
		}

		return true;
	}
}
//super.remove(c);
//toCancel.remove(c);
//final Collection<ReallocationContract<Contract>> toRemove =
//		new ArrayList<ReallocationContract<Contract>>();
//toRemove.addAll(this.upgradingContracts.get(c));
//for (final ReallocationContract<Contract> realloc : toRemove) {
//	Collection<InformedCandidature<Contract>> concernedKeys =
//			this.upgradingContracts.removeAvalue(realloc);
//	//adding lost keys to cancel
//	for (InformedCandidature<Contract> k : concernedKeys){
//		if (k.getInitiator().equals(getMyAgentIdentifier()) && !upgradingContracts.containsKey(k)){
//			assert !k.isMatchingCreation();
//			toCancel.add(k);
//		}
//	}
//}
//		final LinkedList<ReallocationContract<Contract, ActionSpec>> upCont =
//				new LinkedList<ReallocationContract<Contract, ActionSpec>>(this.upgradingContracts.get(c));
//		if (upCont.isEmpty()) {
//			return null;
//		} else {
//			return Collections.max(upCont,pref);
//		}
//	}
//	@Deprecated //non optimiser
//	public ReallocationContract<Contract, ActionSpec> getBestRequestableReallocationContract(
//			final Comparator<Collection<Contract>> pref){
//		final LinkedList<ReallocationContract<Contract, ActionSpec>> upCont =
//				new LinkedList<ReallocationContract<Contract, ActionSpec>>(this.upgradingContracts.getAllValues());
//		Iterator<ReallocationContract<Contract, ActionSpec>> itUpCont = upCont.iterator();
//		while (itUpCont.hasNext()){
//			if (!this.isRequestable(itUpCont.next()))
//				itUpCont.remove();
//		}
//		try {
//			return Collections.max(upCont,pref);
//		}	catch (NoSuchElementException e){
//			return null;
//		}
//	}
//@Deprecated //non optimiser
//public ReallocationContract<Contract, ActionSpec> getBestRequestableReallocationContract(
//		final Comparator<Collection<Contract>> pref){
//	final LinkedList<ReallocationContract<Contract, ActionSpec>> upCont =
//			new LinkedList<ReallocationContract<Contract, ActionSpec>>(this.upgradingContracts.getAllValues());
//	Iterator<ReallocationContract<Contract, ActionSpec>> itUpCont = upCont.iterator();
//	while (itUpCont.hasNext()){
//		if (!this.isRequestable(itUpCont.next()))
//			itUpCont.remove();
//	}
//	try {
//		return Collections.max(upCont,pref);
//	}	catch (NoSuchElementException e){
//		return null;
//	}
//}

//		Collections.sort(upCont,pref);
//		while (!upCont.isEmpty()) {
//			if (this.isRequestable(upCont.getFirst())) {
//				return upCont.getFirst();
//			} else {
//				upCont.pop();
//			}
//		}
//		return null;

//@Override
//public void addContract(final InformedCandidature<Contract> c){
//	super.addContract(c);
//	if (getMyAgent().Iaccept(getMyAgent().getMyCurrentState(), c)){
////		addAcceptation(getMyAgentIdentifier(), c);
//		this.upgradingContracts.add(c,new ReallocationContract<Contract>(c.getInitiator(), c.getCandidature()));
//	}
//}


//
//@Override
//public void addRejection(final AgentIdentifier id,
//		final InformedCandidature<Contract> c) {
//	super.addRejection(id, c);
////	for (ReallocationContract<Contract> r : upgradingContracts.get(c)){
////		//			System.err.println("yooooooooooo111111111111111111111111111"+r.getIdentifiers());
////		upgradingContracts.removeAvalue(r);
////	}
//}


//public void addReallocContract(final ReallocationContract<Contract> realloc){
//	assert this.containsAllKey(realloc.getIdentifiers());
//	//		if (!this.myLocalOptimisations.contains(realloc.getIdentifier()))
//	this.myLocalOptimisations.addContract(realloc);
//	for (final Contract c : realloc)
//		try {
//			this.getContract(c.getIdentifier()).getPossibleContracts().add(realloc);
//		} catch (final UnknownContractException e) {
//			throw new RuntimeException(e);
//		}
//}
//	//
//	// Overrided
//	//
//
//	/*
//	 *
//	 */
//
//
//	@Override
//	public void addContract(final InformedCandidature<Contract> c) {
//
//	}
//
//
//
//
//
//
//
//
//
//
//
//
//
//	@Override
//	public void addContract(final InformedCandidature<Contract> c) {
//		for (final ReallocationContract<Contract> r : c.getPossibleContracts()){
//			this.myLocalOptimisations.addContract(r);
//		}
//		super.addContract(c);
//		for (final ReallocationContract<Contract> r : c.getPossibleContracts()){
//			for (final Contract c2 : r)
//				if (!c2.equals(c))
//					try {
//						this.getContract(c2.getIdentifier()).getPossibleContracts().add(r);
//					} catch (final UnknownContractException e) {
//						throw new RuntimeException(e);
//					}
//		}
//	}
//
//	@Override
//	public void addAcceptation(final AgentIdentifier id,
//			final InformedCandidature<Contract> c) {
//		for (final ReallocationContract<Contract> r : c.getPossibleContracts()){
//			this.myLocalOptimisations.addAcceptation(id,r);
//		}
//		super.addAcceptation(id, c);
//	}
//
//	@Override
//	public void addRejection(final AgentIdentifier id,
//			final InformedCandidature<Contract> c) {
//		for (final ReallocationContract<Contract> r : c.getPossibleContracts()){
//			this.myLocalOptimisations.remove(r);
//		}
//		super.addRejection(id, c);
//	}
//
//	@Override
//	public void removeRejection(final AgentIdentifier id, final InformedCandidature<Contract> c) {
//		for (final ReallocationContract<Contract> r : c.getPossibleContracts()){
//			this.myLocalOptimisations.removeRejection(id,r);
//		}
//		super.removeRejection(id, c);
//	}
//
//	/*
//	 *
//	 */
//
//	@Override
//	public void remove(final  InformedCandidature<Contract> c) {
//		for (final ReallocationContract<Contract> r :this.myLocalOptimisations.getAllContracts()){
//			if (r.getAllParticipants().contains(c.getAgent()))
//				this.myLocalOptimisations.remove(r);
//		}
//
//		//		for (final ReallocationContract<Contract> r : c.getPossibleContracts()){
//		//			this.myLocalOptimisations.remove(r);
//		//		}
//		super.remove(c);
//	}
//
//
//	@Override
//	public void clear() {
//		super.clear();
//		this.myLocalOptimisations.clear();
//	}
//}



//public ReallocationContract<Contract> getBestRequestable(
//		final InformedCandidature<Contract> c,
//		final Comparator<Collection<Contract>> pref){
//	final Iterator<ReallocationContract<Contract>> itPossible =
//			c.getPossibleContracts().iterator();
//	assert itPossible.hasNext():"initialisé!!";
//	ReallocationContract<Contract> max = itPossible.next();
//	for (final ReallocationContract<Contract> r : c.getPossibleContracts()){
//		final ReallocationContract<Contract> neo = itPossible.next();
//		max = pref.compare(neo, max)>1?neo:max;
//	}
//	return max;
//}
//@Override
//public boolean isRequestable(final InformedCandidature<Contract> c) {
//	for (final ReallocationContract<Contract> realloc : c.getPossibleContracts())
//		if (this.myLocalOptimisations.isRequestable(realloc)){
//			return true;
//		}
//	return false;
//}

//@Override
//public boolean isAFailure(final InformedCandidature<Contract> c) {
//	if (super.isAFailure(c))
//		return true;
//	else {
//		boolean everyOneFailed = true;
//		for (final ReallocationContract<Contract> realloc : c.getPossibleContracts()){
//			if (!this.myLocalOptimisations.isAFailure(realloc))
//				everyOneFailed=false;
//		}
//		return everyOneFailed;
//	}
//}
//			for (final Contract c2 : r)
//				try {
//					this.getContract(c2.getIdentifier()).getPossibleContracts().remove(r);
//				} catch (UnknownContractException e) {
//					throw new RuntimeException(e.toString()+" \n realloc "+r+"\n base "+this);
//				}