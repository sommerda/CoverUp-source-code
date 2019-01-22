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

package com.ethz.ugs.dataStructures;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ethz.ugs.server.ENV;

public class PeerMap {
	
	private static ScheduledThreadPoolExecutor executor;
	
	public static volatile boolean lock = false;
	public static volatile Map<String, Long> PEER_MAP = new ConcurrentHashMap<>();
	
	public static synchronized void addToPeerMap(String key)
	{
		synchronized (PEER_MAP) 
		{
			lock = true;
			PEER_MAP.put(key, System.currentTimeMillis());
			lock = false;
		}
	}
	
	public static synchronized boolean isInPeerMap(String key)
	{
		boolean toReturn = false;
		synchronized (PEER_MAP) 
		{	
			lock = true;
			toReturn = PEER_MAP.containsKey(null);
			lock = false;
		}
		
		return toReturn;
	}
	
	/**
	 * Checks for dead peer in the global peer table
	 */
	public static void deadPeerChecker()
	{
		Runnable myRunnable = new Runnable() {
			@Override
			public void run() 
			{
				synchronized (PEER_MAP) 
				{				
					lock = true;

					//int counter = 0;
					for(String key : PEER_MAP.keySet())				
						if(System.currentTimeMillis() - PEER_MAP.get(key) > ENV.PEER_TIMEOUT)						
							PEER_MAP.remove(key);
							//counter++;
											
					lock = false;
					
					//System.out.println("done..." + counter);
				}
			}
		};

		executor = new ScheduledThreadPoolExecutor(2);
		executor.scheduleAtFixedRate(myRunnable, 0, ENV.PEER_CHECK_SCHEDULE, TimeUnit.MILLISECONDS);
	}
	
	//test
	public static void main(String[] args) throws InterruptedException 
	{
		deadPeerChecker();
		for(int i = 0; i < 1000; i++)
			addToPeerMap(new Integer(i).toString());
		
	}

}



