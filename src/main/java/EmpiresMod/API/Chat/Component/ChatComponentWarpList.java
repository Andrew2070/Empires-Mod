package EmpiresMod.API.Chat.Component;

import EmpiresMod.Configuration.Config;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.Misc.Teleport.Teleport;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Relationship;
import net.minecraft.util.IChatComponent;

public class ChatComponentWarpList extends ChatComponentMultiPage {

	private Empire empire;
	public ChatComponentWarpList(Empire empire) {
		super(Config.instance.maxWarps.get());
		this.empire = empire;
		this.construct();
	}

	private void construct() {
		for (Teleport warp : empire.Warps) {
			if (warp.getEmpirename().equals(empire.getName())) {
			IChatComponent warpname = new ChatComponentFormatted("");
			warpname.appendSibling(new ChatComponentFormatted("{7| }"));
			warpname.appendSibling(new ChatComponentFormatted("{7| " + warp.getName().toString() +" }"));
			this.add(new ChatComponentFormatted("{7| --> }{%s}", warpname));
			}
		}
	}

	@Override
	public ChatComponentContainer getHeader(int page) {
		ChatComponentContainer header = super.getHeader(page);

		header.add(new ChatComponentFormatted("{9|     [CURRENT LIST OF ALL AVAILABLE EMPIRE WARP POINTS]"));

		return header;
	}
}