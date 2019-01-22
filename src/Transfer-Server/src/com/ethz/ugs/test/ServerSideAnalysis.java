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

package com.ethz.ugs.test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import jdk.jfr.events.FileWriteEvent;

/**
 * @author Aritra
 *
 */
public class ServerSideAnalysis {

	public static String[] clients = {
			"192.168.3.32",
			"192.168.3.42",
			"192.168.3.49",
			"192.168.3.23",
			"192.168.3.27"
	};
	public static String[] testFiles = {
			"windows_loading_woExt_remote_server_CU",
			"windows_loading_withExt_withApp_active_remote_server_CU",
			"windows_loading_withExt_withApp_idle_remote_server_CU",
			"windows_loading_woExt_withApp_collecting_remote_server_CU",
			"windows_loading_woExt_profile1_remote_server_CU",
			"windows_loading_withExt_withApp_profile1_active_remote_server_CU",
			"windows_loading_withExt_withApp_profile1_idle_remote_server_CU",
			"windows_periodic_woExt_remote_server_CU",
			"windows_periodic_withExt_withApp_active_remote_server_CU",
			"windows_periodic_withExt_withApp_idle_remote_server_CU",
			"windows_periodic_woExt_withApp_collecting_remote_server_CU",
			"windows_periodic_woExt_remote_profile1_server_CU",
			"windows_periodic_withExt_withApp_active_profile1_remote_server_CU",
			"windows_periodic_withExt_withApp_profile1_idle_remote_server_CU"
	};



	public static String root = "Z:\\aritra\\wireless_lab_server_mngt_windows\\server_side_trace\\test_CU";
	public static void main(String[] args) throws IOException {

		File rootFile = new File(root);

		//String testFile = rootFile + "\\" + testFiles[7];
		for(File testFile : rootFile.listFiles())
		{
			if(!testFile.isDirectory())
				continue;
			
			System.err.println(testFile);
			for(String client : clients)
			{
				File clientFolder = new File(testFile + "/" + client);
				if(!clientFolder.exists())
					clientFolder.mkdir();
				File[] dataFiles = testFile.listFiles();

				for(File dataFile : dataFiles)
				{
					if(!dataFile.getName().contains(".txt"))
						continue;

					FileWriter fw = new FileWriter(clientFolder.getAbsolutePath() + "/" + dataFile.getName());
					System.out.println("Processing : " + dataFile);
					BufferedReader br = new BufferedReader(new FileReader(dataFile));
					String str = null;
					while((str = br.readLine()) != null)
					{
						if(str.contains(client))
							fw.append(str + "\n");
					}
					br.close();
					fw.close();
				}
			}
		}
	}
}
