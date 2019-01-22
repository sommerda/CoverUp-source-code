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
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

import com.ethz.app.binUtils.BinUtils;
import com.ethz.app.chatApp.ChatApp;
import com.ethz.app.covertBrowser.CovertBrowserSA;
import com.ethz.app.dbUtils.TableChecker;
import com.ethz.app.dispatchSocket.TCPClient;
import com.ethz.app.env.ASCIIart;
import com.ethz.app.env.ENV;
import com.ethz.app.nativeMessageListener.NativeMessageListenerService;
import com.ethz.app.poll.DataBasePollPresetPK;
import com.sun.xml.internal.messaging.saaj.soap.Envelope;

/**
 * Underground application entry point
 * @author Aritra
 *
 */
public class AppMain {

	public static JTable table;

	public static JFrame frame;

	/**
	 * Launch the application.
	 */
	public static TableChecker tableChecker;
	private JTextField txtQq;
	JFileChooser chooser;
	public int databasePollingRate;
	public static boolean startPolling = false;
	public String pkText;
	public String modifiedCacheLocation;
	public static SecretKeySpec key;
	public static IvParameterSpec ivSpec;
	public static Cipher cipher;
	public static byte[] ivBytes;
	private DataBasePollPresetPK dPool;

	public static boolean backGroundAssembling = false;

	public static String selectedPrimaryBrowser;
	public static boolean set = false;

