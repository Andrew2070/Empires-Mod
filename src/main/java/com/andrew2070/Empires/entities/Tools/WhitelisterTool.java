package com.andrew2070.Empires.entities.Tools;

import com.andrew2070.Empires.API.commands.ChatManager;
import com.andrew2070.Empires.entities.Position.BlockPos;
import com.andrew2070.Empires.entities.Misc.Tool;
import com.andrew2070.Empires.entities.Managers.ToolManager;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.Empires;
import com.andrew2070.Empires.entities.Empire.BlockWhitelist;
import com.andrew2070.Empires.entities.Empire.Citizen;
import com.andrew2070.Empires.entities.Empire.Plot;
import com.andrew2070.Empires.entities.Empire.Empire;
import com.andrew2070.Empires.entities.Flags.FlagType;
import com.andrew2070.Empires.utils.EmpireUtils;


/**
 * A tool that selects a block to add it to whitelists for protection.
 */
public class WhitelisterTool extends Tool {

    private Citizen owner;
    private FlagType flagType = FlagType.ACCESS;

    public WhitelisterTool(Citizen owner) {
        super(owner.getPlayer(), LocalManager.get("Empires.tool.name", LocalManager.get("Empires.tool.whitelister.name")).getLegacyFormattedText()[0]);
        this.owner = owner;
    }

    @Override
    public void onItemUse(BlockPos bp, int face) {
        Empire empire = EmpireUtils.getEmpireAtPosition(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);

        if(!hasPermission(empire, bp)) {
            return;
        }

        if (flagType == null) {
            removeWhitelists(empire, bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
        } else {
            addWhitelists(flagType, empire, bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
        }
        ToolManager.instance.remove(this);
    }

    @Override
    protected String[] getDescription() {
        return LocalManager.get("Empires.tool.whitelister.description", flagType == null ? LocalManager.get("Empires.tool.whitelister.removal").getUnformattedText() : flagType.toString()).getLegacyFormattedText();
    }

    @Override
    public void onShiftRightClick() {
        if(flagType == FlagType.getWhitelistable().get(FlagType.getWhitelistable().size() - 1)) {
            flagType = null;
            updateDescription();
            ChatManager.send(owner.getPlayer(), "Empires.tool.mode",
                    LocalManager.get("Empires.tool.whitelister.property"),
                    LocalManager.get("Empires.tool.whitelister.removal").getUnformattedText());
        } else {
            if(flagType == null) {
                flagType = FlagType.getWhitelistable().get(0);
            } else {
                flagType = FlagType.getWhitelistable().get(FlagType.getWhitelistable().indexOf(flagType) + 1);
            }
            updateDescription();
            ChatManager.send(owner.getPlayer(), "Empires.tool.mode",
                    LocalManager.get("Empires.tool.whitelister.property"),
                    flagType.name);
        }
    }

    protected boolean hasPermission(Empire empire, BlockPos bp) {
        if(empire == null) {
            ChatManager.send(owner.getPlayer(), "Empires.cmd.err.notInEmpire", owner.empiresContainer.getMainEmpire());
            return false;
        }

        //TODO: Switch to using proper permission strings
        if(!(empire.citizensMap.get(owner).getName().equals("Officer") || empire.citizensMap.get(owner).getName().equals("Leader"))) {
            Plot plot = empire.plotsContainer.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
            if(plot == null || !plot.ownersContainer.contains(owner)) {
                ChatManager.send(owner.getPlayer(), "Empires.cmd.err.perm.whitelist.noPermssion");
                return false;
            }
        }

        return true;
    }

    private void removeWhitelists(Empire empire, int dim, int x, int y, int z) {
        for (FlagType flagType : FlagType.getWhitelistable()) {
            BlockWhitelist bw = empire.blockWhitelistsContainer.get(dim, x, y, z, flagType);
            if (bw != null) {
                Empires.instance.datasource.deleteBlockWhitelist(bw, empire);
                ChatManager.send(owner.getPlayer(), "Empires.notification.perm.empire.whitelist.removed");
            }
        }
    }

    private void addWhitelists(FlagType flagType, Empire empire, int dim, int x, int y, int z) {
        BlockWhitelist bw = empire.blockWhitelistsContainer.get(dim, x, y, z, flagType);
        if (bw == null) {
            bw = new BlockWhitelist(dim, x, y, z, flagType);
            ChatManager.send(owner.getPlayer(), "Empires.notification.perm.empire.whitelist.added");
            Empires.instance.datasource.saveBlockWhitelist(bw, empire);
        } else {
            ChatManager.send(owner.getPlayer(), "Empires.notification.perm.empire.whitelist.already");
        }
    }
}