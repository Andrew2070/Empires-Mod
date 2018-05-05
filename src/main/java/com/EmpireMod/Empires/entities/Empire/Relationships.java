package com.EmpireMod.Empires.entities.Empire;

public class Relationships {

	private Empire empire;
	private Relationship relation;
	
	public Relationships(Empire data1, Relationship data2){

		empire = data1; //Name of Other Empire
		relation = data2; //Type of Relation
		
	}
	
}

enum Relationship {
	
	ENEMY,
	
	PEACE,
	
	ALLIANCE,
	
	NEUTRAL;
	
	
}