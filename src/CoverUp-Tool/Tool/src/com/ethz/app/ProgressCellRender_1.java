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

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.json.JSONObject;

import com.ethz.app.dbUtils.TableChecker;
import com.ethz.app.env.ENV;

/**
 * @author Aritra
 *
 */
public class ProgressCellRender_1 extends JProgressBar implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		int progress = 0;

		if (value instanceof Float)
		{
			progress = Math.round(((Float) value) * 100f);
		} 
		else if (value instanceof Integer) 
		{
			progress = (int) value;
		}

		setStringPainted(true);
		setValue(progress);
		return this;
	}


	public static void progressBarAutoUpdate(JTable table)
	{
		int rowCount = table.getRowCount();
		for(int row = 0; row < rowCount; row++)
		{
			String urlString = table.getValueAt(row, 1).toString();
			JSONObject fountainTableRowSpecific =  TableChecker.URL_JSON_TABLE_MAP.get(urlString);
			//get the fountain specific seed and set it to the assembler scope
			Integer dataLength = fountainTableRowSpecific.getInt("len");

			//automated droplet assemble
			String dropletDirID = fountainTableRowSpecific.getString("dropletLoc");
			String JSONDirPath = ENV.APP_STORAGE_LOC + ENV.DELIM + dropletDirID;
			byte[] decodedData_out = new byte[dataLength];
			try
			{
				int progress = AssembleFrameUtils.assembleDroplets_NonFrame(JSONDirPath, decodedData_out);
				table.getModel().setValueAt(progress, row, 3);
			}
			catch(Exception ex)
			{
				continue;
			}
		}
	}
}