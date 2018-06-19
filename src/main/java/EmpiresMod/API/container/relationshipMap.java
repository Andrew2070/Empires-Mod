package EmpiresMod.API.container;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import EmpiresMod.API.Chat.IChatFormat;
import EmpiresMod.API.Chat.Component.ChatComponentFormatted;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Relationship;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class relationshipMap extends HashMap<Empire, Relationship> implements IChatFormat {

    public void remove(Empire empire) {
        for (Empire empire2: super.keySet()) {
        	if (empire2 == empire) {
        		try {	
        		super.remove(empire);
            	} catch (ConcurrentModificationException e) {
            		e.printStackTrace();
            		super.remove(empire);
            	}
        	}
        }
    }

    public boolean contains(Relationship type) {
        for (Empire empire : keySet()) {
            if (super.get(empire).equals(type)) {
                return true;
            }
        }
        return false;
    }

    public Empire getreltype(Relationship.Type type) {
        for(Map.Entry<Empire, Relationship> entry : entrySet()) {
            if(entry.getValue().getType() == type) {
                return entry.getKey();
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
        

        for (Map.Entry<Empire, Relationship> entry : entrySet()) {
            if (root.getSiblings().size() > 0) {
                root.appendSibling(new ChatComponentFormatted("{7|, }"));
            }
            		
            root.appendSibling(LocalizationManager.get("Empires.format.citizen.withRank", entry.getKey(), entry.getValue()));
        }  //the thing that ranks citizens EXAMPLE: /empire info --> Player10101(LEADER)

        return root;
    }


}