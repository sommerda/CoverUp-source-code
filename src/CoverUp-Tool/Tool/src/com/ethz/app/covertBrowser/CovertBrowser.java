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
package com.ethz.app.covertBrowser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ethz.app.dispatchSocket.TCPClient;
import com.ethz.app.env.ENV;
import com.ethz.app.poll.RepeatedDatabaseCheck;
import com.ethz.tree.Node;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Aritra
 *
 */
public class CovertBrowser {

	protected Shell shell;
	private Text text;

	private Text portText;
	private int port;
	private ProxyServer ps;
	private boolean serverClosed;
	public static Set<Long> sliceIdSet = new HashSet<>();
	private com.ethz.tree.Tree sliceTree;
	private TreeItem treeItem0;
	private Set<String> exploredTree;
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			System.setProperty("http.proxySet", "true");
			System.setProperty("http.proxyHost", "127.0.0.1");
			System.setProperty("http.proxyPort", "9700");
			System.setProperty("https.proxyHost", "127.0.0.1");
			System.setProperty("https.proxyPort", "9700");

			CovertBrowser window = new CovertBrowser();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public CovertBrowser() {
		this.ps = null;
		this.serverClosed = false;
		this.exploredTree = new HashSet<>();
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		Image small = new Image(display,"assets//hb.jpg");
		shell.setImage(small);    
		
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}


	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1459, 1003);
		shell.setText("Covert Browser");

		shell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent paramDisposeEvent) {
				if(serverClosed)
				{
					try {
						ps.stopServer();
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
			}
		});
		
		Browser browser = new Browser(shell, SWT.NONE);

		browser.setBounds(275, 85, 1156, 861);
		browser.setJavascriptEnabled(true);	
		

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(10, 3, 1404, 38);
		
		Label lbllinks = new Label(composite, SWT.NONE);
		lbllinks.setBounds(1154, 13, 90, 20);
		lbllinks.setText("#links");
		
		
		Tree tree = new Tree(shell, SWT.BORDER);
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				try
				{
					//String sliceIdStr = tree.getItem(tree.getSelectionIndex());
					//TODO for testing only
					//long sliceIdL = CovertHTMLUtils.sliceMap.get(sliceIdStr);
					
					String selectedtext = tree.getSelection()[0].getText();
					Node selectedNode = sliceTree.nodeMap.get(selectedtext);
					
					if(!selectedtext.equals("ROOT"))
					{
						if(!CovertBrowserUtility.checkSliceFolder(selectedNode.sliceId))
						{
							sliceIdSet.add(selectedNode.sliceId);
							lbllinks.setText(new Integer(sliceIdSet.size()).toString());
						}
					}
					
					if(!selectedNode.isLeaf())
					{	
						if(!exploredTree.contains(selectedtext))
						{
							exploredTree.add(selectedtext);
							for(Node child : selectedNode.children)
							{
								TreeItem treeItemInnter = new TreeItem(treeItem0, 0);
								treeItemInnter.setText(child.sliceName);
								if(!CovertBrowserUtility.checkSliceFolder(child.sliceId))
									treeItemInnter.setForeground(tree.getDisplay().getSystemColor(SWT.COLOR_RED));
							}
						}
					}
					
					if(tree.getSelection()[0].getText() == "ROOT")
						return;
					
				    long sliceIdL = selectedNode.sliceId;
					try {
						byte[] assembledDataBytes = CovertBrowserUtility.assembleSlices(sliceIdL);
						
						if(assembledDataBytes == null)
						{
							browser.setText("<html><body><h1>Not locatated in disk</h1></body></html>");
							return;
						}
						
						//String assembledData = new String(assembledDataBytes, StandardCharsets.UTF_8);
						String assembledData = new String(assembledDataBytes, StandardCharsets.UTF_8);
						browser.setText(assembledData);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					MessageBox messageBox = new MessageBox(shell ,SWT.ICON_INFORMATION);
					messageBox.setMessage("Something went wrong as always : \n" + ex.toString());
					messageBox.setText("Eror");
					messageBox.open();
					
				}
			}
		});
		tree.setBounds(10, 85, 259, 861);
		
		Button backButton = new Button(shell, SWT.NONE);
		backButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				browser.back();
			}
		});
		backButton.setBounds(275, 47, 35, 30);
		backButton.setText("<-");

		Button forwardButton = new Button(shell, SWT.NONE);
		forwardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.forward();
			}
		});
		forwardButton.setBounds(315, 47, 35, 30);
		forwardButton.setText("->");
		
		//browser.setUrl("C:\\Users\\Aritra\\workspace_Mars_new\\UndergroundApp\\a.htm");
		//browser.setUrl("http://forum.codecall.net/topic/57029-simple-java-web-browser/");

		
		/*
		browser.execute("Components.utils.import(\"resource://gre/modules/Services.jsm\");");
		browser.execute("Components.classes[\"@mozilla.org/preferences-service;1\"].getService(Components.interfaces.nsIPrefBranch).setIntPref(\"network.proxy.type\", 1);");
		browser.execute("Components.classes[\"@mozilla.org/preferences-service;1\"].getService(Components.interfaces.nsIPrefBranch).setIntPref(\"network.proxy.http_port\",9700);");
		browser.execute("Components.classes[\"@mozilla.org/preferences-service;1\"].getService(Components.interfaces.nsIPrefBranch).setIntPref(\"network.proxy.http\",\"127.0.0.1\");");
		*/
		
		Button btnLoadCovertSite = new Button(shell, SWT.NONE);
		btnLoadCovertSite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				String sliceTableDataJSON = null;
				try {
					sliceTableDataJSON = CovertBrowserUtility.getSliceTable();
				} catch (IOException e1) {
					MessageBox messageBox = new MessageBox(shell ,SWT.ICON_INFORMATION);
					messageBox.setMessage("Something went wrong as always : \n" + e1.toString());
					messageBox.setText("Eror");
					messageBox.open();
				}
				
				sliceTree = new com.ethz.tree.Tree(sliceTableDataJSON);
				tree.removeAll();
				exploredTree.clear();
				treeItem0 = new TreeItem(tree, 0);
				treeItem0.setText("ROOT");
			}
		});
		btnLoadCovertSite.setBounds(10, 47, 165, 30);
		btnLoadCovertSite.setText("Load Covert site tree");
		
		Combo combo = new Combo(shell, SWT.READ_ONLY);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				try
				{
					//String sliceIdStr = combo.getItem(combo.getSelectionIndex());
					//TODO for testing only
					//long sliceIdL = CovertHTMLUtils.sliceMap.get(sliceIdStr);
					long sliceIdL = Long.parseLong(combo.getItem(combo.getSelectionIndex()));
					try {
						byte[] assembledDataBytes = CovertBrowserUtility.assembleSlices(sliceIdL);
						
						//String assembledData = new String(assembledDataBytes, StandardCharsets.UTF_8);
						String assembledData = new String(assembledDataBytes, StandardCharsets.UTF_8);
						browser.setText(assembledData);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					MessageBox messageBox = new MessageBox(shell ,SWT.ICON_INFORMATION);
					messageBox.setMessage("Something went wrong as always : \n" + ex.toString());
					messageBox.setText("Eror");
					messageBox.open();
					
					browser.setText("<html><body><h1>Something went really wrong</h1></body></html>");
				}

			}
		});
		combo.setBounds(1143, 49, 271, 28);
		
		
		Button btnAvailableData = new Button(shell, SWT.NONE);
		btnAvailableData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				combo.removeAll();
				String[] sliceIds = CovertBrowserUtility.getLocalSliceIds();

				for(String sliceId : sliceIds)
				{
					combo.add(sliceId);
				}
			}
		});
		btnAvailableData.setBounds(1029, 47, 108, 30);
		btnAvailableData.setText("Available data");

		browser.addLocationListener(new LocationListener() {

			@Override
			public void changing(LocationEvent event) {

				//System.out.println(event.location);
				if(!event.location.contains("127.0.0.1") && !event.location.contains("about:blank"))
				{		
					browser.setText("<html><body><h1>Requested for  " + event.location +"  <a href=\"http://127.0.0.1:9700/flag=" + event.location + "\">bla</a></h1></body></html>");
					event.doit = false;
				}
				//event.location = "http://127.0.0.1:9070";
				//System.out.println("bla");
				//event.doit = false;
				//urlList.add(paramLocationEvent.location);
				//lbllinks.setText(new Integer(sliceIdSet.size()).toString());
			}

			@Override
			public void changed(LocationEvent paramLocationEvent) {
				// TODO Auto-generated method stub
			}
		});


		text = new Text(composite, SWT.BORDER);
		text.setBounds(315, 10, 544, 26);
		text.setText("http://127.0.0.1:9700");

		Button btnGo = new Button(composite, SWT.NONE);
		btnGo.setEnabled(false);
		btnGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			}
		});
		btnGo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {

				if(text.getText() != null && text.getText().length() > 0)
				{
					browser.setUrl(text.getText());
				}
			}
		});

		btnGo.setBounds(865, 8, 90, 30);
		btnGo.setText("Go");

		portText = new Text(composite, SWT.BORDER);
		portText.setBounds(0, 10, 97, 28);
		portText.setText("9700");

		Button btnSetPort = new Button(composite, SWT.NONE);
		btnSetPort.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnSetPort.setBounds(103, 8, 120, 30);
		btnSetPort.setText("Set port + proxy");

		Button btnStop = new Button(composite, SWT.NONE);
		btnStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {

				try {
					ps.stopServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		btnStop.setBounds(224, 8, 70, 30);
		btnStop.setText("Stop");

		/*Button btnLoadCovertStart = new Button(composite, SWT.NONE);
		btnLoadCovertStart.setEnabled(false);

		btnLoadCovertStart.setBounds(974, 8, 167, 30);
		btnLoadCovertStart.setText("Load covert start page");
		 */
		
		Button btnDispatch = new Button(composite, SWT.NONE);
		btnDispatch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if(sliceIdSet.size() == 0)
				{
					MessageBox messageBox = new MessageBox(shell ,SWT.ICON_INFORMATION);
					messageBox.setMessage("Nothing to dispatch");
					messageBox.setText("Message");
					messageBox.open();
				}
				else if(RepeatedDatabaseCheck.stored_droplet_counter < ENV.DISPACTH_REQUEST_THRESHOLD)
				{
					MessageBox messageBox = new MessageBox(shell ,SWT.ICON_INFORMATION);
					messageBox.setMessage("Stored droplet count less than threshold value. Unsafe!");
					messageBox.setText("Message");
					messageBox.open();
				}
				else
				{
					try {					

						FileOutputStream fw = new FileOutputStream(ENV.APP_STORAGE_LOC + ENV.DELIM + 
								ENV.APP_STORAGE_SLICE_ID_FILES_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_ID_FILE);
						byte[] out = new byte[Long.BYTES * sliceIdSet.size()];
						int tillNow = 0;
						for(long sliceId : sliceIdSet)
						{
							byte[] sliceBytes = ByteBuffer.allocate(Long.BYTES).putLong(sliceId).array();
							System.arraycopy(sliceBytes, 0, out, tillNow, sliceBytes.length);
							tillNow += sliceBytes.length;
						}
						fw.write(out);
						fw.close();
												
						
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					};
					
					synchronized(this)
					{
						RepeatedDatabaseCheck.stored_droplet_counter = 0;
					}
					
					MessageBox messageBox = new MessageBox(shell ,SWT.ICON_INFORMATION);
					messageBox.setMessage("Slice ids dispatched in local storage");
					messageBox.setText("Message");
					messageBox.open();

					sliceIdSet.clear();
					lbllinks.setText(new Integer(sliceIdSet.size()).toString());
				}
			}
		});
		btnDispatch.setBounds(1288, 8, 106, 30);
		btnDispatch.setText("Dispatch");


		btnSetPort.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {

				port = Integer.parseInt(portText.getText());
				btnGo.setEnabled(true);

				ProxyServer.setProxy(port);
				try {
					ps = new ProxyServer(port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				ps.startServer();
				serverClosed = true;
				//btnLoadCovertStart.setEnabled(true);

			}
		});	

		//html gen
		/*btnLoadCovertStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {

				String sliceFile = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE;
				if(!new File(sliceFile).exists())
				{
					MessageBox messageBox = new MessageBox(shell ,SWT.ERROR);
					messageBox.setMessage("Slice table not exists in APP_DATA");
					messageBox.setText("Error");
					messageBox.open();
				}
				else
				{
					String sliceStartPageHtml = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_COVERT_BROWSER_START_PAGE;
					try {
						CovertHTMLUtils.covertHTMLStartPageGenerator(sliceStartPageHtml, sliceFile, port);
					} catch (IOException e) {
						e.printStackTrace();
					}

					File sliceStartPage = new File(sliceStartPageHtml);
					String fullLocation = sliceStartPage.getAbsolutePath();
					browser.setUrl(fullLocation);
				}

			}
		});*/
	}
}
