package frameworks.faulttolerance.olddcop.dcop;

public class Dependence {
	public int cid;
	public int pid;
	public int[] values;
	
	public Dependence(ReplicationVariable child, MemFreeConstraint c) {
		cid = child.id;
		ReplicationVariable parent = c.getNeighbor(child);
		pid = parent.id;
		values = new int[parent.getDomain()];
		for (int i=0;i<values.length;i++)
			values[i] = -1;
	}
	
}