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

package com.ethz.ugs.server;

import java.util.HashSet;
import java.util.Set;

public class Stats {
	
	public static long TOTAL_CONNECTIONS = 0;
	public static long LIVE_CONNECTIONS = 0;
	
	public static boolean keygen_done = false;
	
	public static Set<String> UNIQUE_IP_ADDRESSES = new HashSet<>();

}
