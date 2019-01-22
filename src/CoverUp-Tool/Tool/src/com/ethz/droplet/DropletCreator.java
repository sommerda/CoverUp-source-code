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

package com.ethz.droplet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DropletCreator {

	protected Shell shell;
	private Text text;
	public static byte[] data;
	public static int chunk_size;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DropletCreator window = new DropletCreator();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
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
		shell.setSize(592, 449);
		shell.setText("Droplet Creator");
		
		Group grpGgbh = new Group(shell, SWT.NONE);
		grpGgbh.setText("Control");
		grpGgbh.setBounds(10, 10, 554, 320);
		
		text = new Text(grpGgbh, SWT.BORDER);
		text.setBounds(142, 133, 118, 26);
		
		Label lblNewLabel = new Label(grpGgbh, SWT.NONE);
		lblNewLabel.setBounds(53, 136, 70, 20);
		lblNewLabel.setText("Chunk Size");
		
		Label lblDataSize = new Label(grpGgbh, SWT.NONE);
		lblDataSize.setText("Data Size");
		lblDataSize.setBounds(53, 97, 70, 20);
		
		Label lblNa = new Label(grpGgbh, SWT.NONE);
		lblNa.setText("N/A");
		lblNa.setBounds(142, 97, 115, 20);
		
		Label lblFile = new Label(grpGgbh, SWT.NONE);
		lblFile.setText("File");
		lblFile.setBounds(53, 59, 70, 20);
		
		Label label_1 = new Label(grpGgbh, SWT.NONE);
		label_1.setText("N/A");
		label_1.setBounds(142, 59, 390, 20);
		
		Button btnBrowse = new Button(shell, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				
					FileDialog fd = new FileDialog(shell);
				 //DirectoryDialog dlg = new DirectoryDialog(shell);
			        
				 	fd.setText("Select file");

			        String dir = fd.open();
			        
			        if (dir != null) {
			        	label_1.setText(dir);
			        	File f = new File(dir);
			        	try {
							data = Files.readAllBytes(f.toPath());
							label_1.setText(dir);
							lblNa.setText(new Integer(data.length).toString() + " bytes");
						} 
			        	catch (IOException e) {
}
			        	
			        }
				
			}
		});
		btnBrowse.setBounds(66, 351, 90, 30);
		btnBrowse.setText("Browse");
		
		Button btnCreateDroplet = new Button(shell, SWT.NONE);
		btnCreateDroplet.addSelectionListener(new SelectionAdapter() {
			
			//create droplet here
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				

				chunk_size = Integer.parseInt(text.getText());
			
				System.out.println(chunk_size);
				
			}
		});
		btnCreateDroplet.setBounds(335, 351, 108, 30);
		btnCreateDroplet.setText("Create droplet");
		
		Button btnDropletLocation = new Button(shell, SWT.NONE);
		btnDropletLocation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setText("Select droplet location");

		        String dir = dlg.open();
		        
		        System.out.println(dir);
			}
		});
		btnDropletLocation.setText("Droplet location");
		btnDropletLocation.setBounds(188, 351, 121, 30);

	}
}
