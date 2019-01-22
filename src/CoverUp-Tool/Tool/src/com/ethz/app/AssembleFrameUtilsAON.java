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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.json.JSONObject;

import com.ethz.app.env.ENV;
import com.ethz.fountain.Droplet;
import com.ethz.fountain.Glass;

public class AssembleFrameUtilsAON {
	
	public static void assembleDropletsAON(String JSONDirPath, 
			JFrame frame, byte[] decodedData_out, JEditorPane textArea, JProgressBar progressBar, JButton btnNewButton)
	{
		if(JSONDirPath == null)
			JOptionPane.showMessageDialog(frame, "JSON path not set!");

		else
		{
			
			File[] files =  new File(AssembleFrame.JSONDirPath).listFiles();
			
			com.ethz.fountainAON.Glass glass = null;

			try
			{
				int counter = 0;
				for(File file : files)
				{
					
					if(file.getName().contains(ENV.APP_STORAGE_DROPLET_URL) || file.getName().contains(ENV.APP_STORAGE_COMPLETE_DATA) ||
							file.getName().contains(ENV.APP_STORAGE_COMPLETE_DATA_AON))
						continue;
					
					//System.out.println(file.getAbsoluteFile());
					BufferedReader br = null;
					try 
					{
						br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
						StringBuffer stb = new StringBuffer();
						String st = "";

						while((st = br.readLine()) != null)
							stb.append(st);

						JSONObject jObject = new JSONObject(stb.toString());

						com.ethz.fountainAON.Droplet d = new com.ethz.fountainAON.Droplet(Base64.getUrlDecoder().decode(jObject.get("data").toString()), Base64.getUrlDecoder().decode(jObject.get("seed").toString()), jObject.getInt("num_chunks"));
						
						//initialize glass only once
						if(glass == null)
							glass = new com.ethz.fountainAON.Glass(jObject.getInt("num_chunks"));
						
						glass.addDroplet(d);

						counter++;
						if(glass.isDone())
						{	
							//TODO: this size has to be dynamic depends on the data size which is to be preshared in the table
							//byte[] decodedData = new byte[1000];

							//for(int i = 0; i < Glass.chunks.length; i++)
							//	System.arraycopy(Glass.chunks[i], 0, decodedData, i * 100, 100);

							byte[] decodedData = glass.getDecodedData();
							
							//copy only the non padded data
							try
							{
								System.arraycopy(decodedData, 0, decodedData_out, 0, decodedData_out.length);
							}
							catch(Exception ex)
							{
								System.out.println("Stand alone run. Table info not available");
							}
							JOptionPane.showMessageDialog(frame, "Decoding success\nDroplet utilized : " + counter + ", Total Droplets : " + files.length);
							
							//textArea.setText(Base64.getUrlEncoder().encodeToString(decodedData));
							textArea.setText(new String(decodedData).trim());
							progressBar.setValue(100);						
							
							AssembleFrame.glassDone = true;
							
							//put this information in APP_STORAGE_LOC
							try
							{
								String dropletUrlFileName =  AssembleFrame.JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_DROPLET_URL;
								BufferedReader brUrl = new BufferedReader(new FileReader(dropletUrlFileName));
								String stTemp = null, fountainUrl = null;
								while((stTemp = brUrl.readLine()) != null)
								{
									fountainUrl = stTemp;
								}
								brUrl.close();
								
								ValRow.progress_map.put(fountainUrl, 100);

								FileWriter compl_fw = new FileWriter(ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_COMPLETED_DROPLET_FILE, true);
								compl_fw.append(fountainUrl + "\n");
								compl_fw.close();

								File completeDataFile = new File(AssembleFrame.JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_COMPLETE_DATA_AON);

								if(!completeDataFile.exists())
								{
									FileWriter data_fw = new FileWriter(AssembleFrame.JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_COMPLETE_DATA_AON);
									data_fw.append(new String(decodedData));
									data_fw.close();
								}
							}
							catch(Exception ex)
							{
								JOptionPane.showMessageDialog(frame, "Exception happened while assembling droplets \n" + ex.getMessage());
							}
							
							btnNewButton.setEnabled(true);
							break;
						}	

						//JOptionPane.showMessageDialog(frame, "Assemble success!!!");
					} 
					catch (FileNotFoundException e1) 
					{
						e1.printStackTrace();
					} 
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
					finally 
					{
						try {

							br.close();
						} 
						catch (IOException e1) 
						{
							e1.printStackTrace();
						}
					}
				}
				if(!glass.isDone())
				{
					JOptionPane.showMessageDialog(frame, "Not enought droplets yet!");
					byte[][] partialChunks = Glass.chunks;
					StringBuffer stb = new StringBuffer();
					
					int completed = 0, s_size = 0;
					boolean flag = false;
					
					for(byte b[] : partialChunks)
					{
						//total += b.length;
						if(b == null)
						{								
							stb.append("<_______missing chunk______>");
						}
						else
						{
							flag = true;
							s_size = b.length;
							completed += b.length;
							stb.append(new String(b));
						}
					}
					
					textArea.setText(stb.toString().trim());
					if(flag)
						progressBar.setValue((100 * completed)/(s_size * partialChunks.length));
					else
						progressBar.setValue(0);
					
					//System.out.println((completed * 100)/(s_size * partialChunks.length));
				}
				
			}
			catch(NullPointerException ex)
			{
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Droplet dir missing!!");
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Bad input!!");
			}

		}
	
	}
	
	
	
