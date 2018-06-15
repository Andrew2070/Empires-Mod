package EmpiresMod.API.container;


import java.util.HashMap;
import java.util.Map;

import EmpiresMod.API.Chat.IChatFormat;
import EmpiresMod.API.Chat.Component.ChatComponentFormatted;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Rank;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class CitizenRankMap extends HashMap<Citizen, Rank> implements IChatFormat {

    public void remove(Citizen res) {
        /*
        for (Iterator<Plot> it = res.getCurrentEmpire().plotsContainer.asList().iterator(); it.hasNext(); ) {
            Plot plot = it.next();
            if (plot.ownersContainer.contains(res) && plot.ownersContainer.size() <= 1) {
                it.remove();
            }
        }
        */
        super.remove(res);
    }

    public boolean contains(String username) {
        for (Citizen res : keySet()) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public Citizen getLeader() {
        for(Map.Entry<Citizen, Rank> entry : entrySet()) {
            if(entry.getValue().getType() == Rank.Type.LEADER) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String abbrieviate(Rank rank) {
    	String rankab = rank.getName().substring(0,1);
    	String abbriev = "[" +rankab+ "]";
    	
    	if (rank.getType() == Rank.Type.LEADER) {
    		String abbrievL = EnumChatFormatting.RED +"[" +rankab+ "]";
    		return abbrievL;
    	}
    	if (rank.getType() == Rank.Type.OFFICER) {
    		String abbrievO = EnumChatFormatting.BLUE +"[" +rankab+ "]";
    		return abbrievO;
    	}
    	if (rank.getType() == Rank.Type.DEFAULT) {
    		String abbrievD = EnumChatFormatting.DARK_GREEN +"[" +rankab+ "]";
    		return abbrievD;
    	}
    	return abbriev;
    }
    

    @Override
    public String toString() {
        return toChatMessage().getUnformattedText();
    }
    

    @Override
    public IChatComponent toChatMessage() {
        IChatComponent root = new ChatComponentText("");

        for (Map.Entry<Citizen, Rank> entry : entrySet()) {
            String abbrievRank = "";
            if (root.getSiblings().size() > 0) {
                root.appendSibling(new ChatComponentFormatted("{7|,}"));
            }
            if (entry.getValue().getType() == Rank.Type.LEADER) {
            	abbrievRank = "[L]";
            }
            
            if (entry.getValue().getType() == Rank.Type.OFFICER) {
            	abbrievRank = "[O]";
            }
            
            if (entry.getValue().getType() == Rank.Type.DEFAULT) {
            	abbrievRank = "[C]";
            }
            		
            root.appendSibling(LocalizationManager.get("Empires.format.citizen.withRank",  abbrieviate(entry.getValue()), entry.getKey()));
        }  //the thing that ranks citizens EXAMPLE: /empire info --> Player10101(LEADER)

        return root;
    }
}