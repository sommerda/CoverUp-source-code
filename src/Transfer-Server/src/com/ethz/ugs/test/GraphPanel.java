package com.ethz.ugs.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;


public class GraphPanel extends JPanel {

	/**
	 * 
	 */

	public static final int h = 1440;
	public static final int w = 2560;

	private static final long serialVersionUID = 3374366456807894314L;
	//private int width = 800;
	//private int heigth = 400;
	private int padding = 80;
	private int labelPadding = 25;
	private Color lineColor = new Color(0, 102, 230, 180);
	private Color pointColor = new Color(0, 0, 0, 180);
	private Color gridColor = new Color(200, 200, 200, 200);
	private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
	private int pointWidth = 4;
	private int numberYDivisions = 20;
	private List<Double> scores1;

	public String lineType;
	public String fileName;
	public String desc;
	public int sampleSize;
	////
	public static long max = 0, min =0; 
	//bucket length in ns
	public static long bucketLen = 2000;
	///

	public GraphPanel(List<Double> scores1, String fileName, String desc, int sampleSize, String lineType) {
		this.scores1 = scores1;
		this.fileName = fileName;
		this.desc = desc;
		this.sampleSize = sampleSize;
		this.lineType = lineType;
	}

	public double[] xAxis;
	public void addCustomX(double[] xAxis)
	{
		this.xAxis = xAxis;
	}
	public void addCustomX(Long[] xAxis)
	{
		this.xAxis = new double[xAxis.length];
		for(int i = 0; i < xAxis.length; i++)
			this.xAxis[i] = xAxis[i];

	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		try {
			draw((Graphics2D) g, this.scores1, lineColor, pointColor);

		} catch (FileNotFoundException | DocumentException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void draw(Graphics2D g2,List<Double> scores, Color lineColor, Color pointColor) throws FileNotFoundException, DocumentException
	{      
		g2.setFont(new Font("Lucida Console", Font.BOLD, 25)); 

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores.size() - 1);
		double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

		List<Point> graphPoints = new ArrayList<>();
		for (int i = 0; i < scores.size(); i++) {
			int x1 = (int) (i * xScale + padding + labelPadding);
			int y1 = (int) ((getMaxScore() - scores.get(i)) * yScale + padding);
			graphPoints.add(new Point(x1, y1));
		}

		// draw white background
		g2.setColor(Color.WHITE);
		g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
		g2.setColor(Color.BLACK);


		// create hatch marks and grid lines for y axis.
		for (int i = 0; i < numberYDivisions + 1; i++) {
			int x0 = padding + labelPadding;
			int x1 = pointWidth + padding + labelPadding;
			int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
			int y1 = y0;
			if (scores.size() > 0) {
				g2.setColor(gridColor);
				g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
				g2.setColor(Color.BLACK);

				String yLabel = "";
				if(xAxis == null)
					//yLabel = ((double) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) )) / 100.0 + "a";
					yLabel =  String.format("%.3f", ((double) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) )) / 100.0);
				else
					yLabel = String.format("%.5f",(((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) )));

				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(yLabel);
				g2.drawString(yLabel, x0 - labelWidth - 10, y0 + (metrics.getHeight() / 2) - 3);

			}
			g2.drawLine(x0, y0, x1, y1);
		}

