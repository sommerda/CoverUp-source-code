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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;

/**
 * Response processing class. Provide resposne in byte stream.
 * @author Aritra
 *
 */
public class ResponseUtilBinBroadcast {

	public static volatile ConcurrentHashMap<String, byte[]> BROADCAST_LIST = new ConcurrentHashMap<>();
	public static volatile String GLOBAL_BROADCAST;
	public static volatile byte[] GLOBAL_BROADCAST_BIN;
	private static ScheduledExecutorService executor;
	private static SecureRandom rand = new SecureRandom();

	public static boolean binSwitch = true;

	public static void BroadcastBin(HttpServletRequest request, HttpServletResponse response, byte[] postBody, byte[] privateKey) throws IOException
	{
		long start = System.currentTimeMillis();
		String id = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
		if(postBody.length == 0)
		{
			postBody = new byte[ENV.FIXED_CHAT_LEN];
			rand.nextBytes(postBody);
		}
		BROADCAST_LIST.put(id, postBody);
		byte[] packetToSend = null;
		try {
			if(!binSwitch)
				packetToSend = ResponseUtilBinBroadcast.dropletPlease(privateKey).toString().getBytes(StandardCharsets.UTF_8);
			else
				packetToSend = ResponseUtilBinBroadcast.dropletPleaseBin(privateKey);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		long offset = ENV.FIXED_REQUEST_PROCESSING_TIME_MILI - (System.currentTimeMillis() - start);
		try {
			if(offset > 0)
				TimeUnit.MILLISECONDS.sleep(offset);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		response.getOutputStream().write(packetToSend);
		response.flushBuffer();
	}

	public static void init()
	{
		Runnable myRunnable = new Runnable() {

			public void run() {
				if(!binSwitch)
					makeBroadCast();
				else
					makeBroadCastBin();
			}
		};

		executor = new ScheduledThreadPoolExecutor(5);
		executor.scheduleAtFixedRate(myRunnable, 0, 5000, TimeUnit.MILLISECONDS);
	}

	/**
	 * Cleanup the global list
	 */
	public static void resetGlobalMap(int hours)
	{
		Runnable myRunnable = new Runnable() {

			public void run() {
				BROADCAST_LIST = new ConcurrentHashMap<>();
			}
		};

		executor = new ScheduledThreadPoolExecutor(5);
		executor.scheduleAtFixedRate(myRunnable, 0, hours, TimeUnit.HOURS);
	}

	public static synchronized void makeBroadCast()
	{
		synchronized (BROADCAST_LIST) 
		{
			JSONArray jArray = new JSONArray();
			for(String id : BROADCAST_LIST.keySet())
				jArray.put(Base64.getEncoder().encodeToString(BROADCAST_LIST.get(id)));

			GLOBAL_BROADCAST = jArray.toString();
		}
	}

	public static synchronized void makeBroadCastBin()
	{
		synchronized (BROADCAST_LIST) 
		{
			byte[] megaMessageBlob = new byte[BROADCAST_LIST.keySet().size() * ENV.FIXED_CHAT_LEN]; 
			int tillNow = 0;
			for(String id : BROADCAST_LIST.keySet())
			{
				System.arraycopy(BROADCAST_LIST.get(id), 0, megaMessageBlob, tillNow, BROADCAST_LIST.get(id).length);
				tillNow += BROADCAST_LIST.get(id).length;
			}
			GLOBAL_BROADCAST_BIN = megaMessageBlob;
		}
	}

	/**
	 * Good old broadcast json
	 * @param privateKey Server's curve25519 private key for signature
	 * @throws NoSuchAlgorithmException 
	 */
	public static synchronized JSONObject dropletPlease(byte[] privateKey) throws IOException, NoSuchAlgorithmException
	{
		String[] dropletStr = new String[2];
		dropletStr = SiteMap.getRandomDroplet(null);
		String url = dropletStr[1];

		System.err.println("Fountain served : " + url);

		JSONObject jObject = new JSONObject();
		//sign droplet|url

		String dropletStrMod = dropletStr[0].concat(url);

		byte[] dropletByte = dropletStrMod.getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = null;
		String signatureBase64 = null;


		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashtableBytes = md.digest(dropletByte);

		System.out.println("hash : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));

		signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
		signatureBase64 = Base64.getUrlEncoder().encodeToString(signatureBytes);

		jObject.put("url", url);
		jObject.put("f_id", FountainTableRow.dropletLocUrlMapRev.get(url));
		jObject.put("droplet", dropletStr[0]);
		jObject.put("signature", signatureBase64);	
		jObject.put("messaages", new JSONArray(GLOBAL_BROADCAST));

		if(ENV.PADDING_ENABLE)
		{
			String responseString = jObject.toString();
			int padLen = ENV.FIXED_PACKET_SIZE - responseString.length();
			String stringPadding = null;

			if(ENV.RANDOM_PADDING)
				stringPadding = ServerUtil.randomString(padLen);
			else
				stringPadding = ServerUtil.deterministicString(padLen);

			jObject.put("pad", stringPadding);
		}

		return jObject;
	}

	/**
	 * P = fixed packet size, q = fixed chat length
	 * <br>
	 * table-> packet_len (4) | droplet (n) | url_len (4) | url (n_1) | f_id (8) | signature (64) | num_chat (4)->k | chats(k*q) | padding (p - 76 - n - n_1 - k*q) |</br>
	 * 
	 * Signature is on  packet_len | droplet | url_len | url | f_id 
	 * <br>
	 * droplet -> seedlen (4) | seed(n) | num_chunk (4) | datalen (4) | data (n)
	 * <br>
	 * @param privateKey Server's Curve 25519 private key 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 */

	public static synchronized byte[] dropletPleaseBin(byte[] privateKey) throws IOException, NoSuchAlgorithmException
	{
		String[] dropletStr = new String[2];

		dropletStr = SiteMap.getRandomDroplet(null);
		String url = dropletStr[1];
		System.err.println("Fountain served : " + url);

		JSONObject jObject2 = new JSONObject(dropletStr[0]);
		byte[] seedBytes = Base64.getUrlDecoder().decode(jObject2.getString("seed"));
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(seedBytes.length).array();
		byte[] num_chunksBytes = ByteBuffer.allocate(Integer.BYTES).putInt(jObject2.getInt("num_chunks")).array();
		byte[] data = Base64.getUrlDecoder().decode(jObject2.getString("data"));
		byte[] dataLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
		//System.out.println(dataLenBytes[3] + "," + dataLenBytes[2] + "," + dataLenBytes[1] + "," + dataLenBytes[0]);


		byte[] dropletByte = new byte[seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length + data.length];

		System.arraycopy(seedLenBytes, 0, dropletByte, 0, seedLenBytes.length);
		System.arraycopy(seedBytes, 0, dropletByte, seedLenBytes.length, seedBytes.length);
		System.arraycopy(num_chunksBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length, num_chunksBytes.length);
		System.arraycopy(dataLenBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length, dataLenBytes.length);
		System.arraycopy(data, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length, data.length);

		byte[] fixedPacketLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();

		byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
		byte[] urlLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(urlBytes.length).array();
		byte[] f_idBytes = ByteBuffer.allocate(Long.BYTES).putLong(FountainTableRow.dropletLocUrlMapRev.get(url)).array();

		byte[] dataToSign = new byte[fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length + f_idBytes.length];

		System.arraycopy(fixedPacketLenBytes, 0, dataToSign, 0, fixedPacketLenBytes.length);
		System.arraycopy(dropletByte, 0, dataToSign, fixedPacketLenBytes.length, dropletByte.length);
		System.arraycopy(urlLenBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length, urlLenBytes.length);
		System.arraycopy(urlBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length, urlBytes.length);
		System.arraycopy(f_idBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length, f_idBytes.length);

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashtableBytes = md.digest(dataToSign);
		byte[] signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);


		byte[] dataNchat = null;
		synchronized (GLOBAL_BROADCAST_BIN) 
		{
			int numChat = GLOBAL_BROADCAST_BIN.length / ENV.FIXED_CHAT_LEN;
			byte[] numChatBytes = ByteBuffer.allocate(Integer.BYTES).putInt(numChat).array();
			dataNchat = new byte[dataToSign.length + numChatBytes.length + GLOBAL_BROADCAST_BIN.length];
			System.arraycopy(dataToSign, 0, dataNchat, 0, dataToSign.length);
			System.arraycopy(numChatBytes, 0, dataNchat, dataToSign.length, numChatBytes.length);
			System.arraycopy(GLOBAL_BROADCAST_BIN, 0, dataNchat, dataToSign.length + numChatBytes.length, GLOBAL_BROADCAST_BIN.length);
		}

		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - dataNchat.length - signatureBytes.length];
		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);

		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];

		//dataToSign | signature | padding
		System.arraycopy(dataToSign, 0, packetToSend, 0, dataToSign.length);
		System.arraycopy(signatureBytes, 0, packetToSend, dataToSign.length, signatureBytes.length);
		System.arraycopy(padding, 0, packetToSend, dataToSign.length + signatureBytes.length, padding.length);

		return packetToSend;
	}
}
