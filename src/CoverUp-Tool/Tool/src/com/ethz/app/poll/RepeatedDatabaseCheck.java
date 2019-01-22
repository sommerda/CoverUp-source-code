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

package com.ethz.app.poll;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sqlite.SQLiteConfig;
import org.whispersystems.curve25519.Curve25519;

import com.ethz.app.binUtils.BinUtils;
import com.ethz.app.chatApp.ChatApp;
import com.ethz.app.chatApp.ChatUtils;
import com.ethz.app.dbUtils.FirefoxCacheExtract;
import com.ethz.app.dbUtils.TableChecker;
import com.ethz.app.env.ENV;

/**
 * I wrote this class a while ago and it is complicated.
 * It is so complicated that right now I am too afraid to look at it.
 * @author Aritra
 *
 */
public class RepeatedDatabaseCheck {

	public static int count = 0;

	public static byte[] ServerPublickey;
	StringBuffer messaage;

	public static String changedDBLoc = "";
	public String modifiedDatabaseLocation;


	/**
	 * Entry for the database check. Goes through the DB and checks for table and fountain/interactive data. Supports both single and multiple entry server provider. 
	 * @param modifiedDatabaseLocation
	 * @throws SQLException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public RepeatedDatabaseCheck(String modifiedDatabaseLocation) throws SQLException, IOException, 
	IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, 
	NoSuchPaddingException, InvalidAlgorithmParameterException 
	{
		this.modifiedDatabaseLocation = modifiedDatabaseLocation;
		//System.out.println("Run");

		this.messaage = new StringBuffer();
		boolean tableSuccess = false;

		try
		{
			tableSuccess = this.doTableCheck();
		}
		catch(SQLException ex)
		{
			tableSuccess = false;
		}

		if(tableSuccess)
		{
			if(!ENV.MULTIPLE_PROVIDER_SUPPORT)
				doDataBaseCheck();
			else
				doDataBaseCheckMultipleProvider();
		}

		else
		{
			this.messaage.append("Table error");
			count %= 4;
			this.messaage.append("\n---------------" + ENV.PROGRESS_SYMB[count++] + "---------------");
		}

	}


	private boolean doTableCheck() throws SQLException
	{
		TableChecker tabCheck = new TableChecker();

		try
		{	
			if(!ENV.MULTIPLE_PROVIDER_SUPPORT)
				tabCheck.loadtableData();
			else
				tabCheck.loadtableDataMultipleProvider();
		}
		catch(RuntimeException ex1)
		{
			if(ex1.getMessage().equals(ENV.EXCEPTION_FOUNTAIN_TABLE_MISSING))
				throw new RuntimeException(ENV.EXCEPTION_FOUNTAIN_TABLE_MISSING);
		}
		catch(Exception ex)
		{
			return false;
		}

		return true;
	}
	private void doDataBaseCheck() throws SQLException, IOException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException
	{
		FirefoxCacheExtract ffce = new FirefoxCacheExtract();
		ffce.getFirefoxCacheFile(this.modifiedDatabaseLocation);
		String jsonData = ffce.connectDatabase(ENV.DATABASE_DROPLET_COL, this.modifiedDatabaseLocation);

		try
		{
			JSONObject jObject = new JSONObject(jsonData);
			doDataBaseCheck(jObject);
		}
		//in case it is binary data
		catch(JSONException ex)
		{
			doDataBaseCheckBin(jsonData);
		}

	}

	public static byte[] lastReadFileHash = null;
	public static volatile int stored_droplet_counter = 0;
	public static String lastChatHash = null;
	/**
	 * This the case where the app inds the data in binary format
	 * @param jsonData
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	synchronized private void doDataBaseCheckBin(String jsonData) throws IOException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException
	{
		//System.err.println("here");
		String jsonBinData = null;
		byte[] receivedBin = Base64.getDecoder().decode(jsonData);
		//System.out.println(receivedBin.length);

		try
		{
			jsonBinData = BinUtils.dropletBinToDropletJson
					(receivedBin, RepeatedDatabaseCheck.ServerPublickey, this.messaage);	
			
			//doDataBaseCheck(new JSONObject(jsonBinData));

		}
		catch(RuntimeException ex)
		{
			//ex.printStackTrace();
			if(ex.getMessage() == null)
			{
				this.messaage.append("Something went horribly wrong");
				return;
			}
			else if(ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_GARBAGE_PACKET))
			{
				FileWriter fw = new FileWriter("garbage.txt");
				fw.append(Base64.getEncoder().encodeToString(receivedBin));
				fw.flush();
				fw.close();
				this.messaage.append("Data not according to spec: garbage, garbage dumped");
				return;
			}
			else if(ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_INTR_MESSAGE_MAGIC_BYTES))
			{
				this.messaage.append("Encrypted slice found\n");
				Object[] returnVal = BinUtils.intrBinProcess(receivedBin, this.messaage);
				/**
				 * 1. slice id in long
				 * 2. slice index in integer
				 * 3. slice data in byte array
				 */
				long sliceId = (long) returnVal[0];
				int sliceIndex =(int) returnVal[1];
				byte[] intrDataBytes = (byte[]) returnVal[2];
				byte[] hashtBytes = null;
				try 
				{		
					MessageDigest md = MessageDigest.getInstance("SHA-256");
					hashtBytes = md.digest(intrDataBytes);
				}
				catch(NoSuchAlgorithmException exH)
				{
					exH.printStackTrace();
				}
				if(lastReadFileHash == null)
				{
					lastReadFileHash = new byte[hashtBytes.length];
					System.arraycopy(hashtBytes, 0, lastReadFileHash, 0, hashtBytes.length);


					//save the slice files in .slice format and the data in it in base64 encode format to stay consistent as the server data format
					String sliceDirLocation = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_INTERACTIVE_DATA + ENV.DELIM + sliceId;
					if(!new File(sliceDirLocation).exists())
						new File(sliceDirLocation).mkdir();
					String sliceFileLocation = sliceDirLocation + ENV.DELIM + sliceIndex + ENV.APP_STORAGE_SLICE_FILE_FORMAT;

					FileWriter fw = new FileWriter(sliceFileLocation);
					fw.append(Base64.getEncoder().encodeToString(intrDataBytes));
					fw.close();

					this.messaage.append("\n Interactive data dumped in local storage");
					this.messaage.append("\n Dump location : " + sliceFileLocation);		
					return;
				}
				else if(Arrays.equals(lastReadFileHash, hashtBytes))
				{
					this.messaage.append("\nInteractive data with hash " + Base64.getMimeEncoder().encodeToString(lastReadFileHash) + " exists");
					this.messaage.append("\n Skipped...");
					return;
				}

