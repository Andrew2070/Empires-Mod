package EmpiresMod.API.Chat.Component;

import java.util.ArrayList;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;

/**
 * A set of IChatComponents that can be sent as a whole
 */
public class ChatComponentContainer extends ArrayList<IChatComponent> {
	/**
	 * Sends all chat components to the sender
	 */
	public void send(ICommandSender sender) {
		for (IChatComponent component : this) {
			sender.addChatMessage(component);
		}
	}
}