	public static int assembleDroplets_NonFrame(String JSONDirPath, byte[] decodedData_out) throws RuntimeException
	{
		if(JSONDirPath == null)
			throw new RuntimeException("Path null");

		else
		{	
			File[] files =  new File(AssembleFrame.JSONDirPath).listFiles();		
			Glass glass = null;
			try
			{
				for(File file : files)
				{
					
					if(file.getName().contains(ENV.APP_STORAGE_DROPLET_URL) || file.getName().contains(ENV.APP_STORAGE_COMPLETE_DATA) ||
							file.getName().contains(ENV.APP_STORAGE_COMPLETE_DATA_AON))
						continue;
					
					//System.out.println(file.getAbsoluteFile());
					BufferedReader br = null;
					try 
					{
						br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
						StringBuffer stb = new StringBuffer();
						String st = "";

						while((st = br.readLine()) != null)
							stb.append(st);

						JSONObject jObject = new JSONObject(stb.toString());				
						Droplet d = new Droplet(Base64.getUrlDecoder().decode(jObject.get("data").toString()), Base64.getUrlDecoder().decode(jObject.get("seed").toString()), jObject.getInt("num_chunks"));
						
						//initialize glass only once
						if(glass == null)
							glass = new Glass(jObject.getInt("num_chunks"));
						
						glass.addDroplet(d);
						if(glass.isDone())
						{	
							byte[] decodedData = glass.getDecodedData();
							
							//copy only the non padded data
							System.arraycopy(decodedData, 0, decodedData_out, 0, decodedData_out.length);
							
							//JOptionPane.showMessageDialog(frame, "Decoding success\nDroplet utilized : " + counter + ", Total Droplets : " + files.length);				
							
							AssembleFrame.glassDone = true;
							
							//put this information in APP_STORAGE_LOC
							try
							{
								String dropletUrlFileName =  AssembleFrame.JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_DROPLET_URL;
								BufferedReader brUrl = new BufferedReader(new FileReader(dropletUrlFileName));
								String stTemp = null, fountainUrl = null;
								while((stTemp = brUrl.readLine()) != null)
								{
									fountainUrl = stTemp;
								}
								brUrl.close();
								
								ValRow.progress_map.put(fountainUrl, 100);

								FileWriter compl_fw = new FileWriter(ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_COMPLETED_DROPLET_FILE, true);
								compl_fw.append(fountainUrl + "\n");
								compl_fw.close();

								File completeDataFile = new File(AssembleFrame.JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_COMPLETE_DATA_AON);

								if(!completeDataFile.exists())
								{
									FileWriter data_fw = new FileWriter(AssembleFrame.JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_COMPLETE_DATA_AON);
									data_fw.append(new String(decodedData));
									data_fw.close();
								}
							}
							catch(Exception ex)
							{
								throw new RuntimeException("Exception happened while assembling droplets");
							}
							
							return 100;
						}	

						//JOptionPane.showMessageDialog(frame, "Assemble success!!!");
					} 
					catch (FileNotFoundException e1) 
					{
						e1.printStackTrace();
					} 
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
					finally 
					{
						try {

							br.close();
						} 
						catch (IOException e1) 
						{
							e1.printStackTrace();
						}
					}
				}
				int completed = 0, s_size = 0;
				boolean flag = false;
				if(!glass.isDone())
				{
					byte[][] partialChunks = Glass.chunks;
					
					for(byte b[] : partialChunks)
					{
						//total += b.length;
						if(b != null)
						{
							flag = true;
							s_size = b.length;
							completed += b.length;
						}
					}
					
					if(flag)
						return (100 * completed)/(s_size * partialChunks.length);
					else
						return 0;
				}
				
			}
			catch(NullPointerException ex)
			{
				ex.printStackTrace();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}

		}
		return 0;
	
	}

}
