package negotiation.horizon.negotiatingagent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import negotiation.negotiationframework.rationality.AgentState;
import negotiation.negotiationframework.rationality.SocialChoiceFunction;

public class HorizonPreferenceFunction extends
	SocialChoiceFunction<HorizonSpecification, HorizonContract> {

    public enum Service {
	Voice(3, 1, 2, 4), Videophony(3, 1, 2, 4), Telephony(3, 2, 4, 1), Multimedia(
		2, 1, 4, 3), VOD(3, 1, 4, 2), VPN(3, 1, 4, 2), DataTealTime(2,
		1, 4, 3), Data(1, 2, 4, 3), Streaming(1, 3, 4, 2);

	private final int[] priorities;

	private Service(final int packetLossRate, final int delay,
		final int jitter, final int availability) {
	    assert (packetLossRate != delay && packetLossRate != jitter
		    && packetLossRate != availability && delay != jitter
		    && delay != availability && jitter != availability);
	    this.priorities = new int[] { packetLossRate, delay, jitter,
		    availability };
	}

	/**
	 * Sorts a list of values in the same order as the constructor arguments
	 * according to their importance for this service.
	 * 
	 * @param <Type>
	 *            Comparable type of the values in the list
	 * @param values
	 *            The list of the values in initial order
	 * @return a list sorted by parameter importance
	 * @throws IllegalArgumentException
	 *             if the size of the list does not correspond to the number
	 *             of parameters
	 */
	public <Type> List<Type> sort(final List<Type> values)
		throws IllegalArgumentException {
	    if (values.size() != this.priorities.length)
		throw new IllegalArgumentException();

	    final List<Type> result = new ArrayList<Type>(values);

	    Iterator<Type> it = values.iterator();
	    for (int i : this.priorities)
		result.set(i, it.next());
	    return result;
	}

	/**
	 * Gives the relative priority of the i-th parameter for this service
	 * type. The highest is the priority the lowest is the returned integer.
	 * 
	 * @param i
	 *            the number of the parameter : 1 is packetLossRate, 2 is
	 *            delay, 3 is jitter, 4 is availability
	 * @return an inversely proportional to the priority integer
	 */
	public int getPriority(final int i) {
	    return this.priorities[i];
	}
    }

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = -2775977866055120559L;

    public HorizonPreferenceFunction(final SocialChoiceType socialWelfare) {
	super(socialWelfare);
    }

    @Override
    public UtilitaristEvaluator<AgentState> getUtilitaristEvaluator() {
	// service.sort;
	// Service.Voice.sort(i)
	return new UtilitaristEvaluator<AgentState>() {

	    @Override
	    public Double getUtilityValue(AgentState s) {
		if (!s.isValid())
		    throw new IllegalArgumentException();

		if (s instanceof VirtualNetworkState) {
		    return ((VirtualNetworkState) s).evaluateUtility();
		} else if (s instanceof SubstrateNodeState) {
		    return ((SubstrateNodeState) s).evaluateUtility();
		} else
		    throw new IllegalArgumentException();
	    }
	};
    }

    // private Double getUtility(final HorizonMeasurableParameters pref,
    // final HorizonMeasurableParameters alloc, final Service qos) {
    // final HeavyDoubleAggregation packetLossRateBag = new
    // HeavyDoubleAggregation();
    // final HeavyDoubleAggregation delayBag = new HeavyDoubleAggregation();
    // final HeavyDoubleAggregation jitterBag = new HeavyDoubleAggregation();
    //
    // InterfacesParameters<LinkMeasurableParameters> linksAlloc = alloc
    // .getInterfacesParameters();
    // for (Map.Entry<HorizonIdentifier, LinkMeasurableParameters> prefEntry :
    // pref
    // .getInterfacesParameters().entrySet()) {
    // assert (linksAlloc.containsKey(prefEntry.getKey()));
    // LinkMeasurableParameters prefLink = prefEntry.getValue(), allocLink =
    // linksAlloc
    // .get(prefEntry.getKey());
    // {
    // Interval<Float> i = Interval.inter(allocLink
    // .getPacketLossRate(), prefLink.getPacketLossRate());
    // try {
    // packetLossRateBag
    // .add((double) (i.getUpper() - i.getLower())
    // / (prefLink.getPacketLossRate().getUpper() - prefLink
    // .getPacketLossRate().getLower()));
    // } catch (EmptyIntervalException e) {
    // packetLossRateBag.add(0.);
    // }
    // }
    // {
    // Interval<Integer> i = Interval.inter(allocLink.getDelay(),
    // prefLink.getDelay());
    // try {
    // delayBag.add((double) (i.getUpper() - i.getLower())
    // / (prefLink.getDelay().getUpper() - prefLink
    // .getDelay().getLower()));
    // } catch (EmptyIntervalException e) {
    // delayBag.add(0.);
    // }
    // }
    // {
    // Interval<Integer> i = Interval.inter(allocLink.getJitter(),
    // prefLink.getJitter());
    // try {
    // jitterBag.add((double) (i.getUpper() - i.getLower())
    // / (prefLink.getJitter().getUpper() - prefLink
    // .getJitter().getLower()));
    // } catch (EmptyIntervalException e) {
    // jitterBag.add(0.);
    // }
    // }
    // }
    // final LightWeightedAverageDoubleAggregation utilityBag = new
    // LightWeightedAverageDoubleAggregation();
    // utilityBag.add(packetLossRateBag.getRepresentativeElement(), qos
    // .getPriority(0));
    // utilityBag.add(delayBag.getRepresentativeElement(), qos.getPriority(1));
    // utilityBag
    // .add(jitterBag.getRepresentativeElement(), qos.getPriority(2));
    // utilityBag.add(
    // ((double) alloc.getMachineParameters().getAvailability())
    // / ((double) pref.getMachineParameters()
    // .getAvailability()), qos.getPriority(3));
    //
    // return utilityBag.getRepresentativeElement();
    // }

    @Override
    protected <State extends AgentState> Collection<State> cleanStates(
	    Collection<State> cs) {
	final Collection<State> res = new ArrayList<State>();
	for (State s : cs) {
	    if (s instanceof VirtualNetworkState)
		res.add(s);
	    else if (!(s instanceof SubstrateNodeState))
		throw new IllegalArgumentException();
	}
	return res;
    }

    @Override
    public <State extends AgentState> Comparator<State> getComparator() {
	return new Comparator<State>() {

	    @Override
	    public int compare(State o1, State o2) {
		assert (o1 instanceof VirtualNetworkState && o2 instanceof VirtualNetworkState);

		// if (o1 instanceof SubstrateNodeState
		// && o2 instanceof SubstrateNodeState) {
		// return HorizonPreferenceFunction.this
		// .getUtilitaristEvaluator().getUtilityValue(o1)
		// .compareTo(
		// HorizonPreferenceFunction.this
		// .getUtilitaristEvaluator()
		// .getUtilityValue(o2));
		// } else if (o1 instanceof VirtualNetworkState
		// && o2 instanceof VirtualNetworkState) {
		return ((VirtualNetworkState) o1)
			.compareTo((VirtualNetworkState) o2);
		// } else
		// throw new IllegalArgumentException();
	    }
	};
    }
}