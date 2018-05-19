package EmpiresMod.API.JSON.Configuration;


import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;

import EmpiresMod.Empires;
import EmpiresMod.API.container.relationshipMap;
import EmpiresMod.entities.Empire.Relationship;

public class RelationshipsConfig extends JsonConfig<Relationship, Relationship.Container> {

    public RelationshipsConfig(String path) {
        super(path, "DefaultEmpireRelationships");
        this.gsonType = Relationship.Container.class; 
        this.gson = new GsonBuilder().registerTypeAdapter(Relationship.class, new Relationship.Serializer()).setPrettyPrinting().create();
    }

    @Override
    protected Relationship.Container newList() {
        return new Relationship.Container();
    }

    @Override
    public void create(Relationship.Container items) {
        Relationship.initDefaultRelationships();
        items.addAll(Relationship.defaultRelations);
        super.create(items);
    }

    @Override
    public Relationship.Container read() {
        Relationship.Container rel = super.read();

        Relationship.defaultRelations.clear();
        Relationship.defaultRelations.addAll(rel);

        return rel;
    }

    @Override
    public boolean validate(Relationship.Container items) {
        boolean isValid = true;
        for(Relationship.Type type : Relationship.Type.values()) {
            if(type.unique) {
                List<Relationship> relOfType = new ArrayList<Relationship>();
                for(Relationship rel : items) {
                    if(rel.getType() == type) {
                        relOfType.add(rel);
                    }
                }

                if(relOfType.size() == 0) {
                    isValid = false;
                    Empires.instance.LOG.error("Unique type of Relation was not found in " + name);
                    items.add(Relationship.defaultRelations.get(type));
                } else if(relOfType.size() > 1) {
                    isValid = false;
                    Empires.instance.LOG.error("Unique type of Relation was found multiple times in " + name + ". Setting all aside from the first to type regular.");
                    for(int i = 1; i < relOfType.size(); i++) {
                        relOfType.get(i).setType(Relationship.Type.ALLY);
                        relOfType.get(i).setType(Relationship.Type.ENEMY);
                        relOfType.get(i).setType(Relationship.Type.TRUCE);
                        relOfType.get(i).setType(Relationship.Type.NEUTRAL);
                        
                        
                        
                    }
                }
            }
        }

        return isValid;
    }
}