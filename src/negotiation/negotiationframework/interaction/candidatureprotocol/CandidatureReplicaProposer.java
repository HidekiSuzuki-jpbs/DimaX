package negotiation.negotiationframework.interaction.candidatureprotocol;

import java.util.HashSet;
import java.util.Set;

import negotiation.faulttolerance.negotiatingagent.ReplicaState;
import negotiation.faulttolerance.negotiatingagent.ReplicationCandidature;
import negotiation.faulttolerance.negotiatingagent.ReplicationSpecification;
import negotiation.negotiationframework.SimpleNegotiatingAgent;
import negotiation.negotiationframework.interaction.consensualnegotiation.AbstractProposerCore;
import negotiation.negotiationframework.interaction.contracts.ResourceIdentifier;
import dima.basicagentcomponents.AgentIdentifier;
import dima.introspectionbasedagents.NotReadyException;
import dima.introspectionbasedagents.services.BasicAgentCompetence;

public class CandidatureReplicaProposer
extends
BasicAgentCompetence
<SimpleNegotiatingAgent<ReplicationSpecification, ReplicaState, ReplicationCandidature>>
implements
AbstractProposerCore
<SimpleNegotiatingAgent<ReplicationSpecification, ReplicaState, ReplicationCandidature>,
ReplicationSpecification, ReplicaState, ReplicationCandidature> {
	private static final long serialVersionUID = -5315491050460219982L;

	@Override
	public Set<ReplicationCandidature> getNextContractsToPropose()
			throws NotReadyException {

		final Set<ReplicationCandidature> candidatures = new HashSet<ReplicationCandidature>();

		for (final AgentIdentifier id : this.getMyAgent().getMyInformation().getKnownAgents())
			if (id instanceof ResourceIdentifier
					&& !this.getMyAgent().getMyCurrentState().getMyResourceIdentifiers()
					.contains(id))
				candidatures.add(
						new ReplicationCandidature(
								(ResourceIdentifier) id,
								this.getMyAgent().getIdentifier(),
								true,true));

				return candidatures;
	}

}
//
//	final boolean mirrorProto;
//
//	public CandidatureReplicaProposer(boolean mirrorProto) {
//		this.mirrorProto = mirrorProto;
//	}
//this.mirrorProto ? new ReplicationCandidatureWithMinInfo(
//								(ResourceIdentifier) id, this.getMyAgent()
//										.getIdentifier(), true)
//								: