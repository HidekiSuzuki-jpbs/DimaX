package frameworks.faulttolerance.experimentation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.omg.CORBA._PolicyStub;

import frameworks.faulttolerance.dcoprealloc.DcopAgent;
import frameworks.faulttolerance.dcoprealloc.DcopHost;


import dima.basicagentcomponents.AgentIdentifier;
import dima.basicagentcomponents.AgentName;
import dima.introspectionbasedagents.kernel.CompetentComponent;
import dima.introspectionbasedagents.modules.distribution.NormalLaw.DispersionSymbolicValue;
import dima.introspectionbasedagents.services.CompetenceException;
import dima.introspectionbasedagents.services.deployment.server.HostIdentifier;
import dima.introspectionbasedagents.services.launch.APIAgent.APILauncherModule;
import dima.introspectionbasedagents.services.loggingactivity.LogService;
import frameworks.experimentation.ExperimentationParameters;
import frameworks.experimentation.IfailedException;
import frameworks.experimentation.Laborantin;
import frameworks.experimentation.Laborantin.NotEnoughMachinesException;
import frameworks.faulttolerance.candidaturewithstatus.StatusHost;
import frameworks.faulttolerance.candidaturewithstatus.StatusReplica;
import frameworks.faulttolerance.centralizedsolver.CentralizedCoordinator;
import frameworks.faulttolerance.centralizedsolver.CentralizedHost;
import frameworks.faulttolerance.centralizedsolver.CentralizedReplica;
import frameworks.faulttolerance.collaborativecandidature.CollaborativeHost;
import frameworks.faulttolerance.collaborativecandidature.CollaborativeReplica;
import frameworks.faulttolerance.negotiatingagent.HostCore;
import frameworks.faulttolerance.negotiatingagent.HostState;
import frameworks.faulttolerance.negotiatingagent.ReplicaCore;
import frameworks.faulttolerance.negotiatingagent.ReplicaState;
import frameworks.faulttolerance.solver.JMetalSolver;
import frameworks.faulttolerance.solver.RessourceAllocationProblem;
import frameworks.faulttolerance.solver.SolverFactory;
import frameworks.faulttolerance.solver.jmetal.core.Solution;
import frameworks.negotiation.NegotiationParameters;
import frameworks.negotiation.NegotiationParameters.SelectionType;
import frameworks.negotiation.contracts.ResourceIdentifier;
import frameworks.negotiation.protocoles.AbstractCommunicationProtocol.SelectionCore;
import frameworks.negotiation.protocoles.dcopProtocol.DCOPLeaderProtocol;
import frameworks.negotiation.rationality.RationalAgent;
import frameworks.negotiation.rationality.SimpleRationalAgent;
import frameworks.negotiation.rationality.SocialChoiceFunction;
import frameworks.negotiation.rationality.SocialChoiceFunction.SocialChoiceType;
import frameworks.negotiation.selection.GreedySelectionModule;
import frameworks.negotiation.selection.OptimalSelectionModule;
import frameworks.negotiation.selection.SelectionModule;
import frameworks.negotiation.selection.SimpleSelectionCore;
import frameworks.negotiation.selection.GreedySelectionModule.GreedySelectionType;

