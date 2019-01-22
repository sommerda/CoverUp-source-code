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
package com.ethz.tree;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;


/**
 * @author Aritra
 *
 */
public class Tree implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9022861670205152935L;
	public Map<String, Node> nodeMap;
	Node ROOT;
	public Tree() 
	{	
		this.ROOT = new Node("ROOT", 0L);
		this.nodeMap = new HashMap<>();
		this.nodeMap.put(ROOT.sliceName, ROOT);
	}
	
	public Tree(String jsonString)
	{
		JSONObject jObject = new JSONObject(jsonString);
		JSONArray jArray = jObject.getJSONArray("tree");
		
		this.ROOT = new Node("ROOT", 0L);
		this.nodeMap = new HashMap<>();
		this.nodeMap.put(ROOT.sliceName, ROOT);
		
		Map<String, JSONObject> tempMap = new HashMap<>();
		for(int i = 0; i < jArray.length(); i++)
		{
			JSONObject nodeObj = jArray.getJSONObject(i);
			//if(!nodeObj.getString("node").equals("ROOT"))
				tempMap.put(nodeObj.getString("node"), nodeObj);
			//Node node = new Node(nodeObj.getString("node"), nodeObj.getLong("id"));
		}
		for(String nodeName : tempMap.keySet())
		{
			JSONObject nodeObj = tempMap.get(nodeName);
			Node newNode = new Node(nodeObj.getString("node"), nodeObj.getLong("id"));
			this.nodeMap.put(newNode.sliceName, newNode);
		}
		for(String nodeName : tempMap.keySet())
		{
			JSONObject nodeObj = tempMap.get(nodeName);
			JSONArray childrenArray  = nodeObj.getJSONArray("children");
			
			Node node = this.nodeMap.get(nodeName);
			for(int i = 0; i< childrenArray.length(); i++)
			{
				Node childNode = this.nodeMap.get(childrenArray.get(i));
				if(node.children.isEmpty())
					node.children = new ArrayList<>();
				node.children.add(childNode);
			}
		}
	}
	
	
	public void insert(String parentName, String childName, long childId)
	{
		Node childNode = new Node(childName, childId);
		Node parentNode = this.nodeMap.get(parentName);
		if(parentNode.isLeaf())
			parentNode.children = new ArrayList<>();
		parentNode.children.add(childNode);
		this.nodeMap.put(childNode.sliceName, childNode);
	}
	
	public String treeToJSON()
	{
		JSONObject jObject = new JSONObject();
		
		JSONArray jArray = new JSONArray();
		for(String nodeStr : this.nodeMap.keySet())
		{
			JSONObject inner = new JSONObject();
			Node node = this.nodeMap.get(nodeStr);
			inner.put("node", node.sliceName);
			inner.put("id", node.sliceId);
			JSONArray childrenArray = new JSONArray();
			for(Node child : node.children)
				childrenArray.put(child.sliceName);
			inner.put("children", childrenArray);
			
			jArray.put(inner);
		}
		jObject.put("tree", jArray);
		return jObject.toString();
	}

	
	//test
	public static void main(String[] args) throws IOException {
		
		Tree tree = new Tree();
		tree.insert("ROOT", "bla", 1L);
		tree.insert("ROOT", "foo", 2L);
		tree.insert("ROOT", "abc", 3L);
		
		tree.insert("abc", "xyz", 4L);
		tree.insert("xyz", "pqr", 5L);
		
		
		FileOutputStream fileOut = new FileOutputStream("tree.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(tree);
		out.close();
		fileOut.close();

		System.out.println(tree.treeToJSON());
		//System.out.println("JSON length : " + tree.treeToJSON().getBytes().length);
		
		byte[] fileBytes = Files.readAllBytes(new File("tree.ser").toPath());
		System.out.println("Serialized length : " + fileBytes.length);
		
		
		Tree tree1 = new Tree(tree.treeToJSON());
		System.out.println(tree1.treeToJSON());
		System.out.println(tree.treeToJSON().equals(tree1.treeToJSON()));
		
		
		Tree testTree = new Tree();
		Random rand = new Random();
		for(int i = 0; i < 280; i++)
		{
			byte[] b = new byte[16];
			rand.nextBytes(b);
			testTree.insert("ROOT", Base64.getEncoder().encodeToString(b), rand.nextInt(10000));
		}
		System.out.println(testTree.treeToJSON().getBytes(StandardCharsets.UTF_8).length);
		System.out.println(compress(testTree.treeToJSON().getBytes(StandardCharsets.UTF_8), 6).length);
	}
	
	public static byte[] compress(byte[] inbyte, int preSet) throws IOException
	{
		InputStream inStream = new ByteArrayInputStream(inbyte);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		LZMA2Options options = new LZMA2Options();

		options.setPreset(preSet);
		
		XZOutputStream out = new XZOutputStream(outStream, options);
		
		byte[] buf = new byte[8192];
		int size;
		while ((size = inStream.read(buf)) != -1)
		   out.write(buf, 0, size);

		out.finish();
			
		inStream.close();
		out.close();
		
		return outStream.toByteArray();
	}
}