		// and for x axis
		int _x0 = 0, _y0 = 0;
		for (int i = 0; i < scores.size(); i++) 
		{
			if (scores.size() > 1)
			{
				int x0 = i * (getWidth() - padding * 2 - labelPadding) / (scores.size() - 1) + padding + labelPadding;
				int x1 = x0;
				int y0 = getHeight() - padding - labelPadding;
				int y1 = y0 - pointWidth;

				if ((i % ((int) ((scores.size() / 20.0)) + 1)) == 0) 
				{
					g2.setColor(gridColor);
					g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
					g2.setColor(Color.BLACK);

					String xLabel = ""; 

					if(xAxis == null)
						xLabel = String.format("%.3f", (float)(min + bucketLen * i) / 1000000);
					else
						xLabel = xAxis[i] + "";

					FontMetrics metrics = g2.getFontMetrics();
					int labelWidth = metrics.stringWidth(xLabel);
					g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 10);
				} 
				_x0 = x0;
				_y0 = y0;
				//g2.drawLine(x0, y0, x1, y1);
			}
		}
		g2.drawString(this.desc + ", sample size : " + sampleSize, _x0 - 700, _y0 - 1240);

		// create x and y axes 
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

		Stroke oldStroke = g2.getStroke();
		g2.setColor(lineColor);
		g2.setStroke(GRAPH_STROKE);

		for (int i = 0; i < graphPoints.size() - 1; i++) 
		{

			int x1 = graphPoints.get(i).x;
			int y1 = graphPoints.get(i).y;
			int x2 = graphPoints.get(i + 1).x;
			int y2 = graphPoints.get(i + 1).y;
			//int x3 = graphPoints.get(i + 2).x;
			//int y3 = graphPoints.get(i + 2).y;

			if(this.lineType.equalsIgnoreCase("line"))
			{
				g2.drawLine(x1, y1, x2, y2); // for line

				/*Curve fitting
				 * QuadCurve2D q = new QuadCurve2D.Float();
				q.setCurve(x1, y1, x2, y2, x3, y3);
				g2.draw(q);*/
			}

			if(this.lineType.equalsIgnoreCase("bar"))
				g2.drawLine(x1, y1, x1, _y0); //for bar

		}

		g2.setStroke(oldStroke);
		g2.setColor(pointColor);

		for (int i = 0; i < graphPoints.size(); i++) 
		{
			int x = graphPoints.get(i).x - pointWidth / 2;
			int y = graphPoints.get(i).y - pointWidth / 2;
			int ovalW = pointWidth;
			int ovalH = pointWidth;
			g2.fillOval(x, y, ovalW, ovalH);
		}

	}


	//    @Override
	//    public Dimension getPreferredSize() {
	//        return new Dimension(width, heigth);
	//    }
	private double getMinScore() {
		double minScore = Double.MAX_VALUE;
		for (double score : scores1) {
			minScore = Math.min(minScore, score);
		}
		return minScore;
	}

	private double getMaxScore() {
		double maxScore = Double.MIN_VALUE;
		for (double score : scores1) {
			maxScore = Math.max(maxScore, score);
		}
		return maxScore;
	}

	public void setScores(List<Double> scores) {
		this.scores1 = scores;
		invalidate();
		this.repaint();
	}

	public List<Double> getScores() {
		return scores1;
	}


	private static void createAndShowGui(boolean csv, String...Files) throws NumberFormatException, IOException, DocumentException 
	{	
		int counter = 0;
		for(String file : Files)
		{
			
			if(counter % 2 == 1)
			{
				counter++;
				continue;
			}
			
			System.out.println("File : " + file);
			List<Long> scores1 = new ArrayList<>();

			BufferedReader br = new BufferedReader(new FileReader(file));

			String st = null;
			//long k = 0;

			//TODO dummy to set the min
			//scores1.add(359674999L);

			if(!csv)
			{
				while((st = br.readLine()) != null)
				{
					//only consider sample size upto 50k
					//if(k == 50000)
					//	break;

					if(!st.startsWith("INFO"))
						continue;
					if(st.length() == 0)
						continue;	
					//if(st.contains("garbage"))
					//	continue;
					st = st.split(":")[2].trim().split(" ")[0].trim();
					//k++;
					long l = Long.parseLong(st);

					//if(l > 50000000L)
					//	continue;

					scores1.add(l);
				}
			}
			else
			{
				int k = 0;
				while((st = br.readLine()) != null)
				{
					//if(k >= 75000)
					//	break;
					if(st.length() == 0)
						continue;
					double d = Double.parseDouble(st);
					scores1.add((long)d);
					k++;
				}
			}
			br.close();

			
			System.out.println("Sample size : " + scores1.size());
			min = scores1.get(0);
			max = 0;
			for(long i : scores1)
			{
				if(min >= i)
					min = i;

				if(max < i)
					max = i;
			}

			System.out.println("Min : " + min);
			System.out.println("Max : " + max);

			int bucketCount = (int) (((max - min) % bucketLen == 0) ? ((max - min) / bucketLen) : ((max - min) / bucketLen) + 1);

			System.out.println(bucketCount);
			int[] bucket = new int[bucketCount + 1];
			for(long i : scores1)
			{
				long diff = i - min;
				int pos = (int) ((diff % bucketLen == 0) ? (diff / bucketLen) : (diff / bucketLen) + 1);
				bucket[pos]++;
			}

			List<Double> scores_n = new ArrayList<>();
			int score_max = 0;
			for(int i : bucket)
			{
				if(score_max < i)
					score_max = i;
				scores_n.add((double) i);		
			}
			/*List<Double> scores_prob = new ArrayList<>();
			double tot = 0d;
			for(int i : bucket)
				tot += i;

			for(int i : bucket)
				scores_prob.add((double)i/tot);
			 */
			/*List<Double> scores_cumulative = new ArrayList<>();
			double cumul = 0.0;
			for(double sc_pr : scores_prob)
			{
				cumul += sc_pr;
				scores_cumulative.add(cumul);
			}*/

			//percentage
			List<Double> scores_relative = new ArrayList<>();

			for(int i : bucket)
			{
				Double d =  (double) (i/(double)score_max) * 100;
				scores_relative.add(d);
			}
			//TODO change relative to actual here
			scores_n = scores_relative;

			//Collections.shuffle(scores1, new SecureRandom());
			//zoom in/out
			int sampleSize = 2000;
			/*List<Long> _scores1 = null;

		try
		{
			scores1 = scores1.subList(0, sampleSize);
		}
		catch(IndexOutOfBoundsException ex)
		{
			_scores1 = scores1;
		}*/
			// GraphPanel mainPanel = new GraphPanel(_scores1);

			List<Double> subScore = null;
			if(sampleSize != 0)
			{
				try
				{
					subScore = scores_n.subList(0, sampleSize);
				}
				catch(IndexOutOfBoundsException ex)
				{
					subScore = scores_n;
				}
			}
			else
				subScore = scores_n;
			
			GraphPanel mainPanel = new GraphPanel(subScore, new File(file).getName(), Files[counter + 1], scores1.size(), "bar");
			mainPanel.setPreferredSize(new Dimension(w, h));
			JFrame frame = new JFrame("DrawGraph : " + new File(file).getName());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			frame.getContentPane().add(mainPanel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			/*BufferedImage bi = new BufferedImage(frame.getSize().width, frame.getSize().height, BufferedImage.TYPE_INT_ARGB); 
        	Graphics g = bi.createGraphics();
        	frame.paint(g);  //this == JComponent
        	g.dispose();
        	try{ImageIO.write(bi,"png",new File("test.png"));}catch (Exception e) {}*/


			Document document = new Document(new Rectangle(frame.getSize().width, frame.getSize().height));
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("Traces\\pdfs\\" + new File(file).getName() + ".pdf"));   
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			Graphics2D g2 = cb.createGraphics(frame.getSize().width, frame.getSize().height);
			frame.paint(g2);
			g2.dispose();
			document.close();

			counter++;
		}
	}


	//JOptionPane.showMessageDialog(frame, "Graph saved in png and pdf format!");


	public static void main(String[] args) throws IOException, InterruptedException {

		File loc = new File("Traces\\pdfs");
		for(File file : loc.listFiles())
		{
			file.delete();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGui(true, new String[]{
							/*
							"Traces\\MainServer.log.8",
							"Traces\\MainServer.log.9",
							"Traces\\MainServer.log.10",
							"Traces\\MainServer.log.11",
							"Traces\\MainServer.log.14",
							"Traces\\MainServer.log.15",
							"Traces\\MainServer.log.16",
							 */
							//"Traces\\MainServer.log.18", "Broadcast droplets",
							//"Traces\\MainServer.log.17", "Interactive droplets"
							
							//"Traces\\bigTrace\\noInt.log.10", "Broadcast droplets",
							//"Traces\\bigTrace\\int.log.10", "Interactive droplets"
							
							"JS_time_test\\WoEx\\timeout\\data_timeout_50000_200_1481971014064_diff.csv_noise.csv" , "External noise",
							"JS_time_test\\WoEx\\timeout\\data_timeout_noise_50000_200_1482036486086.csv_diff.csv" , "Internal noise"
							
							/*"C:\\Users\\Aritra\\workspace_Mars_new\\deniableComChannel\\"
							+ "Measurements\\Data\\JS_new\\all intercept_read\\data_100000_200_noInt_1476149977024.csv", "All intercept read",
							"C:\\Users\\Aritra\\workspace_Mars_new\\deniableComChannel\\"
									+ "Measurements\\Data\\JS_new\\no extension\\data_75000_200_noInt_1476039727271.csv", "No ext"*/
							
							/*"C:\\Users\\Aritra\\workspace_Mars_new\\deniableComChannel\\"
									+ "Measurements\\Data\\JS_new\\nw noise\\all_read\\data_10000_200_noInt_1476205905277.csv", "All intercept read",
							"C:\\Users\\Aritra\\workspace_Mars_new\\deniableComChannel\\"
									+ "Measurements\\Data\\JS_new\\nw noise\\no_int\\data_5000_200_noInt_1476271838263.csv", "No ext"*/
							
							/*"C:\\Users\\Aritra\\workspace_Mars_new\\DeniableCommChannel\\"
							+ "Measurements\\Data\\JS_new\\nw noise\\Large Data Set\\int\\m1.csv", "All intercept read",
							
							"C:\\Users\\Aritra\\workspace_Mars_new\\DeniableCommChannel\\"
							+ "Measurements\\Data\\JS_new\\nw noise\\Large Data Set\\no_int\\m1.csv", "No ext"
							*/
							//"Traces\\ChromeTrace\\withExt.csv", "with extension",
							//"Traces\\ChromeTrace\\woExt.csv", "without extension"
							
							//"server_work_space\\MainServer.log", "bla"

					});

					List<InputStream> pdfs = new ArrayList<InputStream>();

					for(File file : loc.listFiles())
						pdfs.add(new FileInputStream(file));

					new File("Traces\\combinedOutput.pdf").delete();
					OutputStream output = new FileOutputStream("Traces\\combinedOutput.pdf");
					concatPDFs(pdfs, output, true);

				} catch (NumberFormatException | IOException e) 
				{	e.printStackTrace();
				} catch (DocumentException e) {
					e.printStackTrace();
				}
			}
		});

		//Thread.sleep(1000);


	}

	//pdf utils

	public static void concatPDFs(List<InputStream> streamOfPDFFiles,
			OutputStream outputStream, boolean paginate) {

		Document document = new Document(new Rectangle(w, h));
		try {
			List<InputStream> pdfs = streamOfPDFFiles;
			List<PdfReader> readers = new ArrayList<PdfReader>();
			int totalPages = 0;
			Iterator<InputStream> iteratorPDFs = pdfs.iterator();

			// Create Readers for the pdfs.
			while (iteratorPDFs.hasNext()) {
				InputStream pdf = iteratorPDFs.next();
				PdfReader pdfReader = new PdfReader(pdf);
				readers.add(pdfReader);
				totalPages += pdfReader.getNumberOfPages();
			}
			// Create a writer for the outputstream
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);

			document.open();
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
					BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
			// data

			PdfImportedPage page;
			int currentPageNumber = 0;
			int pageOfCurrentReaderPDF = 0;
			Iterator<PdfReader> iteratorPDFReader = readers.iterator();

			// Loop through the PDF files and add to the output.
			while (iteratorPDFReader.hasNext()) {
				PdfReader pdfReader = iteratorPDFReader.next();

				// Create a new page in the target for each source page.
				while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
					document.newPage();
					pageOfCurrentReaderPDF++;
					currentPageNumber++;
					page = writer.getImportedPage(pdfReader,
							pageOfCurrentReaderPDF);
					cb.addTemplate(page, 0, 0);

					// Code for pagination.
					if (paginate) {
						cb.beginText();
						cb.setFontAndSize(bf, 9);
						cb.showTextAligned(PdfContentByte.ALIGN_CENTER, ""
								+ currentPageNumber + " of " + totalPages, 520,
								5, 0);
						cb.endText();
					}
				}
				pageOfCurrentReaderPDF = 0;
			}
			outputStream.flush();
			document.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (document.isOpen())
				document.close();
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}
