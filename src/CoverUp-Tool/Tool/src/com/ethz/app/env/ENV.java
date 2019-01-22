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

package com.ethz.app.env;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Delayed;

import javax.print.DocFlavor.STRING;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class ENV {

	static
	{
		ASCIIart.asciiART();
		System.out.println("\n\n=====================================");
	}
	
	public static String DELIM;
	static
	{
		String OS = System.getProperty("os.name");
		if(OS.contains("windows"))
			DELIM = "\\";

		else
			DELIM = "/";
	}
	
	public static boolean isWindows, isLinux, isMac;
	static
	{
		isWindows = isLinux = isMac = false;
		String OS = System.getProperty("os.name").toLowerCase();
		if(OS.contains("windows"))
			isWindows = true;

		else if(OS.contains("linux") || OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 )
			isLinux = true;
		else
			isMac = true;
			
	}
	
	public static final String OPERATING_SYSTEM_NAME =  System.getProperty("os.name");
	public static final String OPERATING_SYSTEM_ARCH =  System.getProperty("os.arch");

	public static final String APP_STORAGE_LOC = "APP_DATA";
	static
	{
		File file = new File(APP_STORAGE_LOC);
		if(!file.exists())
			file.mkdir();
	}
	
	public static boolean macSupport = false;
	public static boolean AUTO_PILOT = false;

	public static final String APP_STORAGE_BROWSER_COMM_DROPLET_LOC = "DROPLET";
	public static final String APP_STORAGE_BROWSER_COMM_DROPLET_BIN_LOC = "DROPLET_BIN";

	public static final String APP_STORAGE_COMPLETED_DROPLET_FILE = "info.txt";
	public static final String APP_STORAGE_DROPLET_URL = "dropletUrl.txt";
	public static final String APP_STORAGE_COMPLETE_DATA = "data.txt";
	public static final String APP_STORAGE_COMPLETE_DATA_AON = "data_dec.txt";
	public static final String APP_STORAGE_TABLE_DUMP = "table.json";
	public static final String APP_STORAGE_TABLE_MULTIPLE_PROVIDER_DUMP = "table";
	public static final String APP_STORAGE_KEY_FILE = "key.bin";
	public static final String APP_STORAGE_INTERACTIVE_DATA = "Interactive";
	public static final String APP_STORAGE_SLICE_TABLE_LOC = "SLICE_TABLE";
	public static final String APP_STORAGE_SLICE_TABLE = "sliceTable.txt";
	public static final String APP_STORAGE_COVERT_BROWSER_START_PAGE = "sliceTableHTML.htm";
	public static final String APP_STORAGE_SLICE_ID_FILES_LOC = "SLICE_ID";
	public static final String APP_STORAGE_SLICE_ID_FILE = "slice_id.bin";
	public static final String APP_STORAGE_SLICE_FILE_FORMAT = ".slice";
	
	public static final String APP_STORATE_DROPLET_STORE_LOCATION = "DROPLET";
	//chat stuff
	public static final String APP_STORAGE_CHAT_LOC = "Chat";
	public static final String APP_STORAGE_CHAT_DISPATCH_LOC = "Dispatch";
	public static final String APP_STORAGE_CHAT_LOG_LOC = "LOGS";
	public static final String APP_STORAGE_CHAT_REPO_FILE = "CHATLOG.log";

	public static final String APP_STORAGE_CHAT_DISPATCH_FILE = "CHAT.bin";
	public static final String APP_STORAGE_ENC_CHAT_DISPATCH_FILE = "CHAT_ENC.bin";
	
	//native message
	
	public static final String NATIVE_RESOURCE_MESSAGE_JSON_FILE = "/native_comm.json";
	public static final String NATIVE_RESOURCE_MESSAGE_PYTHON_FILE = "/native-messaging-example-host";
	public static final String NATIVE_MESSAGE_LOCATION = APP_STORAGE_LOC + DELIM + "native_messaging";
	
	public static final String NATIVE_MESSAGE_JSON_FILE = NATIVE_MESSAGE_LOCATION + DELIM + "native_comm.json";
	public static final String NATIVE_MESSAGE_PYTHON_FILE = NATIVE_MESSAGE_LOCATION + DELIM + "native-messaging-example-host";
	public static final String NATIVE_MESSAGE_BAT_FILE = NATIVE_MESSAGE_LOCATION + DELIM + "native_ext.bat";

	//////////////////////////////////////////
	
	public static final String APP_STORAGE_INCOMING_CHAT_DATABASE_FILE = APP_STORAGE_LOC + DELIM
			+ APP_STORAGE_CHAT_LOC + DELIM 
			+ "INCOMING_CHAT.db";

	public static final String REPLICATED_CHROME_DB = APP_STORAGE_LOC + DELIM + "webappsstore.sqlite";
	
	//native message
	public static final String APP_STORAGE_NATIVE_MESSAGE_LOC = "NATIVE_MASSAGE";
	public static final String REPLICATED_NATIVE_MESSGAE_DB = APP_STORAGE_LOC + DELIM + APP_STORAGE_NATIVE_MESSAGE_LOC + DELIM + "webappsstore.sqlite";
	

	public static final String BROWSER_FIREFOX = "BROWSER_FIREFOX";
	public static final String BROWSER_CHROME = "BROWSER_CHROME";
	public static final String BROWSER_NATIVE_MESSAGE = "BROWSER_NATIVE_MESSAGE";
	public static final int NATIVE_MESSAGE_LISTER_SERVER_PORT = 56789;
	
	//magic bytes
	public static final byte INTR_MAGIC_BYTE = (byte)0x06;
	public static final int INTR_MAGIC_BYTES_LEN = 8;

	public static final byte CHAT_MAGIC_BYTES = (byte)0x0A;
	public static final int CHAT_MAGIC_BYTES_LEN = 8;

	public static final byte BROADCAST_CHAT_MAGIC_BYTES = (byte)0x0B;
	public static final int BROADCAST_CHAT_MAGIC_BYTES_LEN = 16;
	//magic byte ends

	public static final int CHAT_PUBLIC_ADDRESS_LEN = 8;
	public static final int CHAT_POLLING_RATE = 775;
	public static final boolean CHAT_ACCEPT_UNKOWN_PUBLIC_KEY = true;
	
		
	public static final int FIXED_CHAT_LEN = 512;
	public static final int FIXED_ENC_CHAT_PACK_LEN = FIXED_CHAT_LEN - 64 - 16 -16; //64 for signature and 16 for IV (pad the plain text to make it this size), last 16 is for the AES padding
	public static final int FIXED_CHAT_TYPE_LEN = FIXED_ENC_CHAT_PACK_LEN - CHAT_PUBLIC_ADDRESS_LEN - BROADCAST_CHAT_MAGIC_BYTES_LEN - 4 - 4; //last 4 is to make it divisible by 16
	
	

	public static final String APP_STORAGE_CHAT_KEY_FILE = APP_STORAGE_LOC + DELIM + APP_STORAGE_CHAT_LOC + DELIM + "KeyFile.key";
	public static final String APP_STORAGE_PUBLIC_KEY_LIST = APP_STORAGE_LOC + DELIM + APP_STORAGE_CHAT_LOC + DELIM + "pkList.txt";
	public static final String CHROME_BASE_DIR_LOCATION_CONFIG_FILE = APP_STORAGE_LOC + DELIM + "chromeBaseDirConfigFile.conf";
	public static final String FIREFOX_BASE_DIR_LOCATION_CONFIG_FILE = APP_STORAGE_LOC + DELIM + "firefoxBaseDirConfigFile.conf";
	public static boolean BROWSER_BASE_DIR_LOCATION_CONFIG_FILE_EXISTS = false;

	public static String _CHROME_BASE_FILE;
	public static String _FIREFOX_BASE_FILE;

	static
	{

		File fileI = new File(APP_STORAGE_LOC + DELIM + APP_STORAGE_INTERACTIVE_DATA);
		if(!fileI.exists())
			fileI.mkdir();
		File fileSlice = new File(APP_STORAGE_LOC + DELIM + APP_STORAGE_SLICE_TABLE_LOC);
		if(!fileSlice.exists())
			fileSlice.mkdir();
		File fileSliceID = new File(APP_STORAGE_LOC + DELIM + APP_STORAGE_SLICE_ID_FILES_LOC);
		if(!fileSliceID.exists())
			fileSliceID.mkdir();

		File FileChatLoc = new File(APP_STORAGE_LOC + DELIM + APP_STORAGE_CHAT_LOC);
		if(!FileChatLoc.exists())
			FileChatLoc.mkdir();
		File FileChatDispatch = new File(APP_STORAGE_LOC + DELIM + APP_STORAGE_CHAT_LOC + DELIM + APP_STORAGE_CHAT_DISPATCH_LOC);
		if(!FileChatDispatch.exists())
			FileChatDispatch.mkdir();
		File FileChatLog = new File(APP_STORAGE_LOC + DELIM + APP_STORAGE_CHAT_LOC + DELIM + APP_STORAGE_CHAT_LOG_LOC);
		if(!FileChatLog.exists())
			FileChatLog.mkdir();
		
		File fileNativeDir = new File(NATIVE_MESSAGE_LOCATION);
		if(!fileNativeDir.exists())
			fileNativeDir.mkdir();

		File chatFIle = new File(APP_STORAGE_PUBLIC_KEY_LIST);
		if(chatFIle.exists())
			try {
				chatFIle.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		File nativeMessageDir = new File(APP_STORAGE_LOC + DELIM + APP_STORAGE_NATIVE_MESSAGE_LOC);
		if(!nativeMessageDir.exists())
			nativeMessageDir.mkdir();

		//Initialization of database files
		//replicated Chrome database file initialization
		final String REPLICATED_DB_CREATE_STATEMENT = "CREATE TABLE webappsstore2 (originAttributes TEXT, originKey TEXT, scope TEXT, key TEXT, value TEXT)";
		final String INCOMING_DB_CREATE_STATEMNT = "CREATE TABLE incoming_chat ('sender' TEXT, 'data' TEXT, 'signature' TEXT NOT NULL UNIQUE, PRIMARY KEY(signature))";
		if(!new File(REPLICATED_CHROME_DB).exists())
		{
			try {
				Class.forName(ENV.JDBC_DRIVER);
				Connection c = DriverManager.getConnection(ENV.JDBC_CONNECTION_STRING + REPLICATED_CHROME_DB);
				c.createStatement().executeUpdate(REPLICATED_DB_CREATE_STATEMENT);
				c.close();
				System.out.println("Replicated chrome DB is created : " + new File(REPLICATED_CHROME_DB).getAbsolutePath());
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		//native message replicated database file initialization
		if(!new File(REPLICATED_NATIVE_MESSGAE_DB).exists())
		{
			try {
				Class.forName(ENV.JDBC_DRIVER);
				Connection c = DriverManager.getConnection(ENV.JDBC_CONNECTION_STRING + REPLICATED_NATIVE_MESSGAE_DB);
				c.createStatement().executeUpdate(REPLICATED_DB_CREATE_STATEMENT);
				c.close();
				System.out.println("Replicated native message database is created : " + new File(REPLICATED_NATIVE_MESSGAE_DB).getAbsolutePath());
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		if(!new File(APP_STORAGE_INCOMING_CHAT_DATABASE_FILE).exists())
		{
			try {
				Class.forName(ENV.JDBC_DRIVER);
				Connection c = DriverManager.getConnection(ENV.JDBC_CONNECTION_STRING + APP_STORAGE_INCOMING_CHAT_DATABASE_FILE);
				c.createStatement().executeUpdate(INCOMING_DB_CREATE_STATEMNT);
				c.close();
				System.out.println("Incoing chat database is created : " + new File(APP_STORAGE_INCOMING_CHAT_DATABASE_FILE).getAbsolutePath());
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}


		if(new File(CHROME_BASE_DIR_LOCATION_CONFIG_FILE).exists())
		{
			try
			{
				BufferedReader br = new BufferedReader(new FileReader(CHROME_BASE_DIR_LOCATION_CONFIG_FILE));
				String st = null;
				while((st = br.readLine()) != null)
					_CHROME_BASE_FILE = st;

				br.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}	
		}

		if(new File(FIREFOX_BASE_DIR_LOCATION_CONFIG_FILE).exists())
		{
			try
			{
				BufferedReader br = new BufferedReader(new FileReader(FIREFOX_BASE_DIR_LOCATION_CONFIG_FILE));
				String st = null;
				while((st = br.readLine()) != null)
					_FIREFOX_BASE_FILE = st;

				br.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}	
		}
	}

	//Specific exception messages for exception handling
	public static final String EXCEPTION_INTR_MESSAGE_MAGIC_BYTES = "EXCEPTION_INTR_MESSAGE_MAGIC_BYTES";
	public static final String EXCEPTION_CHAT_MESSAGE_MAGIC_BYTES = "EXCEPTION_CHAT_MESSAGE_MAGIC_BYTES";
	public static final String EXCEPTION_MESSAGE_MISMATCHED_PACKET_SIZE = "EXCEPTION_MESSAGE_MISMATCHED_PACKET_SIZE";
	public static final String EXCEPTION_MESSAGE_MISMATCHED_INTR_PACKET_SIZE = "EXCEPTION_MESSAGE_MISMATCHED_INTR_PACKET_SIZE";
	public static final String EXCEPTION_MESSAGE_CIPHER_FAILURE = "EXCEPTION_MESSAGE_CIPHER_FAILURE";
	public static final String EXCEPTION_MESSAGE_GARBAGE_PACKET = "EXCEPTION_MESSAGE_GARBAGE_PACKET";
	public static final String EXCEPTION_CHAT_SIGNATURE_ERROR = "EXCEPTION_CHAT_SIGNATURE_ERROR";
	public static final String EXCEPTION_BROWSER_EXTENSION_MISSING = "EXCEPTION_BROWSER_EXTENSION_MISSING";
	public static final String EXCEPTION_FOUNTAIN_TABLE_MISSING = "EXCEPTION_FOUNTAIN_TABLE_MISSING";
	

	public static final String EXCEPTION_MESSAGE_EMPTY_TABLE = "EMPTY_TABLE";
	//////////////////////////////////////////////////////

	public static final int DISPACTH_REQUEST_THRESHOLD = 0;

	public static final int AES_KEY_SIZE = 16;

	public static final String APP_JSON_EXTENSION = ".table";
	public static final String APP_BIN_EXTENSION = ".bin";


	public static final int FOUNTAIN_CHUNK_SIZE = 10000;

	public static final String DATABASE_TABLE_COL = "BQVZ-tildem-table";
	public static final String DATABASE_DROPLET_COL = "BQVZ-tildem";

	public static final String BROWSER_COMM_LINK = "comm.txt";

	public static final boolean MULTIPLE_PROVIDER_SUPPORT =  true;
	public static final boolean AON_SUPPORT = true;

	public static final boolean EXPERIMENTAL = false;
	//native messaging port
	public static final int NATIVE_MESSAGE_PORT = 43457;

	public static final boolean COMPRESSION_SUPPORT = false;

	public static final char[] PROGRESS_SYMB = {'-', '\\', '|', '/'};

	//if false then broadcast mode
	public static final boolean CHAT_MODE_RELAY = true;
	public static String DIAG= "OS : " + System.getProperty("os.name") + " architecture : " + System.getProperty("os.arch") 
								+ "\nJava : " + System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.name") + ", version : "+System.getProperty("java.version");
	
	public static final String ABOUT_MESSAGE = "To those who can hear me, I say - do not despair. \n The misery that is now upon us is but the passing of greed - "
			+ "\nthe bitterness of men who fear the way of human progress. \nThe hate of men will pass,"
			+ " and dictators die, and the power they took from the people \n will return to the people.\nAnd so long as men die, liberty will never perish. .....\n"
			+ "- Charlie Chaplin (The Great Dictator)"
			+ "\n\nNinaPumpkin version 0.8b (GC-Native.FF-Feed.BW-Dis)"
			+ "\n" + DIAG;
	
	
	public static boolean isAdmin() {
	    String groups[] = (new com.sun.security.auth.module.NTSystem()).getGroupIDs();
	    for (String group : groups) {
	        if (group.equals("S-1-5-32-544"))
	            return true;
	    }
	    return false;
	}
	
	//cryptographic params
	public static final String CRYPTO_HASH_ALGORITHM = "SHA-256";
	public static final String CRYPTO_SYMMETRIC_ALGORITHM = "AES";
	public static final int CRYPTO_SYMMETRIC_KEY_SIZE = 128;
	public static final String CRYPTO_SYMMETRIC_MODE_OF_OPERATION = "AES/CTR/PKCS5Padding";
	
	//JDBC parameters
	public static final String JDBC_DRIVER = "org.sqlite.JDBC";
	public static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:";
	
	
	//native message experiment
	public static final boolean EXPERIMENTAL_NATIVE_MESSAGE = true;
}
