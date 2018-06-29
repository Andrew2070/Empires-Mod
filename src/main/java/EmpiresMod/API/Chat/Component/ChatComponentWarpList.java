package EmpiresMod.API.Chat.Component;

import java.util.ArrayList;
import java.util.List;

import EmpiresMod.Configuration.Config;
import EmpiresMod.Localization.LocalizationManager;
import EmpiresMod.Misc.Teleport.Teleport;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.Relationship;
import net.minecraft.util.IChatComponent;

public class ChatComponentWarpList extends ChatComponentMultiPage {
	private List<String> printedWarps = new ArrayList<String>();
	private Empire empire;
	public ChatComponentWarpList(Empire empire) {
		super(Config.instance.maxWarps.get()*2);
		this.empire = empire;
		this.construct();
	}
	
	private String dupeWarpCheck() {
		if (printedWarps.isEmpty() != true) {
			for (int i=0; i < printedWarps.size(); i++) {
				String oldwarpname = printedWarps.get(i);
				return oldwarpname;
			}
		}
		return "null";	
	}

	private void construct() {
		for (Teleport warp : empire.Warps) {
			if (warp.getEmpire() == empire) {
					String oldwarpname = dupeWarpCheck();
					if (warp.getName() != oldwarpname) {
						IChatComponent warpname = new ChatComponentFormatted("");
						warpname.appendSibling(new ChatComponentFormatted("{7| }"));
						warpname.appendSibling(new ChatComponentFormatted("{7| " + warp.getName().toString() +" }"));
						this.add(new ChatComponentFormatted("{7| --> }{%s}", warpname));
						printedWarps.add(warp.getName());
					} 
					
					if (oldwarpname == "null") {
						IChatComponent warpname = new ChatComponentFormatted("");
						warpname.appendSibling(new ChatComponentFormatted("{7| }"));
						warpname.appendSibling(new ChatComponentFormatted("{7| " + warp.getName().toString() +" }"));
						this.add(new ChatComponentFormatted("{7| --> }{%s}", warpname));
						printedWarps.add(warp.getName());
					}
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