package frameworks.faulttolerance.dcop.dcop;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.HashSet;
import java.util.Vector;

import frameworks.faulttolerance.dcop.algo.topt.AsyncHelper;
import frameworks.faulttolerance.dcop.algo.topt.DPOPTreeNode;
import frameworks.faulttolerance.dcop.algo.topt.RewardMatrix;
import frameworks.faulttolerance.dcop.algo.topt.TreeNode;



public abstract class DcopAbstractGraph {

	public HashMap<Integer, AbstractVariable> varMap;
	public Vector<AbstractConstraint> conList;

	public DcopAbstractGraph() {
		varMap = new HashMap<Integer, AbstractVariable>();
		conList = new Vector<AbstractConstraint>();
	}

	public DcopAbstractGraph(String inFilename) {
		// We assume in the input file, there is at most one link between two
		// variables
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					inFilename));
			varMap = new HashMap<Integer, AbstractVariable>();
			conList = new Vector<AbstractConstraint>();
			String line;
			AbstractConstraint c = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("VARIABLE")) {
					AbstractVariable v = new AbstractVariable(line, this);
					varMap.put(v.id, v);
				} else if (line.startsWith("CONSTRAINT")) {
					String[] ss = line.split(" ");
					assert ss.length >= 3;
					int first = Integer.parseInt(ss[1]);
					int second = Integer.parseInt(ss[2]);
					assert varMap.containsKey(first);
					assert varMap.containsKey(second);
					c = new AbstractConstraint(varMap.get(first), varMap.get(second));
					conList.add(c);
				} else if (line.startsWith("F")) {
					assert c != null;
					String[] ss = line.split(" ");
					assert ss.length >= 4;
					int x = Integer.parseInt(ss[1]);
					int y = Integer.parseInt(ss[2]);
					int v = Integer.parseInt(ss[3]);
					c.f[x][y] = v;
				}
			}
			for (AbstractConstraint cc : conList)
				cc.cache();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public abstract double evaluate();
	public abstract HashMap<Integer, Integer> solve();
	

	public AbstractVariable getVar(int i) {
		return varMap.get(i);
	}

	public boolean checkValues() {
		for (AbstractVariable v : varMap.values())
			if (v.value == -1)
				return false;
		return true;
	}
	
	public double evaluate(HashMap<Integer, Integer> sol) {
		this.backup();
		for (AbstractVariable v : varMap.values())
			v.value = sol.get(v.id);
		double sum = this.evaluate();
		this.recover();
		return sum;
	}

	public boolean sameSolution(HashMap<Integer, Integer> sol) {
		if (sol == null)
			return false;
		for (AbstractVariable v : varMap.values()) {
			if (!sol.containsKey(v.id))
				return false;
			if (v.value != sol.get(v.id))
				return false;
		}
		return true;
	}

	public void clear() {
		for (AbstractVariable v : varMap.values())
			v.clear();
	}

	public void backup() {
		for (AbstractVariable v : varMap.values())
			v.backupValue();
	}

	public void recover() {
		for (AbstractVariable v : varMap.values())
			v.recoverValue();
	}


	public HashMap<Integer, Integer> getSolution() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (AbstractVariable v : varMap.values())
			map.put(v.id, v.value);
		return map;
	}

}
