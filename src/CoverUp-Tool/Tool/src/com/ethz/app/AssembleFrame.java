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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.json.JSONObject;

import com.ethz.app.dbUtils.TableChecker;
import com.ethz.app.env.ENV;
import com.ethz.compressUtil.CompressUtil;
import com.ethz.fountain.Droplet;
import com.ethz.fountain.Fountain;

import javax.swing.JProgressBar;

public class AssembleFrame {

	JFrame frame;

	JFileChooser chooser;
	public static String JSONDirPath;
	String urlString;
	boolean independent;
	public byte[] fountainSeed;
	public JProgressBar progressBar; 
	public byte[] decodedData;
	public int dataLength;
	public static boolean glassDone = false;
	int dropletCount;
	
	public byte[] decodedDataWOPadding;
	
	/**
	 * Launch the application.
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException 
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());	

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try 
				{
					AssembleFrame window = new AssembleFrame();
					window.frame.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AssembleFrame() {
		this.independent = true;
		initialize();
	}
	
	public int GetProgress()
	{
		if(this.progressBar == null)
			return 0;
		else
			return this.progressBar.getValue();
	}
	
	public void setSeed(String seedStr)
	{
		this.fountainSeed = Base64.getUrlDecoder().decode(seedStr);
	}
	
	public AssembleFrame(String urlString, int dataLength, int dropletCount) 
	{
		this.urlString = urlString;
		this.dataLength = dataLength;
		
		this.decodedDataWOPadding = new byte[dataLength];
		JSONObject jObject = TableChecker.URL_JSON_TABLE_MAP.get(urlString);
		String dropletDirID = jObject.getString("dropletLoc");
		JSONDirPath = ENV.APP_STORAGE_LOC + ENV.DELIM + dropletDirID;
		this.dropletCount = dropletCount;
		
		this.independent = false;
		System.out.println(JSONDirPath);
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 832, 635);
		ImageIcon frameIcon = new ImageIcon(AssembleFrame.class.getResource("/doge.png"));
		frame.setIconImage(frameIcon.getImage());
		frame.setTitle("Fountain assemble");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		JEditorPane textArea = new JEditorPane();
		//Font font = UIManager.getFont("Label.font");
        //String bodyRule = "body { font-family: " + font.getFamily() + "; " +
        //       "font-size: " + 16 + "pt; }";
        //((HTMLDocument) textArea.getDocument()).getStyleSheet().addRule(bodyRule);
        
        
		textArea.setForeground(Color.BLACK);
		textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		textArea.setFont(new Font("Lucida Console", Font.PLAIN, 16));
		//textArea.setLineWrap(true);
		textArea.setEditable(false);
		//scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportView(textArea);

		JPanel panel_1 = new JPanel();
		scrollPane.setColumnHeaderView(panel_1);

		JLabel lblNewLabel = new JLabel("JSON folder not selected");
		
		if(!independent)
			lblNewLabel.setText("JSON folder : " + JSONDirPath);
		
		panel_1.add(lblNewLabel);


		JButton btnLocateDropletFolder = new JButton("Locate");
		btnLocateDropletFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{

				chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Select JSON dir");
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				chooser.setAcceptAllFileFilterUsed(false);  

				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) 
				{ 		
					JSONDirPath = chooser.getSelectedFile().getAbsolutePath();
					
					if(JSONDirPath != null)
						lblNewLabel.setText("JSON folder : " + JSONDirPath);
				}
				else 
				{
					lblNewLabel.setText("JSON folder not selected");

				}
			}
		});
		panel.add(btnLocateDropletFolder);

		JButton btnAssembleDroplets = new JButton("Assemble");
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		JButton btnNewButton = new JButton("Make Droplets");
		btnNewButton.setEnabled(false);
		
		
		btnAssembleDroplets.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				AssembleFrameUtils.assembleDroplets(JSONDirPath, frame, decodedDataWOPadding, textArea, progressBar, btnNewButton);
				
				//if(decodedDataWOPadding == null)
					//System.err.println("NULL");
				/*				
				if(JSONDirPath == null)
					JOptionPane.showMessageDialog(frame, "JSON path not set!");

				else
				{
					
					File[] files =  new File(JSONDirPath).listFiles();
					
					Glass glass = null;

					try
					{
						int counter = 0;
						for(File file : files)
						{
							
							if(file.getName().contains(ENV.APP_STORAGE_DROPLET_URL) || file.getName().contains(ENV.APP_STORAGE_COMPLETE_DATA))
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

								counter++;
								if(glass.isDone())
								{	
									//TODO: this size has to be dynamic depends on the data size which is to be preshared in the table
									//byte[] decodedData = new byte[1000];

									//for(int i = 0; i < Glass.chunks.length; i++)
									//	System.arraycopy(Glass.chunks[i], 0, decodedData, i * 100, 100);

									decodedData = glass.getDecodedData();				
									System.arraycopy(decodedData, 0, decodedDataWOPadding, 0, dataLength);
									
									JOptionPane.showMessageDialog(frame, "Decoding success\nDroplet utilized : " + counter + ", Total Droplets : " + files.length);
									
									//textArea.setText(Base64.getUrlEncoder().encodeToString(decodedData));
									textArea.setText(new String(decodedDataWOPadding));
									progressBar.setValue(100);						
									
									glassDone = true;
									
									//put this information in APP_STORAGE_LOC
									try
									{
										String dropletUrlFileName =  JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_DROPLET_URL;
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

										File completeDataFile = new File(JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_COMPLETE_DATA);

										if(!completeDataFile.exists())
										{
											FileWriter data_fw = new FileWriter(JSONDirPath + ENV.DELIM + ENV.APP_STORAGE_COMPLETE_DATA);
											data_fw.append(new String(decodedDataWOPadding));
											data_fw.close();
										}
									}
									catch(Exception ex)
									{
										System.out.println("--");
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
							
							textArea.setText(stb.toString());
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


				}*/
			}
		});
		panel.add(btnAssembleDroplets);
		
		JButton btnDecrypt = new JButton("Decrypt (AON support)");
		
		btnDecrypt.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				AssembleFrameUtilsAON.assembleDropletsAON(JSONDirPath, frame, decodedDataWOPadding, textArea, progressBar, btnNewButton);
			}
		});
		
		panel.add(btnDecrypt);
		panel.add(progressBar);
		
		JButton decompressButton = new JButton("Decompress");
		
		if(!ENV.COMPRESSION_SUPPORT)
			decompressButton.setEnabled(false);
		
		decompressButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				if(!glassDone)
					JOptionPane.showMessageDialog(frame, "Not enought droplets yet!");
				
				else
				{
					byte[] compressedData = textArea.getText().getBytes();
					byte[] decompressedData = null;
					try 
					{
						decompressedData = CompressUtil.deCompress(compressedData);
					} 
					catch (IOException e1) 
					{			
						e1.printStackTrace();
					}
					textArea.setText(new String(decompressedData));
				}
			}
		});
		panel.add(decompressButton);
		
		
		btnNewButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				String dropletLocation = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_BROWSER_COMM_DROPLET_LOC;
				String dropletLocation_bin = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_BROWSER_COMM_DROPLET_BIN_LOC;
				
				if(!new File(dropletLocation).exists())
					new File(dropletLocation).mkdir();
				
				if(!new File(dropletLocation_bin).exists())
					new File(dropletLocation_bin).mkdir();
				
				
				String dropletLocationSpecific = dropletLocation + ENV.DELIM + new File(JSONDirPath).getName();
				String dropletLocationSpecific_bin = dropletLocation_bin + ENV.DELIM + new File(JSONDirPath).getName();
				
				if(!new File(dropletLocationSpecific).exists())
					new File(dropletLocationSpecific).mkdir();
				
				if(!new File(dropletLocationSpecific_bin).exists())
					new File(dropletLocationSpecific_bin).mkdir();
				
				String counterS = (String)JOptionPane.showInputDialog(frame, "Enter droplet count", "Droplet Generate", JOptionPane.PLAIN_MESSAGE, null, null, "50");
				
				int dropletCounter = dropletCount;
				try
				{
					dropletCounter = Integer.parseInt(counterS);
				}
				catch(Exception ex)
				{
					JOptionPane.showMessageDialog(frame, "Invalid droplet count, default value = 50");
				}
				try 
				{
					//for standalone run seed is null.
					
					Fountain fountain = new Fountain(decodedDataWOPadding, ENV.FOUNTAIN_CHUNK_SIZE, fountainSeed);
					
					for(int i = 0; i < dropletCounter; i++)
					{
						Droplet generatedDroplet = fountain.droplet();
						FileWriter fw = new FileWriter(dropletLocationSpecific + ENV.DELIM + i  + ".json");
						fw.write(generatedDroplet.toString());
						fw.close();
						
						FileOutputStream fw_bin = new FileOutputStream(dropletLocationSpecific_bin + ENV.DELIM + i  + ENV.APP_BIN_EXTENSION);
						byte[] ba = generatedDroplet.toByteArray();
						fw_bin.write(ba);
						//System.out.println(ba.length);
						fw_bin.close();
					}
					
					JOptionPane.showMessageDialog(frame,
						    "Droplets dumped in : \n" + "JSON : "+ dropletLocationSpecific + "\n BIN : " + dropletLocationSpecific_bin,
						    "Droplets generated",
						    JOptionPane.PLAIN_MESSAGE);
				}
				
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(frame, "Error!! Droplets are not generated.\n Run it from Table app");
					e1.printStackTrace();
				}
				
				//File dropletLocation = 
			}
		});
		panel.add(btnNewButton);
		if(!ENV.AON_SUPPORT)
			btnDecrypt.setEnabled(false);
	}

}
