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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import com.ethz.app.env.ENV;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Aritra
 *
 */
public class FeedViewer {

	public JFrame frame;

	/**
	 * Launch the application.
	 */
	public static Map<String, String> droplet_id_loc_map = new HashMap<>();
	public String selectedDir;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException 
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

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

	/**
	 * Create the application.
	 */
	public FeedViewer() throws IOException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	JTree tree;
	MutableTreeNode newNode;
	private void initialize() throws IOException 
	{
		frame = new JFrame();
		frame.setTitle("¯\\_(ツ)_/¯");
		frame.setBounds(100, 100, 911, 919);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.selectedDir = ENV.APP_STORAGE_LOC;
		
		newNode = getFountainInfo();

		tree = new JTree(newNode);

		tree.setBorder(new LineBorder(Color.gray, 1, true));
		JScrollPane scrollPane = new JScrollPane(tree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(frame.getWidth() / 2, frame.getHeight() - 10));
		frame.getContentPane().add(scrollPane, BorderLayout.WEST);	
		
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("choosertitle");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);

				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					selectedDir = chooser.getSelectedFile().toString();
					try {
						newNode = getFountainInfo();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
					//model.reload();
					removeNodesFromTree();
					try {
						addNodes();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				} else {
					JOptionPane.showMessageDialog(frame, "Nothing selected!", "Error", JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		scrollPane.setColumnHeaderView(btnBrowse);

		JEditorPane textArea = new JEditorPane();

		JScrollPane scrollPane1 = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
		scrollPane1.setViewportView(textArea);;

		frame.getContentPane().add(scrollPane1, BorderLayout.CENTER);


		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

				if(node != null)
				{
					if(!node.toString().equals("Available Fountains"))
					{
						File completedDataFile = new File(selectedDir + ENV.DELIM + droplet_id_loc_map.get(node.toString()) + ENV.DELIM + ENV.APP_STORAGE_COMPLETE_DATA);
						String data = null;
						try {
							data = new String(Files.readAllBytes(completedDataFile.toPath()));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						textArea.setText(data.trim());
					}
				}
			}
		});	
		
		
		ComponentResizer cr = new ComponentResizer();
		cr.setSnapSize(new Dimension(10, 10));
		cr.setDragInsets(new Insets(5, 5, 5, 5));
		cr.registerComponent(scrollPane, scrollPane1);
		
		
	}

	private MutableTreeNode getFountainInfo() throws IOException
	{
		MutableTreeNode newNode = new DefaultMutableTreeNode("Available Fountains");
		File dropletLoc = new File(this.selectedDir);
		int index = 0;
		for(File file : dropletLoc.listFiles())
		{
			int all = 0;
			if(file.isDirectory())
			{
				try
				{
					Long.parseLong(file.getName());
					File[] dropletContain = file.listFiles();
					for(File insideDropletDirFile : dropletContain)
					{
						String dropletUrl = null;
						if(insideDropletDirFile.getName().equals(ENV.APP_STORAGE_DROPLET_URL))
						{
							dropletUrl = Files.readAllLines(insideDropletDirFile.toPath()).get(0);
							++all;
							if(all == 2)
							{
								droplet_id_loc_map.put(dropletUrl, file.getName());
								newNode.insert(new DefaultMutableTreeNode(dropletUrl), index++);
							}
						}
						if(insideDropletDirFile.getName().equals(ENV.APP_STORAGE_COMPLETE_DATA))
						{
							++all;
							if(all == 2)
							{
								droplet_id_loc_map.put(dropletUrl, file.getName());
								newNode.insert(new DefaultMutableTreeNode(dropletUrl), index++);
							}
						}
					}
				}
				catch(NumberFormatException ex)
				{
					continue;
				}
			}
		}
		return newNode;
	}
	
	private void removeNodesFromTree()
	{
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		MutableTreeNode rootNode = (MutableTreeNode) model.getRoot();
		//System.out.println("Root node : " + rootNode);
		for(int i = rootNode.getChildCount() - 1; i>=0; i--)
		{
			System.out.println(rootNode.getChildAt(i));
			rootNode.remove(i);
		}
		model.reload();
	}
	
	private void addNodes() throws IOException
	{
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		MutableTreeNode newNode = (MutableTreeNode) model.getRoot();
		droplet_id_loc_map.clear();
		File dropletLoc = new File(this.selectedDir);
		int index = 0;
		for(File file : dropletLoc.listFiles())
		{
			int all = 0;
			if(file.isDirectory())
			{
				try
				{
					Long.parseLong(file.getName());
					File[] dropletContain = file.listFiles();
					for(File insideDropletDirFile : dropletContain)
					{
						String dropletUrl = null;
						if(insideDropletDirFile.getName().equals(ENV.APP_STORAGE_DROPLET_URL))
						{
							dropletUrl = Files.readAllLines(insideDropletDirFile.toPath()).get(0);
							++all;
							if(all == 2)
							{
								droplet_id_loc_map.put(dropletUrl, file.getName());
								newNode.insert(new DefaultMutableTreeNode(dropletUrl), index++);
							}
						}
						if(insideDropletDirFile.getName().equals(ENV.APP_STORAGE_COMPLETE_DATA))
						{
							++all;
							if(all == 2)
							{
								droplet_id_loc_map.put(dropletUrl, file.getName());
								newNode.insert(new DefaultMutableTreeNode(dropletUrl), index++);
							}
						}
					}
				}
				catch(NumberFormatException ex)
				{
					continue;
				}
			}
		}
		model.reload();
	}
}
