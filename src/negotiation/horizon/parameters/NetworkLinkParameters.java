package negotiation.horizon.parameters;


public class NetworkLinkParameters implements LinkParameters {
    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 2427947466031820192L;
    private final LinkAllocableParameters lap;
    private final LinkMeasurableParameters lmp;

    public NetworkLinkParameters(final LinkAllocableParameters allocableParams,
	    final LinkMeasurableParameters measurableParams) {
	this.lap = allocableParams;
	this.lmp = measurableParams;
    }

    public LinkAllocableParameters getAllocableParams() {
	return this.lap;
    }

    public LinkMeasurableParameters getMeasurableParams() {
	return this.lmp;
    }
}