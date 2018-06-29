package EmpiresMod.entities.Relationships;

import EmpiresMod.entities.Empire.Empire;

public class Relations {
	//For Future Use:
	//More organized way of doing relationships.

	private Empire empire1;
	private Empire empire2;
	private RelationType.Type type;
	private Boolean mutuality = false;

	public Relations(Empire empire1, Empire empire2, RelationType.Type type, Boolean mutuality) {
		setEmpire1(empire1);
		setEmpire2(empire2);
		setRelationshipType(type);	
		setMutuality(mutuality);
	}

	public Relations setEmpire1(Empire empire1){
		this.empire1 = empire1;
		return this;
	}
	
	public Relations setEmpire2(Empire empire2) {
		this.empire2 = empire2;
		return this;
	}
	 
	public Relations setRelationshipType(RelationType.Type type) {
		this.type = type;
		return this;
	}
	
	public Relations setMutuality(Boolean value) {
		this.mutuality = value;
		return this;
	}
	
	public Empire getEmpire1() {
		return this.empire1;
	}
	
	public Empire getEmpire2() {
		return this.empire2;
	}
	
	public RelationType.Type getRelationshipType() {
		return this.type;
	}
	
}
