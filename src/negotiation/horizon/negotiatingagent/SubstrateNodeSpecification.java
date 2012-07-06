package negotiation.horizon.negotiatingagent;

import negotiation.horizon.parameters.HorizonMeasurableParameters;

public class SubstrateNodeSpecification extends HorizonSpecification {

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = -1464884496624059098L;
    private final SubstrateNodeIdentifier agentId;
    private final HorizonMeasurableParameters<SubstrateNodeIdentifier> measuredParams;

    public SubstrateNodeSpecification(final SubstrateNodeIdentifier id,
	    final HorizonMeasurableParameters<SubstrateNodeIdentifier> params) {
	this.agentId = id;
	this.measuredParams = params;
    }

    @Override
    public SubstrateNodeIdentifier getMyAgentIdentifier() {
	return this.agentId;
    }

    public HorizonMeasurableParameters<SubstrateNodeIdentifier> getParams() {
	return this.measuredParams;
    }

}