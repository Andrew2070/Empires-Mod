package EmpiresMod.entities.Relationships;

import java.util.ArrayList;
import java.util.List;

import EmpiresMod.entities.Empire.Empire;

public class RelationArray  {
	
	private List<Relations> RelationArray = new ArrayList<Relations>();

	public void setRelations(Relations relation) {
		this.RelationArray.add(relation);
	}
	
	public Empire getAllEmpires(Empire empire1, Empire empire2, RelationType.Type type, Boolean mutuality) {
		Relations relation = new Relations(empire1, empire2, type, mutuality);
		for (int i=0; i < this.RelationArray.size(); i++) {
			Relations selectedRelationship = RelationArray.get(i);
			if (relation == selectedRelationship) {
			return selectedRelationship.getEmpire2();
			}
		}
		return null;
	}
	//Returns specific empire names by the relation specified
	public Empire getSpecificRelationEmpires(Empire empire1, Empire empire2, RelationType.Type type, Boolean mutuality) {
		Relations relation = new Relations(empire1, empire2, type, mutuality);
		for (int i=0; i < this.RelationArray.size(); i++) {
			Relations selectedRelationship = RelationArray.get(i);
		if (relation == selectedRelationship) {
			if (selectedRelationship.getRelationshipType() == type) {
				return selectedRelationship.getEmpire2();
			}
		}
			
	}
		return null;
	}
	
	//Create A New Relationship within the Array.
	public void setRelationship(Relations relation) {
		RelationArray.add(relation);
	}

	//Must Remove Previous Relationship Status, Before Changing To New One, Avoids Duplicate statuses.
	public void removeRelationship(Empire empire2) {
		for (int i=0; i < this.RelationArray.size(); i++) {
			Relations selectedRelationship = RelationArray.get(i);
			if (selectedRelationship.getEmpire2() == empire2) {
				RelationArray.remove(selectedRelationship);
			}
		}
	}
	//Cross Check IF Two Empires Have Mutual Statuses Set For Each Other.
	public void mutualityChecks(Relations relation1, Relations relation2) {
		RelationType.Type relation1Type;
		RelationType.Type relation2Type;
		for (int i=0; i < this.RelationArray.size(); i++) {
			Relations selectedRelationship = RelationArray.get(i);

			
			//Relation1     Relation 2
			//Empire 2      Empire1
			
			if (selectedRelationship.getEmpire2() == relation1.getEmpire2()) {
				relation1Type = selectedRelationship.getRelationshipType();
			}
			
			if (selectedRelationship.getEmpire2() == relation2.getEmpire2()); 
			
			
	}
	}
}
