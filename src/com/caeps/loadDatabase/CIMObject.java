package com.caeps.loadDatabase;

public class CIMObject {
	
	private String rdfId;
	private String name;
	
	public CIMObject(String rdfId, String name){
		this.rdfId=rdfId;
		this.name=name;
	}
	
	public String getRdfId(){
		return rdfId;
		
	}

	public String getName(){
		return name;
		
	}

}
