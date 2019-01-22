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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import javax.swing.UnsupportedLookAndFeelException;

import com.ethz.app.poll.DataBasePoll;

import javax.swing.JLabel;
import java.awt.FlowLayout;

import javax.swing.SwingConstants;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OldAppWindow {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static MessageUtil app;
	private JTextField txtQq;

	JFileChooser chooser;
	
	public static boolean set = false;
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());	
			
		EventQueue.invokeLater(new Runnable() {
			public void run() 
			{
				try {

					OldAppWindow window = new OldAppWindow();
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
	 * @throws NoSuchAlgorithmException 
	 * @throws SQLException 
	 */
	@SuppressWarnings("static-access")
	public OldAppWindow() throws NoSuchAlgorithmException, SQLException {
		
		try
		{
			this.app = new MessageUtil();
			app.loadMessage();
			app.loadSignature();
		}
		catch(Exception ex)
		{
			chooser = new JFileChooser(); 
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Defalt derectory discovery fail. Select Firefox cache dir");
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			chooser.setAcceptAllFileFilterUsed(false);  
			chooser.setAcceptAllFileFilterUsed(false);  

			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) 
			{ 		
				String path = chooser.getSelectedFile().getAbsolutePath();
				this.app = new MessageUtil(path);
				app.loadMessage();
				app.loadSignature();
			}
			
		}
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws NoSuchAlgorithmException 
	 */
	private void initialize() throws NoSuchAlgorithmException 
	{
		
		frame = new JFrame();
		frame.setTitle("Firefox cache extractor");
		frame.setBounds(100, 100, 783, 510);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		//frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		

		
		//tabbedPane.add("wc", panel);
		
		

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		JTextArea textArea = new JTextArea();
		textArea.setForeground(Color.BLACK);;
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		scrollPane.setColumnHeaderView(panel_1);

		txtQq = new JTextField();
		txtQq.setToolTipText("");
		txtQq.setHorizontalAlignment(SwingConstants.LEFT);
		panel_1.add(txtQq);
		txtQq.setColumns(25);

		JButton btnLoadMessage;

		JButton btnSetServerPk = new JButton("Set Server PK");
		panel_1.add(btnSetServerPk);
		
		JLabel lblNewLabel = new JLabel("Using no PK");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		//lblNewLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel_1.add(lblNewLabel);
		
		btnSetServerPk.addActionListener(new ActionListener() 
		{
			
			public void actionPerformed(ActionEvent e) 
			{

				String pkText = txtQq.getText();
				if(pkText == null || pkText.length() == 0)
				{
					lblNewLabel.setText("PK not set");
				}
				else
				{
					try
					{
						app.setPK(pkText);
						lblNewLabel.setText("PK set : " + Base64.getUrlEncoder().encodeToString(app.ServerpublicKey));
					}
					catch(Exception ex)
					{
						lblNewLabel.setText("Invalid PK");
					}

				}
			}
		});
		
		

		if(app == null)
		{
			System.err.println("NULL app");
		}
		//app.verifyMessage();

		btnLoadMessage = new JButton("Load Message");

		btnLoadMessage.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					app.loadMessage();
				} 
				catch (SQLException e1) 
				{
					e1.printStackTrace();
				}
				textArea.setText(app.message);
			}
		});
		panel.add(btnLoadMessage);

		JButton btnLoadSignature = new JButton("Load Signature");
		btnLoadSignature.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				try {
					app.loadSignature();
				} catch ( SQLException e1) {
					e1.printStackTrace();
				}
				textArea.setText(app.signatureString);
			}
		});
		btnLoadSignature.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					app.loadSignature();
				} catch ( SQLException e1) {
					e1.printStackTrace();
				}
				textArea.setText(app.signatureString);
			}
		});
		panel.add(btnLoadSignature);

		JButton btnVerifySignature = new JButton("Verify Signature");
		btnVerifySignature.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {

				try {
					app.verifyMessage();

					if(app.verifyResult)
						textArea.setText("Verify success");

					else
						textArea.setText("Invalid signature");

				} 
				catch(NullPointerException ex)
				{
					textArea.setText("Error! PK not set");
				}
				catch (Exception e1) 
				{
					e1.printStackTrace();
					textArea.setText("Exception happened in signature verification\n-------------------\n"+e1.getClass()+ " " +e1.getMessage());
				}



			}
		});
		panel.add(btnVerifySignature);
		
		//frame.add(tabbedPane);
		
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnMenu = new JMenu("Tool");
		menuBar.add(mnMenu);
		
		JMenuItem mntmDataAssemble = new JMenuItem("Data Assemble");
		mntmDataAssemble.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							AssembleFrame window = new AssembleFrame();
							window.frame.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
		
		JMenuItem mntmTableVerify = new JMenuItem("Table Verify");
		mntmTableVerify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
						
				EventQueue.invokeLater(new Runnable() {
					public void run() 
					{
						try {

							AppMain window = new AppMain();
							window.frame.setVisible(true);
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}
				});
				
			}
		});
		mnMenu.add(mntmTableVerify);
		mnMenu.add(mntmDataAssemble);
		
		//call poll database class here
		JMenuItem mntmPollDatabase = new JMenuItem("Poll Database");
		mntmPollDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				
				EventQueue.invokeLater(new Runnable() 
				{
					public void run() 
					{
						try 
						{
							DataBasePoll dPool = new DataBasePoll();
							dPool.frame.setVisible(true);
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}
				});			
			}
		});
		mnMenu.add(mntmPollDatabase);
		
	}
}
