package com.EmpireMod.Empires.Utilities;

import java.text.DateFormat;
import java.util.Date;

import com.EmpireMod.Empires.API.Chat.Component.ChatManager;
import com.EmpireMod.Empires.API.JSON.JsonMessageBuilder;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;
import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Empire.EmpireBlock;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Formatter {
	private static final DateFormat dateFormatter = DateFormat.getDateTimeInstance(0, 0);

	private Formatter() {
	}

	public static String formatDate(Date date) {
		return dateFormatter.format(date);
	}

	public static String formatBlockInfo(EmpireBlock block) {
		return String.format(" ---------- Claimed Chunk----------\nEmpire: %1$s\nDimension: %2$s\nLocation: %3$s",
				block.getEmpire().getName(), block.getDim(), block.getCoordString());
	}

	public static void sendMap(Citizen res, int dim, int cx, int cz) {
		int heightRad = 4, widthRad = 9; // causes lag if increased (although i
											// prefer 14 to 16 instead of 9, the
											// FPS drop is too high)

		ChatManager.send(res.getPlayer(), new ChatComponentText("---------- [Empire Map] ----------"));
		for (int z = cz - heightRad; z <= cz + heightRad; z++) {
			JsonMessageBuilder msgBuilder = new JsonMessageBuilder();

			for (int x = cx - widthRad; x <= cx + widthRad; x++) {
				EmpireBlock b = EmpiresUniverse.instance.blocks.get(dim, x, z);
				JsonMessageBuilder extraBuilder = msgBuilder.addExtra();

				boolean mid = z == cz && x == cx;
				boolean isEmpire = b != null && b.getEmpire() != null;
				boolean ownEmpire = isEmpire && res.empiresContainer.contains(b.getEmpire());

				if (mid) {
					extraBuilder.setColor(ownEmpire ? EnumChatFormatting.GREEN
							: isEmpire ? EnumChatFormatting.RED : EnumChatFormatting.WHITE);
				} else {
					extraBuilder.setColor(ownEmpire ? EnumChatFormatting.DARK_GREEN
							: isEmpire ? EnumChatFormatting.DARK_RED : EnumChatFormatting.GRAY);
				}

				extraBuilder.setText(isEmpire ? "X" : "_");

				if (b != null) {
					extraBuilder.setHoverEventShowText(formatBlockInfo(b));
					extraBuilder.setClickEventRunCommand("/emp info " + b.getEmpire().getName());
				}
			}

			res.getPlayer().addChatMessage(msgBuilder.build());
		}
	}

	public static void sendMap(Citizen res) {
		if (res.getPlayer() == null)
			return;
		sendMap(res, res.getPlayer().dimension, res.getPlayer().chunkCoordX, res.getPlayer().chunkCoordZ);
	}
}