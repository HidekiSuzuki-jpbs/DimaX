package negotiation.horizon.negotiatingagent;

import negotiation.negotiationframework.contracts.ResourceIdentifier;

/**
 * Extension of an AgentUniqueIdentifier for clarity with SubstrateNodes.
 * 
 * @author Vincent Letard
 */
public class SubstrateNodeIdentifier extends ResourceIdentifier implements
	HorizonIdentifier {

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 8272223481939666990L;

    public SubstrateNodeIdentifier(String url, Integer port) {
	super(url, port);
    }
}