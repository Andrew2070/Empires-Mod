package EmpiresMod.entities.Empire;


import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import EmpiresMod.API.Chat.IChatFormat;
import EmpiresMod.API.Chat.Component.ChatComponentFormatted;
import EmpiresMod.API.JSON.API.SerializerTemplate;
import EmpiresMod.API.container.PermissionsContainer;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.entities.Empire.Rank.Type;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class Relationship implements IChatFormat {

    /**
     * All the default ranks that are added to each empire on creation (except AdminEmpires)
     */
    public static final Container defaultRelations = new Container();
   
    public static void initDefaultRelationships() {

        Relationship AllyRelation = new Relationship("Ally", null, Type.ALLY);
        Relationship TruceRelation = new Relationship("Truce", null, Type.TRUCE);
        Relationship NeutralRelation = new Relationship("Neutral", null, Type.NEUTRAL);
        Relationship EnemyRelation = new Relationship("Enemy", null, Type.ENEMY);
        
       /*/ leaderRank.permissionsContainer.add("Empires.cmd*");
        leaderRank.permissionsContainer.add("Empires.bypass.*");

        officerRank.permissionsContainer.add("Empires.cmd*");
        officerRank.permissionsContainer.add("-Empires.cmd.leader");
        officerRank.permissionsContainer.add("Empires.bypass.plot");
        officerRank.permissionsContainer.add("Empires.bypass.flag.*");

        citizenRank.permissionsContainer.add("Empires.cmd.everyone.*");
        citizenRank.permissionsContainer.add("Empires.cmd.outsider.*");
        citizenRank.permissionsContainer.add("Empires.bypass.flag.*");
        citizenRank.permissionsContainer.add("Empires.bypass.flag.restrictions");
        /*/

        AllyRelation.permissionsContainer.add("Empires.bypass.*");
        
        Relationship.defaultRelations.clear();
        Relationship.defaultRelations.add(AllyRelation);
        Relationship.defaultRelations.add(TruceRelation);
        Relationship.defaultRelations.add(NeutralRelation);
        Relationship.defaultRelations.add(EnemyRelation);
    }

    private String name, newName = null;
    private Empire empire;
    private Type type;

    public final static PermissionsContainer permissionsContainer = new PermissionsContainer();

    public Relationship(String name, Empire empire, Type type) {
        this.name = name;
        this.empire = empire;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    
    public char getChatName() {
    	return name.charAt(0);
    }

    public void rename(String newName) {
        this.newName = newName;
    }

    public void resetNewName() {
        this.name = newName;
        this.newName = null;
    }

    public String getNewName() {
        return this.newName;
    }

    public Empire getEmpire() {
        return empire;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.color + getName();
    }

    @Override
    public IChatComponent toChatMessage() {
        return LocalizationManager.get("Empires.format.relationship", name).setChatStyle(new ChatStyle().setColor(type.color));
    }

    public enum Type implements IChatFormat {
        /**
         * LEADER RANK (ALL POWERS)
         */
        ALLY(EnumChatFormatting.BLUE, true),
        
        TRUCE(EnumChatFormatting.LIGHT_PURPLE, true),
        /**
         * DEFAULT RANK (NO SPECIAL POWERS, DEFAULT CITIZENS)
         */
        NEUTRAL(EnumChatFormatting.WHITE, true),

        /**
         * OFFICER RANK (SEMI-LEADER POWERS)
         */
        ENEMY(EnumChatFormatting.RED, true);
    
        @Override
        public IChatComponent toChatMessage() {
            IChatComponent name = new ChatComponentFormatted("{" + color.getFormattingCode() + "|%s}", name());
            return LocalizationManager.get("Empires.format.rank.type.short", name);
        }

        public final EnumChatFormatting color;
        public final boolean unique;

        Type(EnumChatFormatting color, boolean unique) {
            this.color = color;
            this.unique = unique;
        }
    }
    
    public static class Serializer extends SerializerTemplate<Relationship> {

        @Override
        public void register(GsonBuilder builder) {
            builder.registerTypeAdapter(Relationship.class, this);
        }

        @Override
        public Relationship deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            String name = jsonObject.get("name").getAsString();
            Relationship.Type relType = Type.valueOf(jsonObject.get("type").getAsString());
            Relationship rel= new Relationship(name, null, relType);
            if (jsonObject.has("permissions")) {
                Relationship.permissionsContainer.addAll(ImmutableList.copyOf(context.<String[]>deserialize(jsonObject.get("permissions"), String[].class)));
            }
            return rel;
        }

        @Override
        public JsonElement serialize(Relationship rel, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("name", rel.name);
            json.addProperty("type", rel.type.toString());
            json.add("permissions", context.serialize(rel.permissionsContainer, ArrayList.class));

            return json;
        }
    }

    
    public static class Container extends ArrayList<Relationship> implements IChatFormat {

        public boolean contains(String relationshipName) {
            for (Relationship relationship : this) {
                if (relationship.getName().equals(relationshipName))
                    return true;
            }
            return false;
        }

        public Relationship get(String relationshipName) {
            for (Relationship relationship : this) {
                if (relationship.getName().equals(relationshipName))
                    return relationship;
            }
            return null;
        }

        public Relationship get(Type type) {
            if(!type.unique) {
                throw new RuntimeException("The relationship you are trying to get is not unique!");
            }
           
            for(Relationship relationship : this) {
                if(relationship.getType() == type) {
                    return relationship;
                }
            }
            return null;
        }
        


        public Relationship getAllyRelationship() {
            for(Relationship relationship : this) {
                if(relationship.getType() == Type.ALLY) {
                    return relationship;
                }
            }
            return null;
        }

        public Relationship getTruceRelationship() {
            for(Relationship relationship : this) {
                if(relationship.getType() == Type.TRUCE) {
                    return relationship;
                }
            }
            return null;
        }
        
        public Relationship getNeutralRelationship() {
            for(Relationship relationship : this) {
                if(relationship.getType() == Type.NEUTRAL) {
                    return relationship;
                }
            }
            return null;
        }
        public Relationship getEnemyRelationship() {
            for(Relationship relationship : this) {
                if(relationship.getType() == Type.ENEMY) {
                    return relationship;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return toChatMessage().getUnformattedText();
        }

        @Override
        public IChatComponent toChatMessage() {
            IChatComponent root = new ChatComponentText("");

            for (Relationship relationship : this) {
                if (root.getSiblings().size() > 0) {
                    root.appendSibling(new ChatComponentFormatted("{7|, }"));
                }
                root.appendSibling(relationship.toChatMessage());
            }

            return root;
        }
    }

}