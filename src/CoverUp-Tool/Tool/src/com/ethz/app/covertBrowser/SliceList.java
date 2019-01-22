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

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;

/**
 * @author Aritra
 *
 */
public class SliceList extends Shell {


	private Set<String> displeaySet;

	/**
	 * Create the shell.
	 * @param display
	 */
	public SliceList(Display display, Set<String> displaySet) {
		super(display, SWT.SHELL_TRIM);
		this.displeaySet = displaySet;
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("Slice link list");
		setSize(319, 452);

		List list = new List(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		list.setBounds(0, 20, 298, 347);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		for (String sliceName : displeaySet) {
			list.add(sliceName);
		}

		Button btnDelete = new Button(this, SWT.NONE);
		if(displeaySet.size() == 0)
			btnDelete.setEnabled(false);

		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {   

				if(displeaySet.size() > 0)
				{
					try
					{
						String outString = list.getItem(list.getSelectionIndex());
						displeaySet.remove(outString);
					}
					catch(Exception ex)
					{
						System.err.println("bla");
					}
				}
				else
					setEnabled(false);

				list.removeAll();
				for (String sliceName : displeaySet) {
					list.add(sliceName);
				}
			}
		});
		btnDelete.setBounds(101, 373, 90, 30);
		btnDelete.setText("Delete");
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
