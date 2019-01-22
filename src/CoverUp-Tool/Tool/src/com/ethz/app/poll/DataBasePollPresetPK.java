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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.Base64;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

import com.ethz.app.AppMain;
import com.ethz.app.ProgressCellRender_1;
import com.ethz.app.dbUtils.ChromeCacheTransfer;
import com.ethz.app.dbUtils.FirefoxCacheExtract;
import com.ethz.app.env.ENV;

public class DataBasePollPresetPK extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9079243269473511003L;
	public JFrame frame;
	private ScheduledThreadPoolExecutor executor;
	public static String databaseFileLocation;
	public static volatile int pollingRate = 1000;
	public JLabel progressLabel;
	public static int progress = 0;
	
	JFileChooser chooser;

	//test main.
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());	

		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					DataBasePollPresetPK dPool = new DataBasePollPresetPK("90I1INgfeam-0JwxP2Vfgw9eSQGQjz3WxLO1wu1n8Cg=");
					dPool.frame.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	
	public void stopPoll()
	{
		executor.shutdown();
		frame.dispose();
	}
	
	public void setProgressLabel(JLabel progressLabel)
	{
		this.progressLabel = progressLabel;
	}
	
	public DataBasePollPresetPK(String serverPublicKey) {

		RepeatedDatabaseCheck.ServerPublickey = Base64.getUrlDecoder().decode(serverPublicKey);

		frame = new JFrame();
		frame.setTitle("Database polling");
		frame.setBounds(100, 100, 783, 510);
		//frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


		/*frame.addWindowListener(new java.awt.event.WindowAdapter() 
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				try
				{
					executor.shutdown();
				}
				catch(Exception ex)
				{

				}
			}
		});*/

		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		JTextArea textArea = new JTextArea();
		textArea.setForeground(Color.GREEN);;
		textArea.setBackground(Color.BLACK);
		Font font = new Font("Consolas", Font.PLAIN, 18);
		textArea.setFont(font);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		scrollPane.setViewportView(textArea);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);


		Runnable myRunnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try 
				{
					if(AppMain.selectedPrimaryBrowser.equals(ENV.BROWSER_CHROME))
						new ChromeCacheTransfer(FirefoxCacheExtract.APP_DATA_LOC_CHROME).transfer();
					
					//System.out.println(DataBasePollPresetPK.databaseFileLocation);
					RepeatedDatabaseCheck t = new RepeatedDatabaseCheck(DataBasePollPresetPK.databaseFileLocation);
					progress %= 4;
					progressLabel.setText(new String(new char[]{ENV.PROGRESS_SYMB[progress++]}));
					//System.out.println(DataBasePoll.databaseFileLocation);
					textArea.append("\n".concat(t.messaage.toString()));
					
					//ProgressCellRender_1.progressBarAutoUpdate(AppMain.table);
				} 
				catch(NullPointerException ex)
				{
					ex.printStackTrace();
					textArea.append("\n".concat("Table not exists"));		
				}

				catch(RuntimeException ex)
				{
					if(ex.getMessage().equals(ENV.EXCEPTION_FOUNTAIN_TABLE_MISSING))
					{
						textArea.append("\nTable does not exist in the database");
						RepeatedDatabaseCheck.count %= 4;
						textArea.append("\n---------------" + ENV.PROGRESS_SYMB[RepeatedDatabaseCheck.count++] + "---------------");
					}
					else
						textArea.append("\n".concat(ex.getMessage()));

				}
				catch (Exception e) 
				{
					e.printStackTrace();
					textArea.append("\n".concat("Some other Problem!"));						
				}

			}
		};
		//Taking an instance of class contains your repeated method.

		executor = new ScheduledThreadPoolExecutor(2);
		executor.scheduleAtFixedRate(myRunnable, 0, pollingRate, TimeUnit.MILLISECONDS);

	}

}