public class ReplicationExperimentationParameters extends
ExperimentationParameters<ReplicationLaborantin> implements Comparable {
	private static final long serialVersionUID = -7191963637040889163L;

	//	final AgentIdentifier experimentatorId;
	public enum DCOPGraphType {Classical, Replication};

	/**
	 * Instance
	 */

	ReplicationInstanceGraph rig;

	/***
	 * Variables &  Constantes
	 */


	public int nbAgents;
	public int nbHosts;
	public String _usedProtocol;
	public SocialChoiceType _socialWelfare;

	public int agentAccessiblePerHost;
	public static int maxHostAccessibleParAgent;

	public  int simultaneousCandidature;
	public  int kSolver;
	public  Double	opinionDiffusionDegree;
	//	this.simultaneousCandidature = Math.min(nbHosts,this.simultaneousCandidature);
	//		simultaneousAcceptation = (int) Math.min(nbAgents,Math.max(simultaneousAcceptation,(int)((double)startingNbAgents)/((double)startingNbHosts)+1));


	public SelectionType _agentSelection;
	public SelectionType _hostSelection;

	public Double hostFaultProbabilityMean;
	public  DispersionSymbolicValue hostFaultProbabilityDispersion;

	public  Double agentLoadMean;
	public DispersionSymbolicValue agentLoadDispersion;

	public Double hostCapacityMean;
	public DispersionSymbolicValue hostCapacityDispersion;

	public Double agentCriticityMean;
	public DispersionSymbolicValue agentCriticityDispersion;

	//
	// Quantile
	//

	//	public static final long _statusObservationFrequency = 250;//10 * ReplicationExperimentationProtocol._timeToCollect;// (long)
	// (0.25*_contractExpirationTime);
	public Double alpha_low, alpha_high;


	//
	// System Dynamicity
	//

	public Boolean dynamicCriticity;
	public Boolean faultOccurs=false;

	/* FAULTS
	 *
	 * * lambda haut => weibull bas weibull bas => eventOccur haut lambda = prob
	 * de panne disp = 1 - lambda (useStaticDispo = true) disp = weibull
	 * (useStaticDispo = false)
	 *
	 *
	 * **
	 *
	 * k bas => eventOccur tot et pour tout le monde
	 */

	public enum DisponibilityComputationType {Static, Weibull, Poisson}
	public static final DisponibilityComputationType choosenType = DisponibilityComputationType.Static;//Poisson;//

	private static final Double host_maxSimultaneousFailure=20./100.;
	public static final long _host_maxFaultfrequency = 500;//10 * ReplicationExperimentationProtocol._timeToCollect;// 2*_simulationTime;//
	public static final long _timeScale = 10 * ReplicationExperimentationParameters._host_maxFaultfrequency;
	public static final double _lambdaRepair = 1;

	//	WEIBULL : OLD
	public static final double _kValue = 7;
	public static final double _kRepair = .001;
	public static final double _theta = 0;// _host_maxFaultfrequency;//0.2;


	/*
	 * Criticité
	 */

	public static final double _criticityMin = 0.1;
	public static final double _criticityVariationProba = 20. / 100.;// 20%
	public static final double _criticityVariationAmplitude = 30. / 100.;// 10%
	public static final long _criticity_update_frequency = 4*NegotiationParameters._timeToCollect;// (long)


	//
	// Constructor
	//


	ReplicationExperimentationParameters(
			final int nbAgents, final int nbHosts, 
			final int agentAccessiblePerHost,
			final Double hostFaultProbabilityMean,
			final DispersionSymbolicValue hostFaultProbabilityDispersion,
			final Double agentLoadMean,
			final DispersionSymbolicValue agentLoadDispersion,
			final Double hostCapacityMean,
			final DispersionSymbolicValue hostcapacityDispersion,
			final Double agentCriticityMean,
			final DispersionSymbolicValue agentCriticityDispersion,
			int simultaneousCandidature,
			int simultaneousAcceptation,
			Double	opinionDiffusionDegree,
			final String usedProtocol,
			final SocialChoiceType socialWelfare,
			final SelectionType agentSelection,
			final SelectionType hostSelection,
			Double alpha_low,
			Double alpha_high,
			final boolean dynamicCriticty,
			final boolean faultOccurs) {
		super(new AgentName("ziReplExp"),
				ReplicationExperimentationGenerator.getProtocolId()
				);
		this.nbAgents = nbAgents;
		this.nbHosts= nbHosts;
		this.agentAccessiblePerHost= agentAccessiblePerHost;
		//		assert this.agentAccessiblePerHost>0;
		this.hostFaultProbabilityMean = hostFaultProbabilityMean;
		this.hostFaultProbabilityDispersion=hostFaultProbabilityDispersion;
		this.agentLoadMean = agentLoadMean;
		this.agentLoadDispersion=agentLoadDispersion;
		this.agentCriticityMean=agentCriticityMean;
		this.agentCriticityDispersion=agentCriticityDispersion;
		this.hostCapacityMean=hostCapacityMean;//41.5;//0.30*nbAgents;//hostCapacityMean;
		this.hostCapacityDispersion=hostcapacityDispersion;
		this.simultaneousCandidature=simultaneousCandidature;
		this.kSolver=simultaneousAcceptation;
		this.opinionDiffusionDegree=opinionDiffusionDegree;
		this.alpha_low=alpha_low;
		this.alpha_high=alpha_high;
		this._usedProtocol = usedProtocol;
		this._socialWelfare=socialWelfare;
		this._agentSelection = agentSelection;
		this._hostSelection=hostSelection;
		this.dynamicCriticity = dynamicCriticty;
	}

	//
	// Accessors
	//

	public long getRandomSeed() {
		return randSeed;
	}

	//	public String setAgentSelection(final String agentSelection){
	//		return this._usedProtocol.equals(NegotiationParameters.key4mirrorProto)?NegotiationParameters.key4greedySelect:agentSelection;
	//	}

	public Collection<AgentIdentifier> getReplicasIdentifier() {
		return this.rig.getAgentsIdentifier();
	}

	public Collection<ResourceIdentifier> getHostsIdentifier() {
		return this.rig.getHostsIdentifier();
	}
	//	//	// final NormalLaw numberOfKnownHosts = new NormalLaw(this.p.kAccessible,
	//	//	// 0);
	//	private int getNumberOfKnownHosts() {
	//		// return numberOfKnownHosts.nextValue();
	//		return kAccessible;
	//	}
	//
	//	public void setkAccessible(final double k) {
	//		this.agentAccessiblePerHost =(int) (k * this.nbAgents);
	//	}
	//
	//	public Double getRealkAccessible() {
	//		return (double)this.agentAccessiblePerHost/
	//				(double)this.nbAgents;
	//	}

	//	public void setMaxSimultFailure(final Double host_maxSimultaneousFailurePercent){
	//		this.host_maxSimultaneousFailure = this.agentAccessiblePerHost*host_maxSimultaneousFailurePercent;
	//
	//	}
	//
	//	public Double getRealMaxSimultFailure(){
	//		return this.host_maxSimultaneousFailure/this.agentAccessiblePerHost;
	//	}
	public Double getMaxSimultFailure(){
		return this.agentAccessiblePerHost*host_maxSimultaneousFailure;
	}

	//
	//
	//
	//
	// Methods
	//

	@Override
	public final void initiateParameters() throws IfailedException{
		this.rig = new ReplicationInstanceGraph(_socialWelfare);

		rig.randomInitiaition(getSimulationName(), randSeed,
				nbAgents, nbHosts, 
				agentCriticityMean, agentCriticityDispersion, 
				agentLoadMean, agentLoadDispersion, hostCapacityMean, 
				hostCapacityDispersion, hostFaultProbabilityMean, hostFaultProbabilityDispersion, 
				maxHostAccessibleParAgent, agentAccessiblePerHost);

		//
		//		if (this.withOptimal){
		//			this.logMonologue("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& wtttttttfffffffffffff ", LogService.onBoth);
		//			ReplicationOptimalSolver ros=null;
		//			ros = new ReplicationOptimalSolver(this.getMyAgent());
		//			this.logMonologue("beggining optimal computation &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&", LogService.onBoth);
		//			ros.solve();
		//			this.logMonologue("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& ending optimal computation ", LogService.onBoth);
		//		}

		String initialisationStatus = "Neighborhoog... :\n";
		if (agentAccessiblePerHost==nbHosts) {
			initialisationStatus+="using a complete accesiblity graph";
		} else {
			for (final AgentIdentifier r : this.rig.getAgentsIdentifier()){
				initialisationStatus+=r+"  has acces to  "+this.rig.getAccessibleHosts(r)+"\n";
			}
		}
		initialisationStatus += "\n Initializing allocation... :\n";
		for (final HostState h : this.rig.getHostsStates()){
			initialisationStatus+=h.getMyAgentIdentifier()+"  has allocated  "+h.getMyResourceIdentifiers()+"\n";
		}
		this.logMonologue(initialisationStatus, LogService.onFile);
	}


	//
	// Protocol
	//





	/*
	 * Instanciation
	 */

	@Override
	protected Collection<RationalAgent> instanciateAgents()throws CompetenceException {
		//		System.out.println(this.getMyAgent()+" "+this.getMyAgent().
		//				myStatusObserver);
		//		assert !this._usedProtocol
		//		.equals(NegotiationParameters.key4CentralisedstatusProto) ||
		//		this.getMyAgent().
		//		myStatusObserver.iObserveStatus():
		//			this._usedProtocol
		//			.equals(NegotiationParameters.key4CentralisedstatusProto)+" "+this.getMyAgent().myStatusObserver.iObserveStatus();

		//		this.logMonologue("Initializing agents... ",LogService.onBoth);
		final Map<AgentIdentifier,RationalAgent> result = new HashMap<AgentIdentifier, RationalAgent>();

		/*
		 * Agent instanciation
		 */

		for (final AgentIdentifier replicaId : this.getReplicasIdentifier()) {

			final SimpleRationalAgent rep;
			if (this._usedProtocol
					.equals(NegotiationParameters.key4mirrorProto)) { //Collaborative

				rep = new CollaborativeReplica(
						replicaId,
						this.rig.getAgentState(replicaId),
						this._socialWelfare,
						this.simultaneousCandidature,
						this.dynamicCriticity);

			}else if (this._usedProtocol
					.equals(NegotiationParameters.key4CentralisedstatusProto)){
				rep = new StatusReplica(
						replicaId,
						this.rig.getAgentState(replicaId),
						this.getSelectionCore(this._agentSelection),
						this.simultaneousCandidature,
						this.dynamicCriticity,
						this.getMyAgentIdentifier(),
						alpha_low, alpha_high);
			}else  if (this._usedProtocol.equals(NegotiationParameters.key4statusProto)){ //Status

				rep = new StatusReplica(
						replicaId,
						this.rig.getAgentState(replicaId),
						this.getSelectionCore(this._agentSelection),
						this.simultaneousCandidature,
						this.dynamicCriticity,
						this.opinionDiffusionDegree.intValue(),
						alpha_low, alpha_high);

			}else if (this._usedProtocol
					.equals(NegotiationParameters.key4DcopProto)){

				rep = new DcopAgent(
						replicaId,
						this.rig.getAgentState(replicaId),
						getDcopKValue(this.kSolver),
						this.dynamicCriticity);

			}else if (this._usedProtocol
					.equals(NegotiationParameters.key4GeneticProto)){
				rep = new CentralizedReplica(
						replicaId,
						this.rig.getAgentState(replicaId),this.dynamicCriticity);

			} else {
				throw new RuntimeException("impossible : usedProtocol = "+this._usedProtocol);
			}

			//Ajout des acquaintances
			rep.getMyInformation().addAll(this.rig.getAccessibleHosts(replicaId));
			rep.setKnownResources(this.rig.getAccessibleHosts(replicaId));

			//gestion des état initiaux
			for (final AgentIdentifier host : rep.getMyCurrentState().getMyResourceIdentifiers()){
				if (rep.getMyCore().iMemorizeMyRessourceState()) {
					rep.getMyInformation().add(this.rig.getHostState((ResourceIdentifier)host));
				}
				if (rep.getMyCore().iObserveMyRessourceChanges()) {
					rep.addObserver(host,
							SimpleRationalAgent.stateChangementObservation);
				}
			}
			result.put(rep.getId(),rep);
			getMyAgent().myInformationService.add(rep.getMyCurrentState());
		}

		/*
		 * Host instanciation
		 */
		CentralizedCoordinator cc =null;
		for (final ResourceIdentifier hostId : this.getHostsIdentifier()) {

			final SimpleRationalAgent hostAg;
			if (this._usedProtocol
					.equals(NegotiationParameters.key4mirrorProto)) {
				hostAg = new CollaborativeHost(
						hostId,
						this.rig.getHostState(hostId),
						this._socialWelfare,
						this.kSolver,
						this.getSelectionModule(this._hostSelection),
						this.maxIndividualComputingTime);
			}else if (this._usedProtocol
					.equals(NegotiationParameters.key4CentralisedstatusProto)){ //Status
				hostAg = new StatusHost(
						hostId,
						this.rig.getHostState(hostId),
						this.getSelectionCore(this._hostSelection),
						this._socialWelfare,
						this.getMyAgentIdentifier(),
						alpha_low, alpha_high);

			} else if (this._usedProtocol
					.equals(NegotiationParameters.key4statusProto)) {
				hostAg = new StatusHost(
						hostId,
						this.rig.getHostState(hostId),
						this.getSelectionCore(this._hostSelection),
						this._socialWelfare,
						this.opinionDiffusionDegree.intValue(),
						alpha_low, alpha_high);
			}else if (this._usedProtocol
					.equals(NegotiationParameters.key4DcopProto)){

				hostAg = new DcopHost(
						hostId,
						this.rig.getHostState(hostId),
						this._socialWelfare,
						getDcopKValue(this.kSolver),SolverFactory.getGlobalSolver(this._socialWelfare),
						this.maxIndividualComputingTime);

			}else if (this._usedProtocol
					.equals(NegotiationParameters.key4GeneticProto)){

				JMetalSolver p = new JMetalSolver(_socialWelfare, true, true);
				p.setProblem(rig, new ArrayList<AgentIdentifier>());
				p.setTimeLimit((int) this._maxSimulationTime);

				if (cc==null){
					hostAg = new CentralizedCoordinator(
							hostId,
							this.rig.getHostState(hostId),p);
					cc=(CentralizedCoordinator) hostAg;

				} else {
					hostAg = new CentralizedHost(
							hostId,
							this.rig.getHostState(hostId),p);

					cc.register((CentralizedHost) hostAg);
				}
			}else {
				throw new RuntimeException("impossible : usedProtocol = "+this._usedProtocol);
			}

			//pas d'acquaintance pour les ressources
			hostAg.setKnownResources(rig.getAccessibleAgents(hostId));
			//gestion des état initiaux
			for (final AgentIdentifier ag : hostAg.getMyCurrentState().getMyResourceIdentifiers()){
				if ((hostAg.getMyCore()).iMemorizeMyRessourceState()) {
					hostAg.getMyInformation().add(this.rig.getAgentState(ag));
				}
				if ((hostAg.getMyCore()).iObserveMyRessourceChanges()) {
					hostAg.addObserver(ag,
							SimpleRationalAgent.stateChangementObservation);
				}
			}

			result.put(hostAg.getIdentifier(),hostAg);
			getMyAgent().myInformationService.add(hostAg.getMyCurrentState());
		}

		/*
		 *
		 */

		this.logMonologue("Initializing agents done!:\n" + this.getMyAgent().myInformationService.show(HostState.class) + this.getMyAgent().myInformationService.show(ReplicaState.class),LogService.onFile);
		return result.values();
	}

	int getDcopKValue(int k){
		return (int) Math.sqrt(4*k);
	}

	private SimpleSelectionCore getSelectionCore(final SelectionType selection){

		if (selection
				.equals(SelectionType.Greedy)) {
			return new SimpleSelectionCore(
					true, false, new GreedySelectionModule(GreedySelectionType.Greedy));
		} else if (selection
				.equals(SelectionType.RoolettWheel)) {
			return new SimpleSelectionCore(
					true, false, new GreedySelectionModule(GreedySelectionType.RooletteWheel));
		} else if (selection
				.equals(SelectionType.Random)) {
			return new SimpleSelectionCore(
					true, false, new GreedySelectionModule(GreedySelectionType.Random));
		} else if (selection
				.equals(SelectionType.Opt)) {
			return new SimpleSelectionCore(
					true, false, new OptimalSelectionModule(SolverFactory.getHostSolver(_socialWelfare), true, maxIndividualComputingTime));
		}else if (selection
				.equals(SelectionType.Better)) {
			return new SimpleSelectionCore(
					true, false, new OptimalSelectionModule(SolverFactory.getHostSolver(_socialWelfare), false, maxIndividualComputingTime));
		} else {
			throw new RuntimeException(
					"Static parameters est mal conf : selection = "+ selection);
		}
	}
	private SelectionModule getSelectionModule(final SelectionType selection){

		if (selection
				.equals(SelectionType.Greedy)) {
			return new GreedySelectionModule(GreedySelectionType.Greedy);
		} else if (selection
				.equals(SelectionType.RoolettWheel)) {
			return new GreedySelectionModule(GreedySelectionType.RooletteWheel);
		} else if (selection
				.equals(SelectionType.Opt)) {
			return new OptimalSelectionModule(SolverFactory.getHostSolver(_socialWelfare), true, this.maxIndividualComputingTime);
		}else if (selection
				.equals(SelectionType.Better)) {
			return new OptimalSelectionModule(SolverFactory.getHostSolver(_socialWelfare), false, this.maxIndividualComputingTime);
		} else {
			throw new RuntimeException(
					"Static parameters est mal conf : selection = "+ selection);
		}
	}

	@Override
	public boolean equals(final Object o){
		if (o instanceof ReplicationExperimentationParameters){
			final ReplicationExperimentationParameters that = (ReplicationExperimentationParameters) o;

			return super.equals(that) &&
					this.nbAgents==that.nbAgents &&
					this.nbHosts==that.nbHosts && 
					this.agentAccessiblePerHost==that.agentAccessiblePerHost &&
					this.hostFaultProbabilityMean.equals( that.hostFaultProbabilityMean) &&
					this.agentLoadMean.equals(that.agentLoadMean) &&
					this.agentLoadDispersion.equals(that.agentLoadDispersion) &&
					this.hostCapacityMean.equals(that.hostCapacityMean) &&
					this.hostCapacityDispersion.equals(that.hostCapacityDispersion) &&
					this.opinionDiffusionDegree.equals(that.opinionDiffusionDegree) &&
					this._agentSelection.equals(that._agentSelection) &&
					this._hostSelection.equals(that._hostSelection) &&
					this.alpha_low.equals(that.alpha_low) &&
					this.alpha_high.equals(that.alpha_high) &&
					this.simultaneousCandidature==that.simultaneousCandidature &&
					this.kSolver==that.kSolver &&
					this._usedProtocol.equals(that._usedProtocol) &&
					this._socialWelfare.equals(that._socialWelfare) &&
					this._agentSelection.equals(that._agentSelection) &&
					this._hostSelection.equals(that._hostSelection);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode(){
		return
				2*this.agentAccessiblePerHost
				+4*this.hostFaultProbabilityMean.hashCode()
				+8*this.agentLoadMean.hashCode()
				+16*this._usedProtocol.hashCode()
				+32*this._socialWelfare.hashCode()
				+64*this._agentSelection.hashCode()
				+128*this._hostSelection.hashCode()
				+256*this.hostFaultProbabilityDispersion.hashCode()
				+512*this.agentLoadDispersion.hashCode()
				+1024*this.agentCriticityMean.hashCode()
				+2048*this.agentCriticityDispersion.hashCode()
				+4096*this.dynamicCriticity.hashCode()
				+8192*this.host_maxSimultaneousFailure.hashCode()
				+16000*this.hostCapacityMean.hashCode()
				+32000*this.hostCapacityDispersion.hashCode()
				+64000*this.simultaneousCandidature
				+128000*this.kSolver
				+256000*this.opinionDiffusionDegree.intValue()
				+500000*this.alpha_low.intValue()
				+1000000*this.alpha_high.intValue();
	}

	@Override
	public ReplicationExperimentationParameters clone(){
		assert alpha_high!=0.;
		return new ReplicationExperimentationParameters(
				this.nbAgents,
				this.nbHosts,
				this.agentAccessiblePerHost,
				this.hostFaultProbabilityMean,
				this.hostFaultProbabilityDispersion,
				this.agentLoadMean,
				this.agentLoadDispersion,
				this.hostCapacityMean,
				this.hostCapacityDispersion,
				this.agentCriticityMean,
				this.agentCriticityDispersion,
				this.simultaneousCandidature,
				this.kSolver,
				this.opinionDiffusionDegree,
				this._usedProtocol,
				this._socialWelfare,
				this._agentSelection,
				this._hostSelection,
				this.alpha_low,
				this.alpha_high,
				this.dynamicCriticity,
				this.faultOccurs);

	}
	@Override
	public int compareTo(final Object o) {
		assert alpha_high!=0.;
		final ReplicationExperimentationParameters that = (ReplicationExperimentationParameters) o;
		if (this.nbAgents!=that.nbAgents){
			return (this.nbAgents-that.nbAgents);
		} else {
			if (!this._usedProtocol.equals(that._usedProtocol)){  
				if (this._usedProtocol.equals(NegotiationParameters.key4statusProto) || 
						that._usedProtocol.equals(NegotiationParameters.key4statusProto)){//en premier
					if (this._usedProtocol.equals(NegotiationParameters.key4statusProto))
						return -1;
					else
						return 1;

				} else  if (this._usedProtocol.equals(NegotiationParameters.key4mirrorProto) || 
						that._usedProtocol.equals(NegotiationParameters.key4mirrorProto)){//en deuxieme
					if (this._usedProtocol.equals(NegotiationParameters.key4mirrorProto))
						return -1;
					else
						return 1;

				} else  if (this._usedProtocol.equals(NegotiationParameters.key4GeneticProto) || 
						that._usedProtocol.equals(NegotiationParameters.key4GeneticProto)){//en troisieme
					if (this._usedProtocol.equals(NegotiationParameters.key4GeneticProto))
						return -1;
					else
						return 1;
				} else {
					throw new RuntimeException("impossible avec 4 protos");
				}
			} else {	
				if (this.kSolver!=that.kSolver){
					return this.kSolver-that.kSolver;
				} else {								
					if (!this._socialWelfare.equals(that._socialWelfare)){
						return this._socialWelfare.compareTo(that._socialWelfare);
					} else	{
						if (this.agentAccessiblePerHost!=that.agentAccessiblePerHost) {
							return this.agentAccessiblePerHost-that.agentAccessiblePerHost;
						} else {
							//					if (!this.alpha_low.equals(that.alpha_low) ) {//trie  les protos
							//						return this.alpha_low.compareTo(that.alpha_low);
							//					} else {
							//						if (!this.opinionDiffusionDegree.equals(that.opinionDiffusionDegree)){
							//							return this.opinionDiffusionDegree.compareTo(that.opinionDiffusionDegree);
							//						} else {

							if ((this.hostCapacityMean/(double)this.nbAgents)!=(that.hostCapacityMean/(double)that.nbAgents)){
								return new Double((this.hostCapacityMean/(double)this.nbAgents)).compareTo((that.hostCapacityMean/(double)that.nbAgents));
							} else {
								if (!this._hostSelection.equals(that._hostSelection)){
									return this._hostSelection.compareTo(that._hostSelection);
								} else {	//									assert this.equals(that):this+"\n-->"+that;
									return 0;
								}
							}
						}
						//							}
						//						}
					}
				}
			}
		}
	}


	//		final double fixedResources=
	//				(double)new ReplicationExperimentationGenerator().maxAgentNb/
	//				(double)new ReplicationExperimentationGenerator().maxHostNb;
	//		final boolean thisIsFixed=this.hostCapacityMean==fixedResources;
	//		final boolean thatIsFixed=that.hostCapacityMean==fixedResources;
	//		final double thisHostCapacityPercent=this.hostCapacityMean/this.nbAgents;
	//		final double thatHostCapacityPercent=that.hostCapacityMean/that.nbAgents;
	//
	//		if  (thisIsFixed && thatIsFixed) {
	//			return this.nbAgents-that.nbAgents;
	//		} else if (thisIsFixed && !thatIsFixed){
	//			return -1;
	//		} else if (thatIsFixed && !thisIsFixed){
	//			return 1;
	//		} else {//!thisIsFixed && !thatIsFixed
	//			if (thisHostCapacityPercent!=thatHostCapacityPercent){
	//				return (int) (thisHostCapacityPercent-thatHostCapacityPercent);
	//			} else {
	//				return this.nbAgents-that.nbAgents;
	//			}
	//		}

	@Override
	public boolean isValid() {
		//		return true;

		if (!parametresCoherents()){
			return false;
		}
		//		return true;
		//SOUHAITE
		//
		if (kSolver==ReplicationExperimentationGenerator._kDefault &&
				nbAgents==ReplicationExperimentationGenerator._AgentDefault &&
				_socialWelfare.equals(SocialChoiceType.Utility) &&
				_hostSelection.equals(SelectionType.RoolettWheel)){
			if (_usedProtocol.equals(NegotiationParameters.key4mirrorProto)){
				return true;
			} else if (_usedProtocol.equals(NegotiationParameters.key4statusProto)){
				if ((!alpha_low.equals(ReplicationExperimentationGenerator._alpha_lowDefault) 
						|| !alpha_high.equals(ReplicationExperimentationGenerator._alpha_highDefault))){
					return opinionDiffusionDegree.equals(
							ReplicationExperimentationGenerator.getValue(
									ReplicationExperimentationGenerator._kOpinionDefault,new Double(nbAgents))) &&
									_hostSelection.equals(SelectionType.RoolettWheel);
				} else {
					return true;
				}
			}
		}
		
		if (
				kSolver==ReplicationExperimentationGenerator._kDefault &&
						_socialWelfare.equals(SocialChoiceType.Utility)  &&
				_hostSelection.equals(SelectionType.RoolettWheel)){
			if (_usedProtocol.equals(NegotiationParameters.key4statusProto)){
				return alpha_low.equals(ReplicationExperimentationGenerator._alpha_lowDefault)
						&& alpha_high.equals(ReplicationExperimentationGenerator._alpha_highDefault)
						&& opinionDiffusionDegree.equals(ReplicationExperimentationGenerator.getValue(
								ReplicationExperimentationGenerator._kOpinionDefault,new Double(nbAgents)));
			} else {
				return alpha_high.equals(Double.NaN) && alpha_low.equals(Double.NaN) && this.opinionDiffusionDegree.equals(Double.NaN);
			}
		}



		if (nbAgents==ReplicationExperimentationGenerator._AgentDefault &&
				_hostSelection.equals(SelectionType.RoolettWheel) && 
				//				(alpha_low==Double.NaN && alpha_high==Double.NaN && opinionDiffusionDegree==Double.NaN) &&
				_socialWelfare.equals(SocialChoiceType.Utility) &&
				(_usedProtocol.equals(NegotiationParameters.key4mirrorProto) || _usedProtocol.equals(NegotiationParameters.key4DcopProto))){
			return true;
		}

		return false;
	}

	private boolean parametresCoherents() {
		//INCOHERENT

		assert alpha_high!=0.;
		//
		if (nbHosts*agentAccessiblePerHost<nbAgents || agentAccessiblePerHost<=0) {
			System.out.println("agentAccessiblePerHost not valid "+agentAccessiblePerHost+" "+nbHosts*agentAccessiblePerHost+" "+nbAgents);
			return false;
		} 

		if (this.kSolver>nbAgents)
			return false;

		if (!this._agentSelection.equals(SelectionType.Greedy)){
			//			System.out.println("_agentSelection0 not valid");
			return false;
		}
		//		if (_usedProtocol.equals(NegotiationParameters.key4mirrorProto) 
		//				|| _usedProtocol.equals(NegotiationParameters.key4DcopProto) 
		//				|| _usedProtocol.equals(NegotiationParameters.key4GeneticProto)){
		//			return false;
		//		}
		//
		if (_usedProtocol.equals(NegotiationParameters.key4statusProto)){
			if (alpha_high.equals(Double.NaN) || alpha_low.equals(Double.NaN) || this.opinionDiffusionDegree.equals(Double.NaN)){
				return false;
				//			} else if (this.kSolver!=ReplicationExperimentationGenerator._kDefault){
				//				return false;
			}
		} else {
			if (!alpha_high.equals(Double.NaN) || !alpha_low.equals(Double.NaN) || !this.opinionDiffusionDegree.equals(Double.NaN)){
				//				System.out.println("alpha1 et opinionDiffusionDegree1not valid");
				return false;
			}
		}
		return true;
	}

	@Override
	public Laborantin createLaborantin(final APILauncherModule api)
			throws CompetenceException, IfailedException,
			NotEnoughMachinesException {
		final ReplicationLaborantin l = new ReplicationLaborantin(this, api);
		this.setMyAgent(l);
		return l;
	}


	/*
	 *Distribution
	 */

	final double nbSimuPerMAchine = 1;
	@Override
	public Integer getMaxNumberOfAgent(final HostIdentifier id) {
		return new Integer((int) this.nbSimuPerMAchine*
				//				(nbAgents + nbHosts)+1);
				(new ReplicationExperimentationGenerator().maxAgentNb +new ReplicationExperimentationGenerator().maxHostNb)+1);
		//		return new Integer(10);new
	}



	//
	// Génération
	//

	ReplicationExperimentationGenerator reg = null;

	private void instanciateReg(){
		if (reg==null)
			reg = new ReplicationExperimentationGenerator();
	}

	@Override
	public LinkedList<ExperimentationParameters<ReplicationLaborantin>> generateSimulation() {
		instanciateReg();
		LinkedList<ExperimentationParameters<ReplicationLaborantin>> simulations = reg.generateSimulation();
		Collections.sort(simulations);
		simulations=getPart(simulations, ReplicationLaborantin.informativeParameterNumber, 4);
		return simulations;
	}
	private  <T>  LinkedList<T> getPart(List<T> objets, int partNumber, int numberOfPart){
		LinkedList<T> results = new LinkedList<T>();
		int numberOfPartElements = (int) objets.size()/numberOfPart;
		int startPoint = partNumber*numberOfPartElements;
		int finalPoint = Math.min(objets.size(), startPoint+numberOfPartElements);
		for (int i = startPoint; i < finalPoint; i++){
			results.add(objets.get(i));
		}
		return results;
	}
}



//		if (kSolver==ReplicationExperimentationGenerator._kDefault &&
//				nbAgents==ReplicationExperimentationGenerator._AgentDefault &&
//				_socialWelfare.equals(SocialChoiceType.Utility) &&
//				(
//						_usedProtocol.equals(NegotiationParameters.key4mirrorProto) ||
//						(
//								_usedProtocol.equals(NegotiationParameters.key4statusProto) &&
//								(
//										(alpha_low.equals(ReplicationExperimentationGenerator._alpha_lowDefault) 
//												&& alpha_high.equals(ReplicationExperimentationGenerator._alpha_highDefault)) ||
//												((!alpha_low.equals(ReplicationExperimentationGenerator._alpha_lowDefault) 
//														&& !alpha_high.equals(ReplicationExperimentationGenerator._alpha_highDefault)) &&
//														opinionDiffusionDegree.equals(
//																ReplicationExperimentationGenerator.getValue(
//																		ReplicationExperimentationGenerator._kOpinionDefault,new Double(nbAgents))) &&
//																		_hostSelection.equals(SelectionType.RoolettWheel)))
//								)
//						)
//				)
//		{
//			return true;
//		}

//	
//if (_usedProtocol.equals(NegotiationParameters.key4mirrorProto)){
//	if (!alpha_high.equals(Double.NaN) || !alpha_low.equals(Double.NaN)){
//		//				System.out.println("alpha 2not valid "+alpha_high+" "+alpha_low);
//		assert !alpha_high.equals(Double.NaN) && !alpha_low.equals(Double.NaN);
//		return false;
//	}
//	if (!this.opinionDiffusionDegree.equals(Double.NaN)){
//		//				System.out.println("opinionDiffusionDegree 2not valid "+opinionDiffusionDegree);
//		return false;
//	}
//	if (!this._agentSelection.equals(SelectionType.Greedy)){
//		//				System.out.println("_agentSelection 2not valid");
//		return false;
//	}
//
//}


//			for (final AgentIdentifier h : rep.getMyCurrentState().getMyResourceIdentifiers()){
//				rep.addObserver(h,
//						SimpleObservationService.informationObservationKey);
//				rep.getMyInformation().add(this.rig.getHostState((ResourceIdentifier) h));
//			}
//}else if (){ //Status
//
//rep = new StatusReplica(
//		replicaId,
//		this.rig.getAgentState(replicaId),
//		this.getCore(true, this._usedProtocol, this._socialWelfare),
//		this.getSelectionCore(this._agentSelection),
//		this.getProposerCore(true, this._usedProtocol),
//		this.getInformationService(true, this._usedProtocol),
//		new ReverseCFPProtocol(),
//		this.dynamicCriticity,
//		opinionDiffusionDegree);

//			for (final AgentIdentifier h : rep.getMyCurrentState().getMyResourceIdentifiers()){
//				rep.addObserver(h,
//						SimpleObservationService.informationObservationKey);
//				rep.getMyInformation().add(this.rig.getHostState((ResourceIdentifier) h));
//			}
//
////
//// Creation de laborantin
//// /////////////////////////////////
//
//@Override
//public ReplicationLaborantin createNewLaborantin(
//		final ExperimentationParameters para, final APILauncherModule api)
//				throws NotEnoughMachinesException, CompetenceException, IfailedException {
//	ReplicationLaborantin l = null;
//	final ReplicationExperimentationParameters p = (ReplicationExperimentationParameters) para;
//	//		boolean erreur = true;
//	//		while (erreur)
//	//			try {
//	l = new ReplicationLaborantin(p, api,this.getMaxNumberOfAgentPerMachine(null));
//	//				erreur = false;
//	//			} catch (final IfailedException e) {
//	//				LogService.writeException(
//	//						"retrying to launch simu " + p.getName()
//	//						+ " failure caused by : ", e.e);
//	//				erreur = true;
//	//			}
//
//	return l;
//}

//public String getDescription(){
//	return super.getDescription()+" simus of "
//			+ExperimentationProtocol._simulationTime/1000.+
//			"secs  on "+this.getApi().getAvalaibleHosts().size()+" machine";//+ReplicationExperimentationProtocol.nbSimuPerMAchine+" simu per machine"
//}

//	public final static String simulationResultStateObservationKey = "observe the state!";

// return "Simulation with parameters :\n"
// +"nbrAgents="+this.nbAgents
// +"; nbrHosts="+this.nbHosts
// +"; k="+this.kAccessible
// +"; agent load="+this.agentLoadMean
// +"; host faults="+this.hostFaultProbabilityMean
// +" nbrRepPerHostMoyen="+this.kAccessible/(this.nbAgents*this.agentLoadMean.getNumericValue());
// }