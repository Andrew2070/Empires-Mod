package com.andrew2070.Empires.API.container;


import com.andrew2070.Empires.API.commands.ChatComponentFormatted;
import com.andrew2070.Empires.API.commands.IChatFormat;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.entities.Empire.Rank;
import com.andrew2070.Empires.entities.Empire.Citizen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    public String toString() {
        return toChatMessage().getUnformattedText();
    }

    @Override
    public IChatComponent toChatMessage() {
        IChatComponent root = new ChatComponentText("");

        for (Map.Entry<Citizen, Rank> entry : entrySet()) {
            if (root.getSiblings().size() > 0) {
                root.appendSibling(new ChatComponentFormatted("{7|, }"));
            }
            root.appendSibling(LocalManager.get("Empires.format.citizen.withRank", entry.getKey(), entry.getValue()));
        }

        return root;
    }
}