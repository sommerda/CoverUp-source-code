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
package com.ethz.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.JSONObject;

import com.ethz.app.env.ENV;


/**
 * @author Aritra
 *
 */
public class NativeMessageSetUp {

	static String jsonFilePath = new File(ENV.NATIVE_MESSAGE_JSON_FILE).getAbsolutePath();
	static String pythonFilePath = new File(ENV.NATIVE_MESSAGE_PYTHON_FILE).getAbsolutePath();
	
	public static void setUp(JFrame frame) throws IOException
	{
		unpackNativeMessageFiles();
		
		if(ENV.isWindows)
			setUpWindows(frame, NativeMessageSetUp.jsonFilePath);

		else if(ENV.isLinux)
			setUpLinux(frame, NativeMessageSetUp.jsonFilePath);

		else
			setUpMac(frame, NativeMessageSetUp.jsonFilePath);

	}

	public static String firefoxRegCommand = "REG ADD \"HKEY_CURRENT_USER\\SOFTWARE\\Mozilla\\NativeMessagingHosts\\native_comm\" ";
	public static String chromeRegCommand = "REG ADD \"HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\native_comm\" ";

	public static JSONObject makeNativeJson(String jsonFilePath) throws IOException
	{
		String jsonString = new String(Files.readAllBytes(new File(jsonFilePath).toPath()), StandardCharsets.UTF_8);
		JSONObject jsonRead = new JSONObject(jsonString);

		JSONObject jObject = new JSONObject();
		jObject.put("name", jsonRead.getString("name"));
		jObject.put("description", jsonRead.getString("description"));
		if(ENV.isWindows)
			jObject.put("path", NativeMessageSetUp.jsonFilePath.replaceAll("native_comm.json", "native_ext.bat"));
		
		else if(ENV.isLinux)
			jObject.put("path", NativeMessageSetUp.pythonFilePath);
		
		jObject.put("type", jsonRead.getString("type"));
		
		jObject.put("allowed_origins", jsonRead.get("allowed_origins"));

		return jObject;
	}

	public static void setUpWindows(JFrame frame, String jsonFilePath) throws IOException
	{
		String regCommand = null;
		if(AppMain.selectedPrimaryBrowser.equals(ENV.BROWSER_CHROME) || AppMain.selectedPrimaryBrowser.equals(ENV.BROWSER_NATIVE_MESSAGE)) 
			regCommand = chromeRegCommand;
		else
			regCommand = firefoxRegCommand;

		if(!ENV.isAdmin())
			JOptionPane.showMessageDialog(frame, "Not administrator. Run with administrator", 
					"Not administrator", JOptionPane.ERROR_MESSAGE);				 

		Process p = null;
		try 
		{
			p = Runtime.getRuntime().exec(regCommand + "/ve /d \""+ jsonFilePath + "\" /F");
		} 
		catch (IOException e1) 
		{
			JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
					"Execution result", JOptionPane.ERROR_MESSAGE);
			return;
		}

		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null; 
		StringBuffer stb = new StringBuffer();

		try 
		{
			while ((line = input.readLine()) != null)
				stb.append(line + "\n");
		} 
		catch (IOException e1) 
		{
			JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
					"Execution result", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try 
		{
			p.waitFor();
		} catch (InterruptedException e2)
		{
			JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
					"Execution result", JOptionPane.ERROR_MESSAGE);
		}
  
		
		JSONObject jObject = makeNativeJson(jsonFilePath);

		FileWriter fw = new FileWriter(jsonFilePath);
		fw.write(jObject.toString(2));
		fw.flush();
		fw.close();