				else
				{
					lastReadFileHash = new byte[hashtBytes.length];
					System.arraycopy(hashtBytes, 0, lastReadFileHash, 0, hashtBytes.length);

					//save the slice files in .slice format and the data in it in base64 encode format to stay consistent as the server data format
					String sliceDirLocation = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_INTERACTIVE_DATA + ENV.DELIM + sliceId;
					if(!new File(sliceDirLocation).exists())
						new File(sliceDirLocation).mkdir();
					String sliceFileLocation = sliceDirLocation + ENV.DELIM + sliceIndex + ENV.APP_STORAGE_SLICE_FILE_FORMAT;

					FileWriter fw = new FileWriter(sliceFileLocation);
					fw.append(Base64.getEncoder().encodeToString(intrDataBytes));
					fw.close();

					this.messaage.append("\n Interactive data dumped in local storage");
					this.messaage.append("\n Dump location : " + sliceFileLocation);					
				}
				count %= 4;
				this.messaage.append("\n---------------" + ENV.PROGRESS_SYMB[count++] + "---------------");
				return;
			}
			else if(ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_CHAT_MESSAGE_MAGIC_BYTES))
			{
				this.messaage.append("Got chat message");
				String[] data = null;
				try
				{
					/*
					 * index 0 -> sender public address
					 * index 1 -> decrypted chat data
					 * index 2 -> data hash for database indexing
					 */
					data = BinUtils.chatMessageBinProcess(receivedBin);
				}
				catch(RuntimeException ex_1)
				{
					if(ex_1.getMessage().equals(ENV.EXCEPTION_CHAT_SIGNATURE_ERROR))
					{
						this.messaage.append("\n Chat signature error");
						return;
					}
					else
					{
						this.messaage.append("\n" + ex_1.getMessage());
						return;
					}
				}
				catch (Exception ex_2) {
					this.messaage.append("\n Problem in chat crypto part");
					return;
				}
				if(data == null)
				{

					this.messaage.append("\n Remote public key is not listed in local storage. Skipped");					
					return;
				}

				//good
				String senderAddress = data[0];
				String storeData = data[1];
				String dataSig = data[2];

				boolean poll = false;
				if(lastChatHash == null)
				{
					lastChatHash = dataSig;
					poll = true;
				}
				else if(!lastChatHash.equals(dataSig))
					poll = true;
				else
					poll = false;

				if(poll)
				{
					Connection c = null;
					try 
					{					
						Class.forName("org.sqlite.JDBC");
						c = DriverManager.getConnection("jdbc:sqlite:" + ENV.APP_STORAGE_INCOMING_CHAT_DATABASE_FILE);
					} 
					catch ( Exception e ) 
					{
						System.err.println( e.getClass().getName() + ": " + e.getMessage() );
						return;
					}

					Statement stmt;
					try {
						stmt = c.createStatement();
						ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM incoming_chat WHERE signature = \'" + dataSig + "\';" );
						int size = 0;

						while(rs.next())
							size = rs.getInt(1);

						if(size == 0)
						{
							try
							{					
								// if the chat has ; problem. Encode it to base 64
								stmt.executeUpdate("INSERT INTO incoming_chat (sender,data,signature) VALUES (\'" 
										+ senderAddress + "\',\'" + Base64.getMimeEncoder().encodeToString(storeData.getBytes(StandardCharsets.UTF_8))
										+ "\',\'" + dataSig +"\');" );
								//stmt.executeUpdate("INSERT INTO incoming_chat (sender,data,signature) VALUES ('bla', 'bla', '5');" );
								this.messaage.append("\nChat with signature : " + dataSig + " inserted into the database\n-----------------------------------------------");

							}
							catch(SQLException ex1)
							{
								if(ex1.getMessage().contains("UNIQUE constraint failed"))
									this.messaage.append("\n Chat with signature : " + dataSig + " exists skipped ... \n-----------------------------------------------");
								else
									ex1.printStackTrace();
							}					
						}
						else
							this.messaage.append("\nChat with signature : " + dataSig + " exists skipped ... \n-----------------------------------------------");

						stmt.close();
						rs.close();
						c.close();
						return;

					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
				}
				else
				{
					this.messaage.append("\nChat with signature : " + dataSig + " exists skipped ... \n-----------------------------------------------");
					return;
				}


			}
			else
			{
				this.messaage.append(ex.getMessage());
				return;
			}
		}


		JSONObject jObject = new JSONObject(jsonBinData);

		String droplet = jObject.getString("droplet");
		String dropletUrl = jObject.getString("url");

		JSONObject tableJSONData = TableChecker.URL_JSON_TABLE_MAP.get(dropletUrl);
		if(tableJSONData == null)
			throw new RuntimeException(ENV.EXCEPTION_FOUNTAIN_TABLE_MISSING);
		
		String dropletLocation =  tableJSONData.get("dropletLoc").toString();

		this.messaage.append("\ndroplet location : ").append(dropletLocation);

		String dropletStr = droplet.concat(dropletUrl);
		byte[] dropletByte = dropletStr.getBytes(StandardCharsets.UTF_8);

		byte[] hashtableBytes = null;
		try 
		{		
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			hashtableBytes = md.digest(dropletByte);
		}
		catch(NoSuchAlgorithmException ex)
		{
			ex.printStackTrace();
		}


		String fileName = ENV.APP_STORAGE_LOC + ENV.DELIM + dropletLocation + ENV.DELIM + Base64.getUrlEncoder().encodeToString(hashtableBytes).concat(".txt");
		String dropletUrlFileName =  ENV.APP_STORAGE_LOC + ENV.DELIM + dropletLocation + ENV.DELIM + ENV.APP_STORAGE_DROPLET_URL;

		if(!new File(ENV.APP_STORAGE_LOC + ENV.DELIM + dropletLocation).exists())
			new File(ENV.APP_STORAGE_LOC + ENV.DELIM + dropletLocation).mkdir();


		File file = new File(fileName);
		File dropletUrlFile = new File(dropletUrlFileName);

		if(!file.exists())
		{
			FileWriter fw = new FileWriter(fileName);
			fw.write(droplet);
			this.messaage.append("\n Droplet dumped in local storage");
			this.messaage.append("\n Dump location : " + fileName);
			this.messaage.append("\n Droplet id : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));
			System.out.println(System.currentTimeMillis());
			fw.close();

			stored_droplet_counter++;
		}
		else
		{
			this.messaage.append("\n Droplet id : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));
			this.messaage.append("\n Droplet exists in loal Storage. Skipped...");
		}

		if(!dropletUrlFile.exists())
		{
			FileWriter fwUrl = new FileWriter(dropletUrlFile);
			fwUrl.append(dropletUrl);
			fwUrl.close();
		}


		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//start of broadcast chat handle
		//now put the chats to db
		if(jObject.has("message"))
		{
			JSONArray messageArray = jObject.getJSONArray("message");
			List<String[]> messageList = BinUtils.processBroadcastChatMessages(messageArray);

			for(String[] data : messageList)
			{
				String senderAddress = data[0];
				String storeData = data[1];
				String dataSig = data[2];
				boolean poll = false;
				if(lastChatHash == null)
				{
					lastChatHash = dataSig;
					poll = true;
				}
				else if(!lastChatHash.equals(dataSig))
					poll = true;
				else
					poll = false;

				if(poll)
				{
					Connection c = null;
					try 
					{					
						Class.forName("org.sqlite.JDBC");
						c = DriverManager.getConnection("jdbc:sqlite:" + ENV.APP_STORAGE_INCOMING_CHAT_DATABASE_FILE);
					} 
					catch ( Exception e ) 
					{
						System.err.println( e.getClass().getName() + ": " + e.getMessage() );
						return;
					}

					Statement stmt;
					try {
						stmt = c.createStatement();
						ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM incoming_chat WHERE signature = \'" + dataSig + "\';" );
						int size = 0;

						while(rs.next())
							size = rs.getInt(1);

						if(size == 0)
						{
							try
							{					
								// if the chat has ; problem. Encode it to base 64
								stmt.executeUpdate("INSERT INTO incoming_chat (sender,data,signature) VALUES (\'" 
										+ senderAddress + "\',\'" + Base64.getMimeEncoder().encodeToString(storeData.getBytes(StandardCharsets.UTF_8))
										+ "\',\'" + dataSig +"\');" );
								//stmt.executeUpdate("INSERT INTO incoming_chat (sender,data,signature) VALUES ('bla', 'bla', '5');" );
								this.messaage.append("\nChat with signature : " + dataSig + " inserted into the database\n-----------------------------------------------");

							}
							catch(SQLException ex1)
							{
								if(ex1.getMessage().contains("UNIQUE constraint failed"))
									this.messaage.append("\n Chat with signature : " + dataSig + " exists skipped ... \n-----------------------------------------------");
								else
									ex1.printStackTrace();
							}					
						}
						else
							this.messaage.append("\nChat with signature : " + dataSig + " exists skipped ... \n-----------------------------------------------");

						stmt.close();
						rs.close();
						c.close();
						return;

					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
				}

			}
		}
		//end of broadcast chat
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		count %= 4;
		this.messaage.append("\n---------------" + ENV.PROGRESS_SYMB[count++] + "---------------");
	}



	/**
	 * For json operations only. Not used right now.
	 * @param jObject
	 * @throws IOException
	 */
	private void doDataBaseCheck(JSONObject jObject) throws IOException
	{
		/*FirefoxCacheExtract ffce = new FirefoxCacheExtract();
		ffce.getFirefoxCacheFile(this.modifiedDatabaseLocation);
		String jsonData = ffce.conncetDatabase(ENV.DATABASE_DROPLET, this.modifiedDatabaseLocation);

		JSONObject jObject = new JSONObject(jsonData);*/

		//System.err.println(jObject.toString(2));

		String droplet = jObject.getString("droplet");
		String dropletUrl = jObject.getString("url");

		String dropletStr = droplet.concat(dropletUrl);

		byte[] dropletByte = dropletStr.getBytes(StandardCharsets.UTF_8);
		String signatureString = jObject.getString("signature");
		byte[] signatureBytes = Base64.getUrlDecoder().decode(signatureString);

		this.messaage.append("Droplet length : ").append(dropletByte.length);

		byte[] hashtableBytes = null;
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			hashtableBytes = md.digest(dropletByte);

			//System.out.println("hash " + Base64.getUrlEncoder().encodeToString(hashtableBytes));

			boolean check = Curve25519.getInstance("best").verifySignature(RepeatedDatabaseCheck.ServerPublickey, hashtableBytes, signatureBytes);

			if(!check)
			{
				this.messaage.append("\n Signature verification failed");
				throw new RuntimeException("Signature verification failed");
			}
			else
			{
				this.messaage.append("\n Signature verification success");
			}
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}

		JSONObject tableJSONData = TableChecker.URL_JSON_TABLE_MAP.get(dropletUrl);
		String dropletLocation =  tableJSONData.get("dropletLoc").toString();

		this.messaage.append("\n droplet location : ").append(dropletLocation);

		String fileName = ENV.APP_STORAGE_LOC + ENV.DELIM + dropletLocation + ENV.DELIM + Base64.getUrlEncoder().encodeToString(hashtableBytes).concat(".txt");
		String dropletUrlFileName =  ENV.APP_STORAGE_LOC + ENV.DELIM + dropletLocation + ENV.DELIM + ENV.APP_STORAGE_DROPLET_URL;

		if(!new File(ENV.APP_STORAGE_LOC + ENV.DELIM + dropletLocation).exists())
		{
			new File(ENV.APP_STORAGE_LOC + ENV.DELIM + dropletLocation).mkdir();
		}

		File file = new File(fileName);
		File dropletUrlFile = new File(dropletUrlFileName);

		if(!file.exists())
		{
			FileWriter fw = new FileWriter(fileName);
			fw.write(droplet);
			this.messaage.append("\n Droplet dumped in local storage");
			this.messaage.append("\n Droplet id : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));
			fw.close();
		}
		else
		{
			this.messaage.append("\n Droplet id : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));
			this.messaage.append("\n Droplet exists in loal Storage. Skipped...");
		}

		if(!dropletUrlFile.exists())
		{
			FileWriter fwUrl = new FileWriter(dropletUrlFile);
			fwUrl.append(dropletUrl);
			fwUrl.close();
		}

		count %= 4;
		this.messaage.append("\n---------------" + ENV.PROGRESS_SYMB[count++] + "---------------");
	}

	private void doDataBaseCheckMultipleProvider() throws SQLException, IOException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException
	{
		FirefoxCacheExtract ffce = new FirefoxCacheExtract();
		ffce.getFirefoxCacheFile(this.modifiedDatabaseLocation);
		List<String[]> rows = ffce.connectDatabaseMultipleProvider(ENV.DATABASE_DROPLET_COL, this.modifiedDatabaseLocation);

		for(String[] row : rows)
		{
			JSONObject jObject = null;
			try
			{
				jObject = new JSONObject(row[0]);
				doDataBaseCheck(jObject);
			}
			catch(JSONException ex)
			{
				try {
					this.doDataBaseCheckBin(row[0]);
				} catch(Exception ex1) {
					if(ex1.getMessage().equals(ENV.EXCEPTION_FOUNTAIN_TABLE_MISSING))
						this.messaage.append("\n Fountain table missing from database");
					continue;
				}
			}
			catch(Exception ex1){
				continue;
			}

		}
	}


}
