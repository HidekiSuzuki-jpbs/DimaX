package negotiation.faulttolerance.collaborativecandidature;

import java.io.Serializable;
import java.util.Collection;

import negotiation.experimentationframework.ExperimentationResults;
import negotiation.experimentationframework.ObservingSelfService;
import negotiation.faulttolerance.experimentation.ReplicationResultHost;
import negotiation.faulttolerance.faulsimulation.FaultObservationService;
import negotiation.faulttolerance.faulsimulation.HostDisponibilityComputer;
import negotiation.faulttolerance.negotiatingagent.HostCore;
import negotiation.faulttolerance.negotiatingagent.HostState;
import negotiation.faulttolerance.negotiatingagent.ReplicationCandidature;
import negotiation.faulttolerance.negotiatingagent.ReplicationSpecification;
import negotiation.negotiationframework.SimpleNegotiatingAgent;
import negotiation.negotiationframework.communicationprotocol.OneDeciderCommunicationProtocol;
import negotiation.negotiationframework.contracts.AbstractActionSpecification;
import negotiation.negotiationframework.contracts.ResourceIdentifier;
import negotiation.negotiationframework.protocoles.collaborative.InformedCandidature;
import negotiation.negotiationframework.protocoles.collaborative.ResourceInformedCandidatureContractTrunk;
import negotiation.negotiationframework.protocoles.collaborative.InformedCandidatureRationality;
import negotiation.negotiationframework.protocoles.collaborative.ResourceInformedProposerCore;
import negotiation.negotiationframework.protocoles.collaborative.ResourceInformedSelectionCore;
import negotiation.negotiationframework.selectioncores.GreedyBasicSelectionCore;
import dima.basicagentcomponents.AgentIdentifier;
import dima.introspectionbasedagents.annotations.Competence;
import dima.introspectionbasedagents.services.AgentCompetence;
import dima.introspectionbasedagents.services.CompetenceException;
import dima.introspectionbasedagents.services.information.SimpleObservationService;

public class CollaborativeHost
extends	SimpleNegotiatingAgent<ReplicationSpecification, HostState, InformedCandidature<ReplicationCandidature,ReplicationSpecification>>
{
	private static final long serialVersionUID = -8478683967125467116L;

	//
	// Fields
	//

	@Competence
	ObservingSelfService mySelfObservationService = new ObservingSelfService() {

		/**
		 *
		 */
		private static final long serialVersionUID = -6008018665463786541L;

		@Override
		protected ExperimentationResults generateMyResults() {
			return new ReplicationResultHost(
					CollaborativeHost.this.getMyCurrentState(),
					CollaborativeHost.this.getCreationTime());
		}
	};

	@Competence
	public
	FaultObservationService<ReplicationSpecification, HostState,  InformedCandidature<ReplicationCandidature,ReplicationSpecification>> myFaultAwareService =
	new FaultObservationService<ReplicationSpecification, HostState,  InformedCandidature<ReplicationCandidature,ReplicationSpecification>>() {

		/**
		 *
		 */
		private static final long serialVersionUID = -5530153574167669156L;

		@Override
		protected void resetMyState() {
			CollaborativeHost.this.setNewState(new HostState((ResourceIdentifier) this.getIdentifier(),
					CollaborativeHost.this.getMyCurrentState().getLambda(),this.getMyAgent().nextStateCounter));
			//			this.resetMyUptime();
		}

		@Override
		protected void resetMyUptime() {

			assert 1<0:"Host.this.getMyCurrentState().resetUptime()";
		}

	};

	//
	// Constructor
	//


	public CollaborativeHost(
			final ResourceIdentifier myId,
			final double lambda,
			final String socialWelfare,
			final HostDisponibilityComputer myDispoInfo)
					throws CompetenceException {
		super(
				myId, 
				new HostState(myId, lambda,-1),
				new InformedCandidatureRationality(new HostCore(true, socialWelfare),false),
				new ResourceInformedSelectionCore(){
					@Override
					protected InformedCandidature generateDestructionContract(final AgentIdentifier id) {
						return new InformedCandidature(new ReplicationCandidature(myId,id,false,false));
					}
				},//new GreedyBasicSelectionCore(true, false),// 
				new ResourceInformedProposerCore(),
				new SimpleObservationService(),
				new OneDeciderCommunicationProtocol( new ResourceInformedCandidatureContractTrunk(), true) );
		getMyProtocol().getContracts().setMyAgent(this);
	}
}