	/**
	 * Application entry point
	 * SAmple command line argument : "chrome "C:\Users\Aritra\AppData\Local\Google\Chrome\User Data\Default\Local Storage" 500 900"
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws UnsupportedLookAndFeelException
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		//mac support disabled for now
		if(ENV.isMac)
		{
			if(!ENV.macSupport)
			{
				System.err.println("Current version of NinjaPumpkin does not support mac!");
				System.exit(1);
			}
		}

		//argument format
		//help or
		//firefox <pathToProfile> <pollingrate>
		if(args.length != 0)
		{
			int val = new ArgumentProcess().processArgument(args);
			if(val == 0) System.exit(0);
		}
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());	

		EventQueue.invokeLater(new Runnable() {
			public void run() 
			{
				try 
				{				
					new AppMain();
					AppMain.frame.setVisible(true); 
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	private static void initiateBrowserSelection()
	{
		final JComboBox<String> combo = new JComboBox<>(new String[]{ENV.BROWSER_FIREFOX, ENV.BROWSER_CHROME, ENV.BROWSER_NATIVE_MESSAGE});

		String[] options = { "Select", "Exit"};

		String title = "Select Primary Browser";
		int selection = JOptionPane.showOptionDialog(null, combo, title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, options[0]);

		if(selection > 0)
			System.exit(1);

		AppMain.selectedPrimaryBrowser = combo.getSelectedItem().toString();
	}

	/**
	 * Create the application.
	 * @throws NoSuchAlgorithmException 
	 * @throws SQLException 
	 */
	//@SuppressWarnings("static-access")
	@SuppressWarnings("unused")
	public AppMain() throws NoSuchAlgorithmException, SQLException {	

		if(!ENV.AUTO_PILOT)
			initiateBrowserSelection();

		if(AppMain.selectedPrimaryBrowser.equals(ENV.BROWSER_CHROME))
			DataBasePollPresetPK.databaseFileLocation = ENV.REPLICATED_CHROME_DB;

		if(AppMain.selectedPrimaryBrowser.equals(ENV.BROWSER_NATIVE_MESSAGE) && !ENV.EXPERIMENTAL_NATIVE_MESSAGE)
		{
			DataBasePollPresetPK.databaseFileLocation = ENV.REPLICATED_NATIVE_MESSGAE_DB;
			//start the server here to receive the TCP connection from the native messaging python application
			Thread nativeListenerThread = new Thread(new NativeMessageListenerService());
			nativeListenerThread.start();
		}

		//TODO experimental, start the message lister service here
		if(AppMain.selectedPrimaryBrowser.equals(ENV.BROWSER_NATIVE_MESSAGE))
		{
			DataBasePollPresetPK.databaseFileLocation = ENV.REPLICATED_NATIVE_MESSGAE_DB;
			Thread t = new Thread(new NativeMessageListenerService());
			t.start();
			
			ArgumentProcess.chrome_native = true;
		}

		//print argument here
		//System.out.println(ASCIIart.ascii);
		System.out.println("============================");
		System.out.println("Autopilot : " + ENV.AUTO_PILOT);
		System.out.println("Primary browser : " + AppMain.selectedPrimaryBrowser);
		System.out.println("Native message : " + ArgumentProcess.chrome_native);
		System.out.println("Autochat : " + ArgumentProcess.autoChat);
		System.out.println("============================");

		AppMain.ivBytes = new byte[16];
		Arrays.fill(AppMain.ivBytes, (byte)0x00);

		//load the key for AES if it exists
		String KeyFileLoc = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_KEY_FILE;
		File keyFile = new File(KeyFileLoc);

		byte[] keyBytes = null;
		if(!keyFile.exists())
		{
			keyBytes = new byte[ENV.AES_KEY_SIZE];
			Arrays.fill(keyBytes, (byte) 0x00);
			FileOutputStream fw_bin = null;
			try 
			{
				fw_bin = new FileOutputStream(KeyFileLoc);
				fw_bin.write(keyBytes);
				fw_bin.close();
				//JOptionPane.showMessageDialog(frame, "Key file generated in " + KeyFileLoc);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		{	
			try {
				keyBytes = Files.readAllBytes(keyFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}

		AppMain.key = new SecretKeySpec(keyBytes, "AES");
		AppMain.ivSpec = new IvParameterSpec(ivBytes);
		try {
			AppMain.cipher = Cipher.getInstance("AES/CTR/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) 
		{
			e.printStackTrace();
		}

		AppMain.tableChecker = new TableChecker();

		try
		{
			AppMain.tableChecker.loadtableData();
		}
		catch(Exception ex)
		{
			if(ex instanceof RuntimeException && (ex.getMessage() == null || 
					ex.getMessage().equals(ENV.EXCEPTION_MESSAGE_EMPTY_TABLE)))
				//ex.printStackTrace();
				//JOptionPane.showMessageDialog(frame, "Database empty. Run polling");
				System.err.println("Database empty. Run polling");

			else
			{
				chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Defalt derectory discovery fail. Select Firefox cache dir");
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				chooser.setAcceptAllFileFilterUsed(false);  

				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) 
				{ 		
					String path = chooser.getSelectedFile().getAbsolutePath();
					AppMain.tableChecker.loadtableData(path);
				}
			}
		}	

		AppMain.ivBytes = new byte[16];
		//TODO bad idea
		Arrays.fill(AppMain.ivBytes, (byte)0x00);

		//initialize data for the chat
		try {
			BinUtils.initializeChatData();
		} catch (Exception e) {

			JOptionPane.showMessageDialog(frame, "Error initializing chat data structures. Whatever! \n" + e.getMessage());
			e.printStackTrace();
		}

		initialize();
	}

	public static Thread covertThread;
	/**
	 * Initialize the contents of the frame.
	 * @throws NoSuchAlgorithmException 
	 */
	private void initialize() throws NoSuchAlgorithmException 
	{
		frame = new JFrame();
		frame.setTitle("Ninja Pumpkin: The most awesome thing ever happened to humanity");
		frame.setBounds(100, 100, 823, 848);
		ImageIcon frameIcon = new ImageIcon(AssembleFrame.class.getResource("/doge.png"));
		//System.out.println(">>>> " + new File(AssembleFrame.class.getResource("/doge.png").toString()) + " :: " + new File(AssembleFrame.class.getResource("/doge.png").getFile()).exists());
		frame.setIconImage(frameIcon.getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/*frame.addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		    	System.out.println("Closed");
		    }
		});*/		
		//menu bar
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);

		JMenuItem mntmCacheLocation = new JMenuItem("Set Cache Location");
		mntmCacheLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Select Firefox cache dir");
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				chooser.setAcceptAllFileFilterUsed(false);  

				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) 
				{ 		
					modifiedCacheLocation = chooser.getSelectedFile().getAbsolutePath();
				}   
			}
		});
		mnSettings.add(mntmCacheLocation);

		JMenuItem mntmSetPollingRate = new JMenuItem("Set Polling Rate");
		mntmSetPollingRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String pollingRateString = (String)JOptionPane.showInputDialog(
						frame,
						"Set Database polling rate (ms), Default = 1000 ms, current = " + DataBasePollPresetPK.pollingRate + " ms"
								+ "\nRestart polling to take effect",
								"Polling rate",
								JOptionPane.PLAIN_MESSAGE,
								null,
								null,
						"1000");

				if(pollingRateString == null)
				{
					JOptionPane.showMessageDialog(frame, "Choosing  Default = 1000 ms polling rate");
					DataBasePollPresetPK.pollingRate = 1000;
				}
				else
				{
					try
					{
						DataBasePollPresetPK.pollingRate = Integer.parseInt(pollingRateString);
					}
					catch(Exception ex)
					{
						JOptionPane.showMessageDialog(frame, "Invalid polling rate, Choosing  Default = 1000 ms polling rate");
						DataBasePollPresetPK.pollingRate = 1000;
					}
				}
			}
		});
		mnSettings.add(mntmSetPollingRate);

		JMenuItem mntmShowPollingWindow = new JMenuItem("Show Polling Window");
		mntmShowPollingWindow.setEnabled(false);

		mntmShowPollingWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				dPool.frame.setVisible(true);
			}
		});
		mnSettings.add(mntmShowPollingWindow);

		JCheckBoxMenuItem menuBackgroundAssembling = new JCheckBoxMenuItem("Background Assembling");
		menuBackgroundAssembling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AppMain.backGroundAssembling = menuBackgroundAssembling.getState();
				System.out.println(AppMain.backGroundAssembling);
			}
		});
		mnSettings.add(menuBackgroundAssembling);	

		JMenuItem mntmNativeMessagingSetup = new JMenuItem("Native Messaging Setup");
		mntmNativeMessagingSetup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					NativeMessageSetUp.setUp(frame);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		mnSettings.add(mntmNativeMessagingSetup);

		JMenu mnCoolStuff = new JMenu("Cool Stuff");
		menuBar.add(mnCoolStuff);

		JMenuItem mntmCovertBrowsing = new JMenuItem("Covert Browsing");
		mnCoolStuff.add(mntmCovertBrowsing);

		JMenuItem mntmMessenger = new JMenuItem("Messenger");
		mntmMessenger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							ChatApp window = new ChatApp();
							window.frame.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
		mnCoolStuff.add(mntmMessenger);

		JMenuItem mntmFeedViewer = new JMenuItem("Feed Viewer");
		mntmFeedViewer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							FeedViewer window = new FeedViewer();
							window.frame.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
		mnCoolStuff.add(mntmFeedViewer);

		//TODO this is still blocking
		mntmCovertBrowsing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				/*CovertBrowserSA window = new CovertBrowserSA();	
				window.open();*/
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							CovertBrowserSA window = new CovertBrowserSA();				
							window.open();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				/*Display.getDefault().syncExec(new Runnable() {
				    public void run() {
				    	try {
							CovertBrowserSA window = new CovertBrowserSA();				
							window.open();
						} catch (Exception e) {
							e.printStackTrace();
						}
				    }
				});*/

				/*Runnable myRunnable = new Runnable(){


					public void run(){
						try {
							CovertBrowserSA window = new CovertBrowserSA();				
							window.open();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				covertThread = new Thread(myRunnable);
				covertThread.start();*/
			}
		});


		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JOptionPane.showMessageDialog(frame, ENV.ABOUT_MESSAGE, "about", JOptionPane.INFORMATION_MESSAGE, frameIcon);
			}
		});
		mnHelp.add(mntmAbout);

		///

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		frame.getContentPane().add(panel_1, BorderLayout.NORTH);	

		txtQq = new JTextField("90I1INgfeam-0JwxP2Vfgw9eSQGQjz3WxLO1wu1n8Cg=");
		txtQq.setToolTipText("");
		txtQq.setHorizontalAlignment(SwingConstants.LEFT);
		panel_1.add(txtQq);
		txtQq.setColumns(25);

		JButton btnSetServerPk = new JButton("Set Server PK");
		panel_1.add(btnSetServerPk);

		JLabel lblNewLabel = new JLabel("Using no PK");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);

		panel_1.add(lblNewLabel);

		btnSetServerPk.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{

				pkText = txtQq.getText();
				if(pkText == null || pkText.length() == 0)
				{
					lblNewLabel.setText("PK not set");
				}
				else
				{
					try
					{
						AppMain.tableChecker.setPK(pkText);
						lblNewLabel.setText("PK set : " + Base64.getUrlEncoder().encodeToString(tableChecker.ServerpublicKey));
					}
					catch(Exception ex)
					{
						lblNewLabel.setText("Invalid PK");
					}

				}
			}
		});

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);

		/*JPanel panelMid = new JPanel();
		frame.getContentPane().add(panelMid, BorderLayout.CENTER);*/



		DefaultTableModel model = new DefaultTableModel() { 
			private static final long serialVersionUID = 1L;
			String[] col = {"Source","URL", "View Data", "Progress"};

			@Override 
			public int getColumnCount() { 
				return col.length; 
			} 

			@Override 
			public String getColumnName(int index) { 
				return col[index]; 
			} 

			@Override
			public boolean isCellEditable(int row, int column) {
				if(column != 2)
					return false;
				else
					return true;

			}

		};

		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setAutoCreateRowSorter(true);

		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.getContentPane().add(scrollPane);

		JButton btnLoadTable;
		JButton btnDumpTable = new JButton("Dump Table");
		btnDumpTable.setEnabled(false);

		table.setVisible(false);

		if(AppMain.tableChecker == null)
		{
			System.err.println("NULL app");
		}
		JLabel progressLabel = new JLabel("|");

		JButton btnStartPolling = new JButton("Start Polling");
		btnStartPolling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if(!startPolling)
				{
					if(pkText == null || pkText.length() == 0)
					{
						JOptionPane.showMessageDialog(frame, "Server public key is not set");
						return;
					}

					//polling queue
					EventQueue.invokeLater(new Runnable() 
					{
						public void run() 
						{
							try 
							{
								dPool = new DataBasePollPresetPK(pkText);
								dPool.setProgressLabel(progressLabel);
								mntmShowPollingWindow.setEnabled(true);
								//dPool.frame.setVisible(true);
							} 
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}
					});
					btnStartPolling.setText("Stop Polling");
					startPolling = true;

				}
				else
				{

					dPool.stopPoll();

					mntmShowPollingWindow.setEnabled(false);
					btnStartPolling.setText("Start Polling");
					startPolling = false;		

				}
			}
		});


		panel.add(progressLabel);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panel.add(horizontalStrut_1);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panel.add(horizontalStrut);
		panel.add(btnStartPolling);
		//app.verifyMessage();

		btnLoadTable = new JButton("Load Table");
		panel.add(btnLoadTable);


		JButton btnVerifySignature = new JButton("Verify Signature");
		btnVerifySignature.setEnabled(false);
		panel.add(btnVerifySignature);


		btnLoadTable.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				//TableVerify.tableChecker = new TableChecker();
				//try to reload from the database in case there is any update
				boolean missingTable = false;
				try 
				{	
					if(!ENV.MULTIPLE_PROVIDER_SUPPORT)
					{
						if(modifiedCacheLocation == null)
							AppMain.tableChecker.loadtableData();
						else
							AppMain.tableChecker.loadtableData(modifiedCacheLocation);
					}
					else
					{
						if(modifiedCacheLocation == null)
							AppMain.tableChecker.loadtableDataMultipleProvider();
						else
							AppMain.tableChecker.loadtableDataMultipleProvider(modifiedCacheLocation);
					}
					//auto dump slice table in the slice table location

					String sliceTableDump = AppMain.tableChecker.sliceJson;
					String sliceTable = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE;
					FileWriter fwSliceTableDump = null;
					try {
						fwSliceTableDump = new FileWriter(sliceTable);
					} catch (IOException e2) {
						JOptionPane.showMessageDialog(frame, "Error in locating slice tabel file");

						e2.printStackTrace();
					}
					try {
						fwSliceTableDump.append(sliceTableDump);
					} catch (IOException e3) {
						JOptionPane.showMessageDialog(frame, "Error in saving slice table file");
						e3.printStackTrace();
					}

				} 
				catch (SQLException e1) 
				{
					e1.printStackTrace();
					JOptionPane.showMessageDialog(frame,
							"Houston we have a problem. Some problem with database access",
							"Error",
							JOptionPane.ERROR_MESSAGE);

					return;
				}
				catch (NullPointerException e2) {
					e2.printStackTrace();
					JOptionPane.showMessageDialog(frame,
							"Houston we have a problem. Long story short we have no idea",
							"Error",
							JOptionPane.ERROR_MESSAGE);

					return;
				}
				catch(RuntimeException e3)
				{
					if(e3.getMessage().equals(ENV.EXCEPTION_FOUNTAIN_TABLE_MISSING))
					{
						JOptionPane.showMessageDialog(frame,
								"Table is missing from the database.",
								"Missing table",
								JOptionPane.ERROR_MESSAGE);
						missingTable = true;
					}
				}

				if(!missingTable)
				{
					String[] sourceKeys = tableChecker.getOriginKeysFromTable();

					Object[][] tableModelData = new Object[tableChecker.getRowCount()][4];

					//System.out.println(tableChecker.getRowCount());

					int i = 0;
					for(String sourceKey : sourceKeys)
					{
						for(String url : TableChecker.SOURCE_KEY_URL_MAP.get(sourceKey))
						{
							tableModelData[i][0] = sourceKey;
							tableModelData[i][1] = url;
							tableModelData[i][2] = "Go";
							tableModelData[i][3] = null;
							i++;
						}
					}

					model.setDataVector(tableModelData, new Object[]{"Source","URL", "View Data", "Progress"});

					table.getColumn("Progress").setCellRenderer(new ProgressCellRender_1());
					table.getColumn("View Data").setCellRenderer(new ButtonRenderer());
					table.getColumn("View Data").setCellEditor(new ButtonEditor(new JCheckBox(), table));
					table.setVisible(true);

					btnVerifySignature.setEnabled(true);
					btnDumpTable.setEnabled(true);
				}
			}
		});


		btnVerifySignature.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if(!ENV.MULTIPLE_PROVIDER_SUPPORT)
				{
					try {
						boolean ret = tableChecker.verifyMessage();

						if(!ret)
						{
							JOptionPane.showMessageDialog(frame, "Error! PK not set");
							return;
						}

						if(tableChecker.verifyResult)
							JOptionPane.showMessageDialog(frame, "Table verification successful!!");

						else
							JOptionPane.showMessageDialog(frame, "Table verification fail!!");

					} catch(NullPointerException ex) {
						JOptionPane.showMessageDialog(frame, "Error! PK not set");
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(frame, "Exception happened in signature verification");
					}
				}

				else
				{
					try {
						boolean sliceTableVerifyRes = tableChecker.verifySliceTable();
						boolean ret = tableChecker.verifyMessageMultipleProvider();
						if(!ret)
						{
							JOptionPane.showMessageDialog(frame, "Error! PK not set");
							return;
						}

						List<String> failedSigOriginKeys = TableChecker.verifyMessageList();

						if(failedSigOriginKeys.size() == 0)
						{
							if(sliceTableVerifyRes)
								JOptionPane.showMessageDialog(frame, "Table verification successful!!");
							else
								JOptionPane.showMessageDialog(frame, "Table verification successful!!\n Interactive table verify failed!!");
						}
						else
						{
							StringBuffer stb = new StringBuffer();
							for(String key : failedSigOriginKeys)
								stb.append(key).append("\n");

							JOptionPane.showMessageDialog(frame, "Table verification fail for keys : \n" + stb.toString());
						}

					} 
					catch (NullPointerException e1) {
						JOptionPane.showMessageDialog(frame, "Error! PK not set");
						e1.printStackTrace();
					}
					catch(RuntimeException e2)
					{
						if(e2.getMessage().equals(ENV.EXCEPTION_FOUNTAIN_TABLE_MISSING))
							JOptionPane.showMessageDialog(frame, "Fountain table missing from the database file");
					}
					catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(frame, "Exception happened in signature verification");
					}

				}

			}
		});



		btnDumpTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if(!ENV.MULTIPLE_PROVIDER_SUPPORT)
				{
					String dump = tableChecker.tableDumpJson;
					String sliceTableDump = tableChecker.sliceJson;
					FileWriter fwTableDump = null, fwSliceTableDump = null;
					try {
						fwTableDump = new FileWriter(ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_TABLE_DUMP);
						fwSliceTableDump = new FileWriter(ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE);

					} catch (IOException e1) {
						JOptionPane.showMessageDialog(frame, "Error in file");
						e1.printStackTrace();
					}
					try {
						fwTableDump.append(dump);
						fwSliceTableDump.append(sliceTableDump);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(frame, "Error in file");					
						e1.printStackTrace();
					}
					try {
						fwTableDump.close();
						fwSliceTableDump.close();
						JOptionPane.showMessageDialog(frame, "Table dumped @ \n" + ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_TABLE_DUMP);

					} catch (IOException e1) {

						JOptionPane.showMessageDialog(frame, "Error in closing file!");
						e1.printStackTrace();
					}

				}

				else
				{
					List<String[]> dumpList = tableChecker.multipleProviderRows;
					String sliceTableDump = tableChecker.sliceJson;
					String sliceTable = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE;
					FileWriter fwSliceTableDump = null;
					try {
						fwSliceTableDump = new FileWriter(sliceTable);
					} catch (IOException e2) {
						JOptionPane.showMessageDialog(frame, "Error in file");

						e2.printStackTrace();
					}
					try {
						fwSliceTableDump.append(sliceTableDump);
					} catch (IOException e3) {
						JOptionPane.showMessageDialog(frame, "Error in file");
						e3.printStackTrace();
					}
					try {
						fwSliceTableDump.close();
					} catch (IOException e2) {
						JOptionPane.showMessageDialog(frame, "Error in closing file!");						
						e2.printStackTrace();
					}
					for(String[] dumpRow : dumpList)
					{
						FileWriter fwTableDump = null;
						try {
							//Due to the batshit provider names I am calculating sha256 of the provider name and keep it as the file name.
							//I will also add the provider name on the first row of the table

							byte[] providerNameBytes = dumpRow[1].getBytes();
							MessageDigest digest = MessageDigest.getInstance("SHA-256");
							byte[] hashBytes = digest.digest(providerNameBytes);
							String fileName = Base64.getUrlEncoder().encodeToString(hashBytes);

							fwTableDump = new FileWriter(ENV.APP_STORAGE_LOC + 
									ENV.DELIM + ENV.APP_STORAGE_TABLE_MULTIPLE_PROVIDER_DUMP 
									+ "_" + fileName
									+ ENV.APP_JSON_EXTENSION);
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(frame, "Error in file");
							e1.printStackTrace();
						} catch (NoSuchAlgorithmException e1) {
							JOptionPane.showMessageDialog(frame, "Some problem");
						}
						try {
							fwTableDump.append(dumpRow[1] + "\n" + new JSONObject(dumpRow[0]).toString(2));
						} catch (IOException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(frame, "Dump error");
						}
						try {
							fwTableDump.close();

						} catch (IOException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(frame, "Close error");
						}
					}

					JOptionPane.showMessageDialog(frame, "Tables dumped");
				}

			}
		});
		panel.add(btnDumpTable);


		//set auto server pk
		if(ENV.AUTO_PILOT)
		{
			pkText = txtQq.getText();
			try
			{
				AppMain.tableChecker.setPK(pkText);		
				lblNewLabel.setText("PK set : " + Base64.getUrlEncoder().encodeToString(tableChecker.ServerpublicKey));
				System.out.println("PK set : " + Base64.getUrlEncoder().encodeToString(tableChecker.ServerpublicKey));
			}
			catch(Exception ex)
			{
				lblNewLabel.setText("Invalid PK");
			}
			//start the polling here
			if(!ArgumentProcess.chrome_native)
			{
				if(!startPolling)
				{
					if(pkText == null || pkText.length() == 0)
					{
						JOptionPane.showMessageDialog(frame, "Server public key is not set");
						return;
					}

					//polling queue
					EventQueue.invokeLater(new Runnable() 
					{
						public void run() 
						{
							try 
							{
								dPool = new DataBasePollPresetPK(pkText);
								dPool.setProgressLabel(progressLabel);
								mntmShowPollingWindow.setEnabled(true);
								//dPool.frame.setVisible(true);
							} 
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}
					});
					btnStartPolling.setText("Stop Polling");
					startPolling = true;

				}

				else
				{
					if(dPool != null)
						dPool.stopPoll();

					mntmShowPollingWindow.setEnabled(false);
					btnStartPolling.setText("Start Polling");
					startPolling = false;		

				}
			}
		}


		JButton btnKeyGen = new JButton("Key Gen");
		btnKeyGen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				byte[] keyBytes = new byte[ENV.AES_KEY_SIZE];
				//LOL
				Arrays.fill(keyBytes, (byte) 0x00);
				FileOutputStream fw_bin = null;
				try {
					fw_bin = new FileOutputStream(ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_KEY_FILE);
					fw_bin.write(keyBytes);
					fw_bin.close();
					//JOptionPane.showMessageDialog(frame, "Key file generated in " + ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_KEY_FILE);

					AppMain.key = new SecretKeySpec(keyBytes, "AES");
					AppMain.ivSpec = new IvParameterSpec(ivBytes);
					try {
						AppMain.cipher = Cipher.getInstance("AES/CTR/NoPadding");
						cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
					} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) 
					{
						e.printStackTrace();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		panel.add(btnKeyGen);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		panel.add(horizontalStrut_2);

		//autoChat

		if(ArgumentProcess.autoChat && ArgumentProcess.autoChatInterval > 0)
		{

			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			System.out.println("     Autochat started");
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			//String chatBase64 = "020000004722EC7E646466B39C3CC79D8AD64ABA00000002370EFE014AEC2FD83DD6D78F0D12968C";

			byte[] toSend = DatatypeConverter.parseHexBinary("020000004722EC7E646466B39C3CC79D8AD64ABA00000002370EFE014AEC2FD83DD6D78F0D12968C");
			String chatBase64 = Base64.getEncoder().encodeToString(toSend);
			Runnable myRunnable = new Runnable() {

				@Override
				public void run() {
					try {
						TCPClient.connectToBrowser(chatBase64);
						System.out.println(">> autochat dispatched");
					} catch (Exception e1) {
						System.err.println("Error at composing the auto chat messages");
					}
				}
			};

			ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
			executor.scheduleAtFixedRate(myRunnable, 0, ArgumentProcess.autoChatInterval, TimeUnit.MILLISECONDS);			
		}

	}

}
