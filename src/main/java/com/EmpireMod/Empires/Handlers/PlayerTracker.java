package com.EmpireMod.Empires.Handlers;

import com.EmpireMod.Empires.Configuration.Config;
import com.EmpireMod.Empires.Utilities.PlayerUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;

public class PlayerTracker {

	public static final PlayerTracker instance = new PlayerTracker();

	@SuppressWarnings("UnnecessaryReturnStatement")
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent ev) {
		if (Config.instance.maintenanceMode.get() && ev.player instanceof EntityPlayerMP
				&& !(ev.player instanceof FakePlayer)) {
			if (!PlayerUtils.isOp(ev.player)) {
				((EntityPlayerMP) ev.player).playerNetServerHandler
						.kickPlayerFromServer(Config.instance.maintenanceModeMessage.get());
			}
			return;
		}
	}
}