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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ethz.ugs.server.ENV;

/**
 * @author Aritra
 *
 */
public class ClientState {

	public Map<String, ClientStateDataStructure> stateMap;
	
	public ClientState()
	{
		this.stateMap = new ConcurrentHashMap<>();
	}
	
	public boolean containSSLId(String sslId)
	{
		return this.stateMap.containsKey(sslId);
	}
	
	public void addState(String sslId, List<Long> sliceIds)
	{
		this.stateMap.put(sslId, new ClientStateDataStructure(sliceIds, null));
	}
	
	public void addState(String sslId, List<Long> sliceIds, byte[] key)
	{
		this.stateMap.put(sslId, new ClientStateDataStructure(sliceIds, key));
	}
	
	public static String dummySSLId = "000-000-0000-000-00000-000-000";
	public void addDummyState()
	{
		List<Long> sliceIds  = new ArrayList<Long>();
		//make sure these steps are not optimizable by JVM
		for(int i = 0; i < 5; i++)
			sliceIds.add((long) (System.currentTimeMillis() + 5000));
		byte[] key = new byte[ENV.AES_KEY_SIZE];
		new SecureRandom().nextBytes(key);
		this.addState(dummySSLId, sliceIds, key);
		
	}
	
	public byte[] getkey(String sslId) throws RuntimeException
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		return this.stateMap.get(sslId).key;
	}
	
	public int getState(String sslId, long sliceId) throws RuntimeException
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		int state = cds.getState(sliceId);
		if(state == -1)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SLICE_ID_MISSING);
		
		return state;
	}
	
	public void setState(String sslId, long sliceId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		int state = cds.getState(sliceId);
		if(state == -1)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SLICE_ID_MISSING);
		
		cds.setState(sliceId, state);
	}
	
	public void setState(String sslId, List<Long> sliceIds) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		
		for(long sliceId : sliceIds)
		{
			int state = cds.getState(sliceId);
			if(state == -1) 
				continue;
			cds.setState(sliceId, state);
		}
	}

	public void incrementState(String sslId, long sliceId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		int state = cds.getState(sliceId);
		if(state == -1)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SLICE_ID_MISSING);
		
		cds.incrementState(sliceId);
	}
	
	public void incrementState(String sslId, List<Long> sliceIds) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		
		for(long sliceId : sliceIds)
		{
			int state = cds.getState(sliceId);
			if(state == -1)
				continue;
			cds.incrementState(sliceId);
		}
	}
	
	public void incrementStateDummy(String sslId, long sliceId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		int state = cds.getState(sliceId);
		if(state == -1)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SLICE_ID_MISSING);
		
		cds.incrementStateDummy(sliceId);
	}
	
	
	public void incrementStateDummy(String sslId, List<Long> sliceIds) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		
		for(long sliceId : sliceIds)
		{
			int state = cds.getState(sliceId);
			if(state == -1)
				continue;
			cds.incrementStateDummy(sliceId);
		}
	}
	
	public void removeState(String sslId, List<Long> sliceIds) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		for(long sliceId : sliceIds)
			cds.removeState(sliceId);
	}
	
	public void removeState(String sslId, long sliceId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		cds.removeState(sliceId);
	}
	
	public long getASliceId(String sslId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		if(cds.clientStateMap.size() == 0)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_EMPTY_STATE_TABLE);
		
		return cds.clientStateMap.keySet().iterator().next();
	}
}


class ClientStateDataStructure
{
	public Map<Long, Integer> clientStateMap;
	
	byte[] key;
	public ClientStateDataStructure(List<Long> sliceIds, byte[] key)
	{
		this.clientStateMap = new HashMap<>();
		//set all initial states as 0x00
		if(sliceIds != null)
		{
			for(long sliceId : sliceIds)
				this.clientStateMap.put(sliceId, 0);
		}
		
		if(key != null)
			this.key = key;
	}
	
	public int getState(long sliceId)
	{
		return this.clientStateMap.containsKey(sliceId) ? this.clientStateMap.get(sliceId) : -1;
	}
	
	public void setState(long sliceId, int state)
	{
		 this.clientStateMap.put(sliceId, state);
	}
	
	public void incrementState(long sliceId)
	{
		if(!this.clientStateMap.containsKey(sliceId))
			this.clientStateMap.put(sliceId, 0);
		else
			this.clientStateMap.put(sliceId, this.clientStateMap.get(sliceId) + 1);
	}
	
	public void incrementStateDummy(long sliceId)
	{
		if(!this.clientStateMap.containsKey(sliceId))
			this.clientStateMap.put(sliceId, 0);
		else
			this.clientStateMap.put(sliceId, (this.clientStateMap.get(sliceId) + 1) % 2);
	}
	
	public void removeState(long sliceId)
	{
		if(this.clientStateMap.containsKey(sliceId))
			this.clientStateMap.remove(sliceId);
	}
}
