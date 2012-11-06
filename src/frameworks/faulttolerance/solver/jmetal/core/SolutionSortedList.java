//  SolutionSet.Java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package frameworks.faulttolerance.solver.jmetal.core;

import java.io.*;
import java.util.*; 

import frameworks.faulttolerance.solver.jmetal.util.Configuration;
import frameworks.faulttolerance.solver.jmetal.util.PseudoRandom;


/** 
 * Class representing a SolutionSet (a set of solutions)
 */
public class SolutionSortedList extends PriorityQueue<Solution>  {
    
  
  /** 
   * Maximum size of the solution set 
   */
  int capacity_ = 0; 

  /** 
   * Creates a empty solutionSet with a maximum capacity.
   * @param maximumSize Maximum size.
   */
  public SolutionSortedList(int maximumSize){    
    capacity_      = maximumSize;
  } // SolutionSet

  /**
   * Constructor.
   * Creates an unbounded solution set.
   */
  public SolutionSortedList(SolutionSortedList s2) {
   super(s2);
    capacity_      = s2.capacity_;
  } // SolutionSet

  /**
   * Constructor.
   * Creates an unbounded solution set.
   */
  public SolutionSortedList(int maximumSize,SolutionSortedList s2) {
    super(s2);
    capacity_      = maximumSize;
  } // SolutionSet
    
  /** 
   * Creates a empty solutionSet with a maximum capacity.
   * @param maximumSize Maximum size.
   */
  public SolutionSortedList(int maximumSize, Comparator comparator){    
    super(maximumSize,comparator);
    capacity_      = maximumSize;
  } // SolutionSet
    
 /** 
  * Inserts a new solution into the SolutionSet. 
  * @param solution The <code>Solution</code> to store
  * @return True If the <code>Solution</code> has been inserted, false 
  * otherwise. 
  */
  public boolean add(Solution solution) {
    if (this.size() == capacity_) {
      Configuration.logger_.severe("The population is full");
      Configuration.logger_.severe("Capacity is : "+capacity_);
      Configuration.logger_.severe("\t Size is: "+ this.size());
      throw new RuntimeException();
//      return false;
    } // if
    
   super.add(solution);
    return true;
  } // add
  

  /** 
   * Inserts a new solution into the SolutionSet. 
   * @param solution The <code>Solution</code> to store
   * @return True If the <code>Solution</code> has been inserted, false 
   * otherwise. 
   */
   public synchronized boolean addIfImproving(Solution solution) {
	  Solution worst = this.worst();
	  Comparator<? super Solution> c = this.comparator();
	  
	  if (c.compare(solution, worst)>0){
		  this.removeWorst();
		  this.add(solution);
		  return true;
	  }
     return false;
   } // add
  /**
   * Returns the maximum capacity of the solution set
   * @return The maximum capacity of the solution set
   */
  public int getMaxSize(){
    return capacity_ ;
  } // getMaxSize
  
  /** 
   * Returns the worst Solution using a <code>Comparator</code>.
   * If there are more than one occurrences, only the first one is returned
   * @param comparator <code>Comparator</code> used to compare solutions.
   * @return The worst Solution attending to the comparator or <code>null<code>
   * if the SolutionSet is empty
   */
  public Solution worst(){
	  return this.peek();
        
  } // worst
   
  public void removeWorst(){
	  this.poll();
  }
  
} // SolutionSet
