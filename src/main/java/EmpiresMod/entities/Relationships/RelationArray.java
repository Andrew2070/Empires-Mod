package EmpiresMod.entities.Relationships;

import java.util.ArrayList;
import java.util.List;

import EmpiresMod.entities.Empire.Empire;

public class RelationArray  {
	
	private List<Relations> RelationArray = new ArrayList<Relations>();

	public void setRelations(Relations relation) {
		this.RelationArray.add(relation);
	}
	//Returns specific empire names by the relation specified
	public Empire getAllEmpiresByType(Empire empire1, RelationType.Type type, Boolean mutuality) {
		for (int i=0; i < this.RelationArray.size(); i++) {
			Relations selectedRelationship = RelationArray.get(i);
			if (selectedRelationship.getRelationshipType() == type) {
			return selectedRelationship.getEmpire2();
			}
		}
		return null;
	}
	//Create A New Relationship within the Array.
	public void setRelationship(Relations relation) {
		RelationArray.add(relation);
	}
	
	public RelationType.Type getSpecificRelationByEmpire(Empire foreignEmpire, RelationType.Type type, Boolean mutuality) {
		for (int i=0; i < this.RelationArray.size(); i++) {
			Relations selectedRelationship = RelationArray.get(i);
			if (selectedRelationship.getEmpire2() == foreignEmpire) {
				return selectedRelationship.getRelationshipType();
			}
		}
		return null;
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
	public Boolean mutualityChecks(Relations relation1, Relations relation2) {
		for (int i=0; i < this.RelationArray.size(); i++) {
			Relations selectedRelationship = RelationArray.get(i);
			if (relation1.getEmpire2() == relation2.getEmpire1()) {
				return true;
			}
		}
	  return false;
	}
}
