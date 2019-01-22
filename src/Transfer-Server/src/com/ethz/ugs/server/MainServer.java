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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import com.ethz.ugs.dataStructures.ChatManager;
import com.ethz.ugs.dataStructures.ClientState;
import com.ethz.ugs.test.InitialGen;


/**
 * Main server class for underground server implementation
 * @author Aritra
 *
 */
@WebServlet("/MainServer")
public class MainServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	
	public Set<String> codes;
	private byte[] sharedSecret;
	public byte[] publicKey;
	private byte[] privateKey;

	private Map<String, byte[]> sharedSecretMap;
	//public String broadCastMessage;
	public static ChatManager chatManager;

	public static Logger logger = Logger.getLogger(MainServer.class.getName());

	public static volatile int C = 0;
	public static final char[] charC = {'|', '/', '-', '\\'};

	public static ClientState clientState;

	public MainServer() throws IOException, InterruptedException, NoSuchAlgorithmException, NoSuchProviderException {
		super();

		File f = new File(".");
		System.out.println(f.getAbsolutePath());
		///////////////////////////////////////
		//initiate the client state for interactive data management
		MainServer.clientState = new ClientState();
		////////////////////////////////////////
		
		////////////////////////////////////////
		//Initiate the chat manager for client chat management 
		MainServer.chatManager = new ChatManager();
		////////////////////////////////////////
		
		////////////////////////////////////////
		//initiate chat broadcast lookup
		ResponseUtilBinBroadcast.init();
		/////////////////////////////////////////

		FileHandler fileH = new FileHandler("MainServer.log", true);
		fileH.setFormatter(new SimpleFormatter());
		MainServer.logger.addHandler(fileH);

		this.sharedSecretMap = new HashMap<>();

		String os = System.getProperty("os.name");
		System.out.println(os);

		
		LogManager.getLogManager().reset();

		BufferedReader br = null;
		try
		{
			if(os.contains("Windows"))
				br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\workspace_Mars\\UndergroundClient\\codes.bin"));
			else
				br = new BufferedReader(new FileReader("/home/dhara/codes.bin"));
			String str = "";
			this.codes = new HashSet<>();

			while((str = br.readLine()) != null)
				codes.add(str);

			br.close();
		}
		catch(Exception ex)
		{
			System.err.println("Code file unavailable");
		}
		if(!Stats.keygen_done)
		{
			//System.out.println("No keys. Generating...");
			keyGeneration();
		}
		//this.broadCastMessage = this.readBroadcastFile();


		//dummy initialization
		try
		{
			InitialGen.init();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.err.println("Fatal error..");
		}

		System.out.println("Started...");

		System.out.println("Default Charset=" + Charset.defaultCharset());    	
	}

	/*private String readBroadcastFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(ENV.BROADCAST_LOCATION));

		String s = "";
		StringBuffer sb = new StringBuffer("");
		while((s = br.readLine()) != null)
		{
			sb.append(s).append("\n");
		}

		br.close();
		return sb.toString();
	}*/

	/**
	 * Generate Curve25519 private and public key pairs
	 * @throws IOException
	 */
	private void keyGeneration() throws IOException
	{
		Stats.keygen_done = true;

		if(new File("pk.key").exists() && new File("pk.key").length() > 0 && new File("sk.key").exists() && new File("sk.key").length() > 0 )
		{
			System.err.println("Key file exists");

			BufferedReader pkBr = new BufferedReader(new FileReader("pk.key")); 
			this.publicKey = Base64.getUrlDecoder().decode(pkBr.readLine());
			pkBr.close();

			BufferedReader skBr = new BufferedReader(new FileReader("sk.key")); 
			this.privateKey = Base64.getUrlDecoder().decode(skBr.readLine());
			skBr.close();

			return;
		}
		else
		{
			System.err.println("Key file not found. Regenerating keyfile");


			FileWriter pkFw = new FileWriter("pk.key");
			FileWriter skFw = new FileWriter("sk.key");

			Curve25519KeyPair keypair = Curve25519.getInstance("best").generateKeyPair();
			this.publicKey = keypair.getPublicKey();
			pkFw.write(Base64.getUrlEncoder().encodeToString(this.publicKey));
			this.privateKey = keypair.getPrivateKey();
			skFw.write(Base64.getUrlEncoder().encodeToString(this.privateKey));

			pkFw.close();
			skFw.close();
		}
	}

	/**
	 * Right now we are supporting both GET and POST message. Later we have to stop supporting any GET call to this server
	 * Redirect to post
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request,response);
		//response.getWriter().append("Get request to this server is not supported");

	}


	/**
	 * Post. Default
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		synchronized (request) 
		{
			C += 1;
			C %= 4;
		}		
		//System.out.println(request.getAttribute("javax.servlet.request.ssl_session_id"));
		//response.setBufferSize(120000);		
		String flag = request.getParameter("flag");
		String flag1 = request.getParameter("prob");

		response.addHeader("Access-Control-Allow-Origin", "*");
		//response.setHeader("X-Frame-Options", "SAMEORIGIN");

		if(flag1 != null)
		{
			ENV.PROB_THRESHOLD = Double.parseDouble(flag1);

			response.getWriter().append("Prob reset");
			response.flushBuffer();
			return;
		}

		if(flag == null)
		{
			//byte[] bytes = Files.readAllBytes(new File("Index//index.html").toPath());
			response.getWriter().write("No valid HTTP parameter found!");
			response.flushBuffer();
		}

		else if(flag.equals("init"))
		{

			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;

			String code = request.getParameter("code");
			if(code == null)
				response.getWriter().append("No valid code ");
			else if(this.codes.contains(code))
				response.getWriter().append("code authenticated ");
		}

		else if(flag.equals("ke"))
		{
			ResponseUtil.ke(request, response, this.publicKey, this.privateKey, this.sharedSecret, this.sharedSecretMap);		
		}

		else if(flag.equals("admin"))
		{
			StringBuffer responseStr = new StringBuffer("Total connection : " + Stats.TOTAL_CONNECTIONS + "\nlive connections : " + Stats.LIVE_CONNECTIONS + "\n");
			//responseStr.append("Unique IP addresses\n");

			//for(String address : Stats.UNIQUE_IP_ADDRESSES)
			//	responseStr.append(address + "\n");

			responseStr.append(Base64.getUrlEncoder().encodeToString(publicKey)).append("\n");

			File[] files = new File(".").listFiles();

			for(File file : files)
			{
				if(file.getName().contains(".lck"))
					continue;

				if(!file.getName().contains("MainServer.log"))
					continue;


				BufferedReader br = new BufferedReader(new FileReader(file));
				List<Integer> el = new ArrayList<>();

				String st = null;
				int counter = 0, k = 0, tot = 0;
				while((st = br.readLine()) != null)
				{
					counter++;
					if(counter % 2 == 1)
						continue;
					if(st.length() == 0)
						continue;

					st = st.split(":")[2].trim().split(" ")[0].trim();
					k++;
					int l = Integer.parseInt(st);
					tot += l;
					el.add(l);
					//System.out.println(st);
				}
				br.close();
				double mean = (double) tot/k;

				double s = 0;
				for(int i : el)
					s += ((double)mean - i) * ((double)mean - i);

				double var = (double) s/ (k-1);

				responseStr.append("Log file : " + file.getName()).append("\n");
				responseStr.append("sample size : " + k).append("\n");
				responseStr.append("Mean : " + mean).append("\n");
				responseStr.append("Variance : " + var).append("\n");
				responseStr.append("----------------------------------\n");
			}
			response.getWriter().append(responseStr.toString());
			response.flushBuffer();
		}

		else if(flag.equals("testframe"))
		{
			byte[] bytes = Files.readAllBytes(new File("test.html").toPath());
			response.getOutputStream().write(bytes);
			response.flushBuffer();
		}

		else if(flag.equals("testframe_1"))
		{
			byte[] bytes = Files.readAllBytes(new File("test.js").toPath());
			response.getOutputStream().write(bytes);
			response.flushBuffer();
		}
		else if(flag.equals("testframe_2"))
		{
			byte[] bytes = Files.readAllBytes(new File("luka.html").toPath());
			response.getOutputStream().write(bytes);
			response.flushBuffer();
		}
		
		else if(flag.equals("testframe_constant"))
		{
			byte[] bytes = Files.readAllBytes(new File("testframe_constant.html").toPath());
			response.getOutputStream().write(bytes);
			response.flushBuffer();
		}
		
		else if(flag.equals("testframe_local"))
		{
			byte[] bytes = Files.readAllBytes(new File("testframe_local.html").toPath());
			response.getOutputStream().write(bytes);
			response.flushBuffer();
		}
		
		
		
		else if(flag.equals("tg"))
		{
			
			//System.out.println(new File(".").getCanonicalPath());
			String fileName = request.getParameter("fileName");
		
			if(fileName == null)
			{
				response.getWriter().write("file name missing");
				response.flushBuffer();
			}
			if(!new File(fileName).exists())
			{
				response.getWriter().write("invalid FileName");
				response.flushBuffer();
				return;
			}
			
			String absolutePath = new File(fileName).getAbsolutePath();
			String filePath = absolutePath.
				    substring(0,absolutePath.lastIndexOf(File.separator));
			
			//System.out.println(filePath);
			if(!filePath.equals(new File(".").getCanonicalPath()))
			{
				response.getWriter().write("invalid FileName");
				response.flushBuffer();
			}
			else
			{
				if(!new File(fileName).exists())
				{
					response.getWriter().write("invalid FileName");
					response.flushBuffer();
				}
				else
				{
					byte[] bytes = Files.readAllBytes(new File(fileName).toPath());
					response.getOutputStream().write(bytes);
					response.flushBuffer();
				}
			}
		}


		else if(flag.equals("rand"))
		{
			ResponseUtil.rand(request, response);
		}

		else if(flag.equals("randB"))
		{
			ResponseUtil.randB(request, response);
		}

		else if(flag.equals("broadCast"))
		{
			//ResponseUtil.broadCast(request, response, this.broadCastMessage);
		}

		//request for sending broadcast json
		else if(flag.equals("broadCastjson"))
		{
			//ResponseUtil.broadCastjson(request, response, this.broadCastMessage, this.publicKey, this.privateKey);
		}


		//request for the sitemap table
		else if(flag.equals("tablePlease"))
		{
			ResponseUtil.tablePlease(request, response, this.privateKey);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}


		else if(flag.equals("dropletPlease"))
		{
			//String postBody = ServerUtil.GetBody(request);

			BufferedReader payloadReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

			String st = new String();
			StringBuffer stb = new StringBuffer("");

			while((st = payloadReader.readLine())!= null)
				stb.append(st);

			String postBody = stb.toString();

			//System.out.println("BODY : " + postBody);

			if(postBody == null || postBody.length() == 0)
			{
				try
				{
					ResponseUtil.dropletPlease(request, response, this.privateKey);
				}
				catch(IOException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			else if(postBody.startsWith("0"))
				ResponseUtil.dropletPlease(request, response, this.privateKey);

			else if(postBody.startsWith("1"))
				ResponseUtil.dropletPleaseIntr(request, response, this.privateKey,postBody);

			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");

		}

		else if(flag.equals("tablePleaseBin"))
		{
			ResponseUtilBin.tablePleaseBin(request, response, this.privateKey);
			//System.out.println(0);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}

		else if(flag.equals("dropletPleaseBin"))
		{
			BufferedReader payloadReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

			String st = new String();
			StringBuffer stb = new StringBuffer("");

			while((st = payloadReader.readLine())!= null)
				stb.append(st);

			String postBody = stb.toString();

			System.out.println("BODY : " + postBody);

			if(postBody == null || postBody.length() == 0)
			{
				try
				{
					ResponseUtilBin.dropletPleaseBin(request, response, this.privateKey, false);
				}
				catch(IOException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			else if(postBody.startsWith("0"))
				ResponseUtilBin.dropletPleaseBin(request, response, this.privateKey, false);

			else if(postBody.startsWith("1"))
				ResponseUtilBinHP.dropletPleaseIntrBin(request, response, this.privateKey, postBody);

			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			//System.out.println(1);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}

		else if(flag.equals("slicePleaseBin"))
		{
			ResponseUtilBinHP.slicePleaseBin(request, response, privateKey, null);

			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}

		else if(flag.equals("dropletPleaseBin_1"))
		{
			ResponseUtilBin.dropletPleaseBin(request, response, this.privateKey, true);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}

		//the fake one
		else if(flag.equals("dropletPleaseBinFake"))
		{
			ResponseUtilBin.dropletPleaseBin(request, response, this.privateKey, true);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
			//System.out.println(3);
		}	

		else if(flag.equals("dropletPleaseBinProb"))
		{
			byte[] postBody = IOUtils.toByteArray(request.getInputStream());

			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
			if(sslId == null)
			{
				response.getWriter().append("Non TLS/SSL connection terminated");
				response.flushBuffer();
				return;
			}

			byte[] randAESkey = new byte[16];
			byte[] randAESiv = new byte[16];
			SecureRandom rand = new SecureRandom();
			rand.nextBytes(randAESkey);

			if(postBody == null || postBody.length == 0)
			{
				try
				{
					ResponseUtilBinProb.dropletPleaseBin(request, response, this.privateKey, randAESkey, randAESiv);
				}
				catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			else if(postBody[0] == 0x00)
				try {
					ResponseUtilBinProb.dropletPleaseBin(request, response, this.privateKey, randAESkey, randAESiv);
				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();

					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					rand.nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			else if(postBody[0] == 0x01)
				try {
					ResponseUtilBinProb.dropletPleaseIntrBin(request, response, this.privateKey, randAESkey, randAESiv, postBody);

				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();

					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					rand.nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			//System.out.println(1);
			//System.out.println(flag + " " + request.getRemoteAddr());
			//System.out.println(charC[C]);
			//System.out.println("-------------------------------------");
		}

		//TODO interactive
		//constant time response
		//this only supports interactive
		else if(flag.equals("dropletPleaseBinConst"))
		{
			response.setContentType("text/plain");
			byte[] postBody = IOUtils.toByteArray(request.getInputStream());
			//System.out.println(new String(postBody));
			//postBody = Base64.getDecoder().decode(new String(postBody));

			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
			if(sslId == null)
			{
				response.getWriter().append("Non TLS/SSL connection terminated");
				response.flushBuffer();
				return;
			}
			//broadcast
			if(postBody == null || postBody.length == 0)
			{
				try
				{
					ResponseUtilBinConstantTimeBrowsing.dropletPleaseBin(request, response, this.privateKey);
				}
				catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			//broadcast
			else if(postBody[0] == 0x00)
			{
				try {
					ResponseUtilBinConstantTimeBrowsing.dropletPleaseBin(request, response, this.privateKey);
				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();

					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			}
			//interactive
			else if(postBody[0] == 0x01)
			{
				try {
					ResponseUtilBinConstantTimeBrowsing.dropletPleaseIntrBin(request, response, this.privateKey, postBody);

				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();

					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			}

			else if(postBody[0] == 0x02)
			{
				try {
					ResponseUtilBinConstantTimeChat.dropletPleaseChatBin(request, response, postBody);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			}

			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			//System.out.println(1);
			/*System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");*/
		}

		//TODO chat
		//constant time response
		//this only supports chat
		else if(flag.equals("dropletPleaseBinConstChat"))
		{
			//System.out.println("Req::");
			byte[] postBody = IOUtils.toByteArray(request.getInputStream());

			
			//System.out.println(new String(postBody));
			try
			{
				postBody = Base64.getDecoder().decode(new String(postBody));
			}
			catch(Exception ex)
			{
				postBody = null;
			}
			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
			if(sslId == null)
			{
				response.getWriter().append("Non TLS/SSL connection terminated");
				response.flushBuffer();
				return;
			}
			//broadcast
			if(postBody == null || postBody.length == 0)
			{
				try
				{
					ResponseUtilBinConstantTimeChat.dropletPleaseBin(request, response, this.privateKey);
				}
				catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			//broadcast
			else if(postBody[0] == 0x00)
			{
				try {
					ResponseUtilBinConstantTimeChat.dropletPleaseBin(request, response, this.privateKey);
				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();

					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			}

			//chat
			else if(postBody[0] == 0x02)
			{
				try {
					ResponseUtilBinConstantTimeChat.dropletPleaseChatBin(request, response, postBody);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			}
			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			//System.out.println(1);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}



		////////////////////////////////////////
		//Mixed Mode -> intr/char
		/////////////////////////////////////////

		else if(flag.equals("dropletPleaseMix"))
		{
			byte[] postBody = IOUtils.toByteArray(request.getInputStream());

			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
			if(sslId == null)
			{
				response.getWriter().append("Non TLS/SSL connection terminated");
				response.flushBuffer();
				return;
			}
			//This branch will serve chat
			if(Math.random() <= 0.5)
			{
				if(postBody == null || postBody.length == 0)
				{
					try
					{
						ResponseUtilBinConstantTimeChat.dropletPleaseBin(request, response, this.privateKey);
					}
					catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
							InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex)
					{
						response.getWriter().append(ex.getMessage());
						response.flushBuffer();
					}
				}
				//broadcast
				else if(postBody[0] == 0x00)
				{
					try {
						ResponseUtilBinConstantTimeChat.dropletPleaseBin(request, response, this.privateKey);
					} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
							InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
						e.printStackTrace();

						byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
						new SecureRandom().nextBytes(ret);
						ServletOutputStream out = response.getOutputStream();
						out.write(ret);
						out.flush();
						out.close();
						response.flushBuffer();
					}
				}
			}
			//This branch will serve interactive browsing
			else
			{
				if(postBody == null || postBody.length == 0)
				{
					try
					{
						ResponseUtilBinConstantTimeBrowsing.dropletPleaseBin(request, response, this.privateKey);
					}
					catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
							InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex)
					{
						response.getWriter().append(ex.getMessage());
						response.flushBuffer();
					}
				}
				//broadcast
				else if(postBody[0] == 0x00)
				{
					try {
						ResponseUtilBinConstantTimeBrowsing.dropletPleaseBin(request, response, this.privateKey);
					} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
							InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
						e.printStackTrace();

						byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
						new SecureRandom().nextBytes(ret);
						ServletOutputStream out = response.getOutputStream();
						out.write(ret);
						out.flush();
						out.close();
						response.flushBuffer();
					}
				}
			}

			//intr
			if(postBody[0] == 0x01)
			{
				try {
					ResponseUtilBinConstantTimeBrowsing.dropletPleaseIntrBin(request, response, this.privateKey, postBody);

				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();

					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			}
			//chat
			else if(postBody[0] == 0x02)
			{
				try {
					ResponseUtilBinConstantTimeChat.dropletPleaseChatBin(request, response, postBody);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			}
			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			//System.out.println(1);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}
		
		else if(flag.equals("chatBroadcastPlease"))
		{
			BufferedReader payloadReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

			String st = new String();
			StringBuffer stb = new StringBuffer("");

			while((st = payloadReader.readLine())!= null)
				stb.append(st);

			String postBodyStr = stb.toString();
			
			//Base64 decoding is for the testing only
			byte[] postBody = null;
			try{
				postBody = Base64.getDecoder().decode(postBodyStr);
			}
			catch(Exception ex)
			{
				postBody = IOUtils.toByteArray(request.getInputStream());
			}
			
			ResponseUtilBinBroadcast.BroadcastBin(request, response, postBody, privateKey);
		}

		else if(flag.equals("end"))
		{
			Stats.LIVE_CONNECTIONS--;

			response.getWriter().append("Connection terminated");
			response.flushBuffer();
		}

		else
		{
			String fileName = request.getParameter("Index//index.html");
			try
			{
				byte[] bytes = Files.readAllBytes(new File(fileName).toPath());
				response.getOutputStream().write(bytes);
				response.flushBuffer();
			}
			catch(Exception ex)
			{
				response.getWriter().append("Wrong url");
				response.flushBuffer();
			}
			//response.getWriter().append("Wrong url");
			//response.flushBuffer();
		}
	}
}
