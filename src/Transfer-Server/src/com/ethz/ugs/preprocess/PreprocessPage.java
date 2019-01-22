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

package com.ethz.ugs.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PreprocessPage 
{
	public static void main(String[] args) throws IOException {
		
		Set<String> imgLinkSet = new HashSet<>();
		Set<String> refLinkSet = new HashSet<>();
		
		Document document = Jsoup.parse(new File("C:\\Users\\Aritra\\Desktop\\pika.htm"), "UTF-8");
		
		Elements pngs = document.select("img[src]");
		
		int counter = 0;
		for(Element element : pngs)
		{
			imgLinkSet.add(element.attr("src"));
			if(element.attr("srcset").length() > 0)
				imgLinkSet.add(element.attr("srcset"));
		}
		Elements links = document.select("a[href]");
		
		for(Element element : links)
		{
			refLinkSet.add(element.attr("href"));
			
			
			if(element.attr("background-image").length()>0)
				System.out.println(element.attr("background-image"));
			
		}
		System.out.println("counter : " + counter);
		System.out.println(imgLinkSet.size());
		System.out.println(refLinkSet.size());
		
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\Desktop\\pika.htm"));
		FileWriter fw = new FileWriter("C:\\Users\\Aritra\\Desktop\\pikapika.htm");
		
		//StringBuffer stb = new StringBuffer();
		
		String str = null;
		while((str = br.readLine()) != null)
		{
			if(str.length() == 0)
				continue;
			for(String imgLink : imgLinkSet)
			{
				if(str.contains(imgLink))
				{
					str = str.replaceAll(imgLink, "bla" + imgLink);
					break;
				}
				if(str.contains("http://"))
				{
					str = str.replaceAll("http://", "blahttp://");
					break;
				}
			}
			
			fw.append(str + "\n");
			//stb.append(str);
		}
		br.close();
		fw.close();
	}
	
}
