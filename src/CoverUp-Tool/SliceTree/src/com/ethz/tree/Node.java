//*************************************************************************************
//*********************************************************************************** *
//author Aritra Dhar 																* *
//PhD Researcher																  	* *
//ETH Zurich													   				    * *
//Zurich, Switzerland															    * *
//--------------------------------------------------------------------------------- * * 
///////////////////////////////////////////////// 									* *
//This program is meant to do world domination... 									* *
///////////////////////////////////////////////// 									* *
//*********************************************************************************** *
//*************************************************************************************
package com.ethz.tree;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Aritra
 *
 */
public class Node implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 494944619751553925L;
	public String sliceName;
	public long sliceId;
	public List<Node> children;
	
	Node(String sliceName, Long sliceId)
	{
		this.sliceId = sliceId;
		this.sliceName = sliceName;
		this.children = Collections.emptyList();
	}
	
	public boolean isLeaf()
	{
		return this.children.isEmpty();
	}

	@Override
	public String toString() {
		return this.sliceName;
	}
}
