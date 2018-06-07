package EmpiresMod.API.Chat.Component;

import net.minecraft.command.ICommandSender;

/**
 * A multi-page IChatComponent container. Used for sending large amount of lines
 * to a player.
 */
public class ChatComponentMultiPage extends ChatComponentContainer {

	private int maxComponentsPerPage = 15;

	public ChatComponentMultiPage(int maxComponentsPerPage) {
		this.maxComponentsPerPage = maxComponentsPerPage;
	}

	public void sendPage(ICommandSender sender, int page) {
		getHeader(page).send(sender);
		getPage(page).send(sender);
	}

	public ChatComponentContainer getHeader(int page) {
		ChatComponentContainer header = new ChatComponentContainer();
		header.add(
				new ChatComponentFormatted("{6| ===========[Empires]>>>>[Menu]<<<<[Page: %s/%s]===========}", page, getNumberOfPages()));

		return header;
	}

	public ChatComponentContainer getPage(int page) {
		ChatComponentContainer result = new ChatComponentContainer();
		result.addAll(this.subList(maxComponentsPerPage * (page - 1),
				maxComponentsPerPage * page > size() ? size() : maxComponentsPerPage * page));
		return result;
	}

	public int getNumberOfPages() {
		return (int) Math.ceil((float) size() / (float) maxComponentsPerPage);
	}
}