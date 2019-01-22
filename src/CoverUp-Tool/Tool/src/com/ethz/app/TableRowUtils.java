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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;


import org.json.JSONObject;

import com.ethz.app.dbUtils.TableChecker;
import com.ethz.app.env.ENV;

class ButtonRenderer extends JButton implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ButtonRenderer() {
		setOpaque(true);

	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) 
	{
		if (isSelected) 
		{
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} 
		else 
		{
			setForeground(table.getForeground());
			setBackground(UIManager.getColor("Button.background"));

			//setText((value == null) ? "" : value.toString());
		}
		setText((value == null) ? "" : value.toString());
		return this;
	}
}

class ButtonEditor extends DefaultCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JButton button;
	private String label;
	private boolean isPushed;
	private int row;
	private int column;
	private JTable table;

	public ButtonEditor(JCheckBox checkBox, JTable table) {
		super(checkBox);
		this.table = table;

		button = new JButton();
		button.setOpaque(true);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//button.setText("Selected");
				fireEditingStopped();
			}
		});
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		this.row = row;
		this.column = column;

		if (isSelected) 
		{

			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
		} 
		else 
		{
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
		}
		label = (value == null) ? "" : value.toString();
		button.setText(label);
		isPushed = true;

		return button;
	}

	@Override
	public Object getCellEditorValue() {
		if (isPushed) {
			String urlString = table.getValueAt(row, 1).toString();

			//test
			//System.out.println(urlString);

			//TableHandler.insertDataToTable("index-CCC", linkStr);
			//JOptionPane.showMessageDialog(button, "Database insert success!!");

			//call the assemble window from here

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					//table.getColumn("Progress").setCellRenderer(new ProgressCellRender());

					try {

						JSONObject fountainTableRowSpecific =  TableChecker.URL_JSON_TABLE_MAP.get(urlString);
						//get the fountain specific seed and set it to the assembler scope
						String seedStr = fountainTableRowSpecific.getString("seed");
						Integer dataLength = fountainTableRowSpecific.getInt("len");

						Integer dropletCount = 0;
						if(ENV.EXPERIMENTAL)
							dropletCount = fountainTableRowSpecific.getInt("dropletCount");

						AssembleFrame window = new AssembleFrame(urlString, dataLength, dropletCount);
						window.setSeed(seedStr);
						window.frame.setVisible(true);

						//automated droplet assemble
						String dropletDirID = fountainTableRowSpecific.getString("dropletLoc");
						String JSONDirPath = ENV.APP_STORAGE_LOC + ENV.DELIM + dropletDirID;
						byte[] decodedData_out = new byte[dataLength];
						int progress = AssembleFrameUtils.assembleDroplets_NonFrame(JSONDirPath, decodedData_out);
						table.getModel().setValueAt(progress, row, column + 1);
						//table.getColumn("Progress").setCellRenderer(new ProgressCellRender_1(65, row, column + 1));

					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
			});       	
		}
		isPushed = false;
		return label;
	}


	@Override
	public boolean stopCellEditing() {
		isPushed = false;
		//button.setText("Selected");
		return super.stopCellEditing();
	}

	@Override
	protected void fireEditingStopped() {
		//button.setText("Selected");
		super.fireEditingStopped();
	}
}

class ValRow
{
	public static volatile Map<String, Integer> progress_map = new HashMap<>();
}

