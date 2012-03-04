package negotiation.negotiationframework.protocoles.collaborative;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import negotiation.negotiationframework.SimpleNegotiatingAgent;
import negotiation.negotiationframework.communicationprotocol.AbstractCommunicationProtocol;
import negotiation.negotiationframework.communicationprotocol.AbstractCommunicationProtocol.ProposerCore;
import negotiation.negotiationframework.contracts.AbstractActionSpecification;
import negotiation.negotiationframework.contracts.MatchingCandidature;
import dima.introspectionbasedagents.services.BasicAgentCompetence;
import dima.introspectionbasedagents.services.loggingactivity.LogService;
import dima.introspectionbasedagents.shells.NotReadyException;

public class ResourceInformedProposerCore<
Contract extends MatchingCandidature<ActionSpec>,
ActionSpec extends AbstractActionSpecification,
PersonalState extends ActionSpec>
extends BasicAgentCompetence<SimpleNegotiatingAgent<ActionSpec, PersonalState, InformedCandidature<Contract, ActionSpec>>>
implements ProposerCore<
SimpleNegotiatingAgent<ActionSpec, PersonalState, InformedCandidature<Contract,ActionSpec>>,
ActionSpec,
PersonalState,
InformedCandidature<Contract,ActionSpec>> {

	private Collection<InformedCandidature<Contract, ActionSpec>> contractsToPropose =
			new HashSet<InformedCandidature<Contract, ActionSpec>>();


	public void addContractsToPropose(
			Collection<InformedCandidature<Contract, ActionSpec>> contractsToPropose) {
		this.contractsToPropose.addAll(contractsToPropose);
	}


	@Override
	public Set<? extends InformedCandidature<Contract, ActionSpec>> getNextContractsToPropose()
			throws NotReadyException {
		logMonologue("proposing "+contractsToPropose, AbstractCommunicationProtocol.log_negotiationStep);
		final Set<InformedCandidature<Contract, ActionSpec>> result =
				new HashSet<InformedCandidature<Contract, ActionSpec>>();
		result.addAll(this.contractsToPropose);
		this.contractsToPropose.clear();
		return result;
	}

}