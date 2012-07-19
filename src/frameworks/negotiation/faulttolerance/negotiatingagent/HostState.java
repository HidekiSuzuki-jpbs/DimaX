package frameworks.negotiation.faulttolerance.negotiatingagent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import dima.basicagentcomponents.AgentIdentifier;
import dima.introspectionbasedagents.services.core.information.ObservationService.Information;
import dima.introspectionbasedagents.services.core.opinion.OpinionService.Opinion;
import dima.introspectionbasedagents.services.modules.aggregator.AbstractCompensativeAggregation;
import dima.introspectionbasedagents.services.modules.aggregator.LightAverageDoubleAggregation;
import dima.introspectionbasedagents.services.modules.aggregator.LightWeightedAverageDoubleAggregation;
import frameworks.negotiation.faulttolerance.experimentation.ReplicationExperimentationParameters;
import frameworks.negotiation.negotiationframework.contracts.ResourceIdentifier;
import frameworks.negotiation.negotiationframework.rationality.AgentState;
import frameworks.negotiation.negotiationframework.rationality.SimpleAgentState;

public class HostState extends SimpleAgentState {
	private static final long serialVersionUID = 4107771452086657790L;

	/*
	 * Fields  (all must stay final for clone validity)
	 */

	private  final Set<AgentIdentifier> myReplicatedAgents;

	private final Double procChargeMax;
	private final Double procCurrentCharge;

	private final Double memChargeMax;
	private final Double memCurrentCharge;

	private final double lambda;
	private final boolean faulty;

	// Take all fields
	public HostState(
			final ResourceIdentifier myAgent,
			final double hostMaxProc,
			final double hostMaxMem,
			final double lambda,
			final int stateNumber) {
		this(myAgent,
				new HashSet<AgentIdentifier>(), 
				lambda,
				hostMaxProc, 0., 
				hostMaxMem, 0.,
				false,
				stateNumber);
	}

	public HostState(
			final ResourceIdentifier myAgent,
			final double hostMaxProc,
			final double hostMaxMem,
			final double lambda) {
		this(myAgent,
				new HashSet<AgentIdentifier>(), 
				lambda,
				hostMaxProc, 0., 
				hostMaxMem, 0.,
				false,
				-1);
	}


	public HostState allocate(final ReplicaState newRep){
		final HashSet<AgentIdentifier> rep =new HashSet<AgentIdentifier>(this.myReplicatedAgents);
		Double procCurrentCharge, memCurrentCharge;

		if (rep.contains(newRep.getMyAgentIdentifier())) {
			rep.remove(newRep.getMyAgentIdentifier());
			procCurrentCharge=this.procCurrentCharge-newRep.getMyProcCharge();
			memCurrentCharge=this.memCurrentCharge-newRep.getMyMemCharge();
		} else {
			rep.add(newRep.getMyAgentIdentifier());
			procCurrentCharge=this.procCurrentCharge+newRep.getMyProcCharge();
			memCurrentCharge=this.memCurrentCharge+newRep.getMyMemCharge();
		}

		return new HostState(
				this.getMyAgentIdentifier(),
				rep,
				this.getLambda(),
				this.getProcChargeMax(),
				procCurrentCharge,
				this.getMemChargeMax(),
				memCurrentCharge,
				this.isFaulty(),
				this.getStateCounter()+1);
	}

	// private universal constructor
	HostState(
			final ResourceIdentifier myAgent,
			final Set<AgentIdentifier> myReplicatedAgents,
			final double lambda, 
			final Double procChargeMax,
			final Double procCurrentCharge, 
			final Double memChargeMax,
			final Double memCurrentCharge, 
			final boolean faulty,// final Long creationTime,
			final int stateNumber) {
		super(myAgent, stateNumber);
		this.myReplicatedAgents = myReplicatedAgents;
		this.procChargeMax = procChargeMax;
		this.procCurrentCharge = procCurrentCharge;
		this.memChargeMax = memChargeMax;
		this.memCurrentCharge = memCurrentCharge;
		this.lambda = lambda;
		this.faulty = faulty;
	}

	//
	// Accessors
	//

	//pas propre!!! camarche uniquement pcq le h ne change pas de charge
	//	public boolean update(ReplicaState h){
	//		if (myReplicatedAgents.contains(h)){
	//			//remove previous h :
	//			myReplicatedAgents.remove(h);
	//			//adding new h
	//			return myReplicatedAgents.add(h);
	//			//			return true;
	//		} else
	//			throw new RuntimeException();
	//	}
	@Override
	public ResourceIdentifier getMyAgentIdentifier() {
		return (ResourceIdentifier) super.getMyAgentIdentifier();
	}

	public Double getMyCharge() {
		if (ReplicationExperimentationParameters.multiDim) {
			return Math.max(this.getCurrentMemCharge() / this.getMemChargeMax(),
					this.getCurrentProcCharge() / this.getProcChargeMax());
		} else {
			return this.getCurrentMemCharge() / this.getMemChargeMax();
		}
	}

	public boolean ImSurcharged() {
		return this.getMyCharge() > 1.;
	}


	@Override
	public Collection<AgentIdentifier> getMyResourceIdentifiers(){
		return  this.myReplicatedAgents;
	}

	@Override
	public Class<? extends AgentState> getMyResourcesClass() {
		return ReplicaState.class;
	}

	public boolean Ihost(final AgentIdentifier id){
		return this.getMyResourceIdentifiers().contains(id);
	}

	@Override
	public boolean isValid() {
		return !this.ImSurcharged();
	}

	/*
	 *
	 */

	Double getCurrentProcCharge() {
		return this.procCurrentCharge;
	}

	Double getCurrentMemCharge() {
		return this.memCurrentCharge;
	}

	public Double getProcChargeMax() {
		return this.procChargeMax;
	}

	public Double getMemChargeMax() {
		return this.memChargeMax;
	}

	public double getLambda() {
		return this.lambda;
	}

	/*
	 *
	 */

	public boolean isFaulty() {
		return this.faulty;
	}

	//	public void setFaulty(final boolean faulty) {
	//		this.faulty = faulty;
	//	}

	/*
	 *
	 */

	//	@Override
	//	public boolean setLost(final ResourceIdentifier h, final boolean isLost) {
	//		if (h.equals(this.getMyAgentIdentifier())) {
	//			this.setFaulty(isLost);
	//		} else {
	//			// Do nothing
	//		}
	//		return false;
	//	}

	//
	// Opinion Handling
	//

	//	@Override
	//	public int compareTo(final Information o) {
	//		if (o instanceof HostState) {
	//			final HostState e = (HostState) o;
	//			return this.getMyCharge().compareTo(e.getMyCharge());
	//		} else
	//			throw new RuntimeException("melange d'infos!!!"+this+" "+o);
	//	}

	//
	// Primitives
	//

	@Override
	public HostState clone(){
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof HostState ) {
			final HostState that = (HostState) o;
			return that.getMyAgentIdentifier().equals(
					this.getMyAgentIdentifier())&&this.getStateCounter()==that.getStateCounter();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode(){
		return this.getMyAgentIdentifier().hashCode();
	}

	@Override
	public String toString() {
		return "\nHOST="+ this.getMyAgentIdentifier()
				+ "\n --> current charge % = "+100*this.getMyCharge()
				+ "\n --> charge : "+ this.getCurrentProcCharge()+", "+this.getCurrentMemCharge()
				+ "\n --> capacity : "+ this.getProcChargeMax()+", "+this.getMemChargeMax()
				+ "\n --> lambda : "+this.lambda+
				"\n --> agents : " + this.getMyResourceIdentifiers()
				+ "\n --> faulty? : " + this.isFaulty()
				+"\n --> creation time : "+this.getCreationTime()+"#"+this.getStateCounter()
				+"\n valid ? : "+this.isValid();
	}
}