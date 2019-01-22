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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ethz.ugs.server.ENV;

/**
 * @author Aritra
 *
 */
public class ChatManager {


	private Map<String, List<byte[]>> AddressChatDataMap;
	private Map<String, String> AddressSSLMap;


	public ChatManager() {
		this.AddressChatDataMap = new HashMap<>();
		this.AddressSSLMap = new HashMap<>();
	}
	
	public boolean containSSLId(String sslId)
	{
		return this.AddressSSLMap.containsValue(sslId);
	}
	
	public byte[] getChat(String sslId)
	{
		if(!this.AddressSSLMap.containsValue(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		for(String address : AddressSSLMap.keySet())
		{
			String fetchedSSLId = AddressSSLMap.get(address);
			//System.out.println(address + " -> " + fetchedSSLId);
			if(sslId.equals(fetchedSSLId))
			{
				return getChatbyAddress(address);
			}
		}
		
		return null;
	}

	public byte[] getChatbyAddress(String publicAddress)
	{
		if(!AddressChatDataMap.containsKey(publicAddress))
			return null;
		List<byte[]> chatData = this.AddressChatDataMap.get(publicAddress);

		if(chatData.size() == 0)
			return null;

		byte[] dataToRet = chatData.get(0);
		chatData.remove(0);

		return dataToRet;
	}
	
	public void addChat(String sourceSSLId, String sourceAddress, String targetAddress, byte[] data)
	{
		//renew the ssl id corresponding to the 
		if(AddressChatDataMap.containsKey(sourceAddress))
			AddressSSLMap.put(sourceAddress, sourceSSLId);
		
		if(!AddressChatDataMap.containsKey(targetAddress))
		{
			List<byte[]> chatData = new ArrayList<>();		
			chatData.add(data);
			AddressChatDataMap.put(targetAddress, chatData);
			AddressSSLMap.put(sourceAddress, sourceSSLId);
		}
		else
		{
			List<byte[]> chatData = AddressChatDataMap.get(targetAddress);
			chatData.add(data);
			AddressChatDataMap.put(targetAddress, chatData);
			AddressSSLMap.put(sourceAddress, sourceSSLId);
		}
		
		//if the target address is new to system
		if(!AddressSSLMap.containsKey(targetAddress))
			AddressSSLMap.put(targetAddress, null);
		
		//for(String add : AddressSSLMap.keySet())
		//	System.out.println(add + " => " + AddressSSLMap.get(add));
		
		
	}
}
