package EmpiresMod.entities.Relationships;

public class RelationType {
	//For Future Use:
	//More organized way of doing relationships.
	RelationType.Type Type;
	
	public RelationType(RelationType.Type Type) {
		
	}
	
	public enum Type {
		ALLY,
		TRUCE,
		NEUTRAL,
		ENEMY,
	}
	
	public RelationType.Type getRelationshipType() {
		return Type;
	}
	
	public RelationType.Type setRelationshipType(RelationType.Type type) {
		return this.Type = type;
	}
	
}
