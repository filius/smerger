package com.sm;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Copyright 2012 Valeriy Filatov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
public class DepTreeNode {
	
	public static interface IncIterator{
		public void onDep(DepTreeNode dep);
	}
	
	public static enum DepType{
		DEP,
		INC
	}

	private List<DepTreeNode> nodes = new ArrayList<DepTreeNode>();
	private DepType type;
	private String fileName;
	private List<String> postprocessors;
	
	DepTreeNode(DepType type, String file) throws Exception {
		this.type = type;
		this.fileName = file;
		if(type == DepType.DEP){
			nodes.addAll(getDeps(file+".xml"));
		}
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public List<String> getPostProcessors(){
		return postprocessors;
	}
	
	private List<DepTreeNode> getDeps(String modulePath) throws Exception {
		if(modulePath == null || modulePath.isEmpty())
			throw new Exception("Module path is Empty");
		
		File moduleFile = null;
		URL url = Merger.class.getResource(modulePath);
		if(url == null)
			moduleFile = new File(modulePath);
		else
			moduleFile = new File(url.toURI());
		
		Document doc = new SAXReader().read(moduleFile);
		Element root = doc.getRootElement();
		if(!"module".equals(root.getName())){
			throw new Exception("Root element not a [MODULE]");
		}
		
		Element ppsElement = root.element("postprocessors");
		if(ppsElement != null){
			postprocessors = new ArrayList<String>();
			@SuppressWarnings("unchecked")
			List<Element> els = ppsElement.elements("postprocessor");
			if(els != null){
				for(Element el : els){
					postprocessors.add(el.getStringValue());
				}
			}
		}
		
		List<DepTreeNode> deps = new ArrayList<DepTreeNode>();
		Element depsNode = root.element("deps");
		
		for(DepType type : DepType.values()){
			@SuppressWarnings("unchecked")
			List<Element> els = depsNode.elements(type.toString().toLowerCase());
			for(Element el : els){
				String path = el.getStringValue();
				if(!path.startsWith(File.separator) && !path.startsWith("classpath:")){
					path=moduleFile.getParent()+File.separator+path;
				}
				deps.add(new DepTreeNode(type, path));
			}
		}
		
		return deps;
	}
	
	public void iterate(IncIterator it){
		for(DepTreeNode node : nodes){
			if(node.type.equals(DepType.DEP))
				node.iterate(it);
			else
				it.onDep(node);
		}
	}
	
}