		JFileChooser choose = new JFileChooser(".");
		choose.setDialogTitle("Locate python.exe file (python27)");
		int res = choose.showDialog(frame, "Open file");
		if(res == JFileChooser.APPROVE_OPTION)
		{
			File pythonPath = choose.getSelectedFile();
			fw = new FileWriter(jsonFilePath.replaceAll("native_comm.json", "native_ext.bat"));
			fw.append("@echo off\n");
			fw.append("\"" + pythonPath.getCanonicalPath() + "\" \"" + jsonFilePath.replaceAll("native_comm.json", "native-messaging-example-host") + "\"");
			fw.flush();
			fw.close();
			JOptionPane.showMessageDialog(frame, stb.toString(), "Execution result", JOptionPane.INFORMATION_MESSAGE);
		}
	}


	public static final String linuxChromeNativePathSystem = "/etc/chromium/native-messaging-hosts";
	public static final String linuxChromeNativePathUser = System.getenv("HOME") + "/.config/chromium/NativeMessagingHosts";
	public static final String linuxPermissionCommand = "chmod a+x ";
	/**
	 * Only written for Chrome, Firefox excluded
	 * @param frame
	 * @param jsonFilePath
	 * @throws IOException
	 */
	public static void setUpLinux(JFrame frame, String jsonFilePath) throws IOException
	{
		File file = new File(linuxChromeNativePathSystem);
		if(!file.exists())
			file.mkdir();
		file = null;
		file = new File(linuxChromeNativePathUser);
		if(!file.exists())
			file.mkdir();

		JSONObject jObject = makeNativeJson(jsonFilePath);

		try
		{
			String nativeJSONSystemWidePath = linuxChromeNativePathSystem + "/native_comm.json";
			FileWriter fwS = new FileWriter(nativeJSONSystemWidePath);
			fwS.write(jObject.toString(2));
			fwS.flush();
			fwS.close();
			
			Process p = null;
			try 
			{
				p = Runtime.getRuntime().exec(linuxPermissionCommand + "nativeJSONSystemWidePath");
			} 
			catch (IOException e1) 
			{
				JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
						"Execution result", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null; 
			StringBuffer stb = new StringBuffer();

			try 
			{
				while ((line = input.readLine()) != null)
					stb.append(line + "\n");
			} 
			catch (IOException e1) 
			{
				JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
						"Execution result", JOptionPane.ERROR_MESSAGE);
				return;
			}

			try 
			{
				p.waitFor();
			} catch (InterruptedException e2)
			{
				JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
						"Execution result", JOptionPane.ERROR_MESSAGE);
			}

			JOptionPane.showMessageDialog(frame, "Operation executed successfully", "Execution result", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(IOException ex)
		{
			//ex.printStackTrace();
			//System.out.println("------- end of stack trace ------");
			try
			{
				System.err.println("System wide resource injection failed. Try with user only");
				String nativeJSONUserPath = linuxChromeNativePathUser + "/native_comm.json";
				FileWriter fwU = new FileWriter(nativeJSONUserPath);
				fwU.write(jObject.toString(2));
				fwU.flush();
				fwU.close();
				
				Process p = null;
				try 
				{
					p = Runtime.getRuntime().exec(linuxPermissionCommand + "nativeJSONSystemWidePath");
				} 
				catch (IOException e1) 
				{
					JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
							"Execution result", JOptionPane.ERROR_MESSAGE);
					return;
				}

				
				JOptionPane.showMessageDialog(frame, "Operation executed successfully", "Execution result", JOptionPane.INFORMATION_MESSAGE);
			}
			catch(IOException ex1)
			{
				JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
						"Execution result", JOptionPane.ERROR_MESSAGE);
				ex1.printStackTrace();
				System.out.println("-------- end of stack trace --------------");
			}
		}

	}

	public static final String macChromeNativePathSystem = " /Library/Google/Chrome/NativeMessagingHosts";
	public static final String macChromeNativePathUser = "~/Library/Application Support/Google/Chrome/NativeMessagingHosts";

	public static void setUpMac(JFrame frame, String jsonFilePath) throws IOException
	{
		File file = new File(macChromeNativePathSystem);
		if(!file.exists())
			file.mkdir();
		file = null;
		file = new File(macChromeNativePathUser);
		if(!file.exists())
			file.mkdir();
		try
		{
			FileWriter fwS = new FileWriter(macChromeNativePathSystem + "/native_comm.json");
			FileWriter fwU = new FileWriter(macChromeNativePathUser + "/native_comm.json");

			JSONObject jObject = makeNativeJson(jsonFilePath);

			fwS.write(jObject.toString(2));
			fwS.flush();
			fwS.close();

			fwU.write(jObject.toString(2));
			fwU.flush();
			fwU.close();
			JOptionPane.showMessageDialog(frame, "Operation executed successfully", "Execution result", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(IOException ex)
		{
			JOptionPane.showMessageDialog(frame, "Operation failed. Please follow instruction from CoverUp.tech", 
					"Execution result", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void unpackNativeMessageFiles() throws IOException
	{

		FileWriter fwPy = new FileWriter(ENV.NATIVE_MESSAGE_PYTHON_FILE);
		InputStream in = AssembleFrame.class.getResourceAsStream(ENV.NATIVE_RESOURCE_MESSAGE_PYTHON_FILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String str = null;
		while((str = br.readLine()) != null)
			fwPy.append(str + "\n");

		in.close();
		br.close();
		fwPy.close();


		FileWriter fwJson = new FileWriter(ENV.NATIVE_MESSAGE_JSON_FILE);

		InputStream in1 = AssembleFrame.class.getResourceAsStream(ENV.NATIVE_RESOURCE_MESSAGE_JSON_FILE);
		BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
		while((str = br1.readLine()) != null)
			fwJson.append(str + "\n");

		in1.close();
		br1.close();
		fwJson.close();

	}
	/*
	public static void main(String[] args) throws IOException {

		System.out.println(NativeMessageSetUp.class.getResource(ENV.NATIVE_RESOURCE_MESSAGE_JSON_FILE).getFile());
		String t = NativeMessageSetUp.class.getResource(ENV.NATIVE_RESOURCE_MESSAGE_JSON_FILE).getFile();
		System.out.println(new String(Files.readAllBytes(new File(t).toPath())));
	}*/
}
