package EmpiresMod.entities.Tools;

import EmpiresMod.Empires;
import EmpiresMod.API.Chat.Component.ChatManager;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.Utilities.EmpireUtils;
import EmpiresMod.entities.Empire.BlockWhitelist;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Plot;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.entities.Managers.ToolManager;
import EmpiresMod.entities.Misc.Tool;
import EmpiresMod.entities.Position.BlockPosition;

/**
 * A tool that selects a block to add it to whitelists for protection.
 */
public class WhitelisterTool extends Tool {

	private Citizen owner;
	private FlagType flagType = FlagType.ACCESS;

	public WhitelisterTool(Citizen owner) {
		super(owner.getPlayer(),
				LocalizationManager.get("Empires.tool.name", LocalizationManager.get("Empires.tool.whitelister.name"))
						.getLegacyFormattedText()[0]);
		this.owner = owner;
	}

	@Override
	public void onItemUse(BlockPosition bp, int face) {
		Empire empire = EmpireUtils.getEmpireAtPosition(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);

		if (!hasPermission(empire, bp)) {
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
		return LocalizationManager.get("Empires.tool.whitelister.description",
				flagType == null ? LocalizationManager.get("Empires.tool.whitelister.removal").getUnformattedText()
						: flagType.toString())
				.getLegacyFormattedText();
	}

	@Override
	public void onShiftRightClick() {
		if (flagType == FlagType.getWhitelistable().get(FlagType.getWhitelistable().size() - 1)) {
			flagType = null;
			updateDescription();
			ChatManager.send(owner.getPlayer(), "Empires.tool.mode",
					LocalizationManager.get("Empires.tool.whitelister.property"),
					LocalizationManager.get("Empires.tool.whitelister.removal").getUnformattedText());
		} else {
			if (flagType == null) {
				flagType = FlagType.getWhitelistable().get(0);
			} else {
				flagType = FlagType.getWhitelistable().get(FlagType.getWhitelistable().indexOf(flagType) + 1);
			}
			updateDescription();
			ChatManager.send(owner.getPlayer(), "Empires.tool.mode",
					LocalizationManager.get("Empires.tool.whitelister.property"), flagType.name);
		}
	}

	protected boolean hasPermission(Empire empire, BlockPosition bp) {
		if (empire == null) {
			ChatManager.send(owner.getPlayer(), "Empires.cmd.err.notInEmpire", owner.empiresContainer.getMainEmpire());
			return false;
		}

		// TODO: Switch to using proper permission strings
		if (!(empire.citizensMap.get(owner).getName().equals("Officer")
				|| empire.citizensMap.get(owner).getName().equals("Leader"))) {
			Plot plot = empire.plotsContainer.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
			if (plot == null || !plot.ownersContainer.contains(owner)) {
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