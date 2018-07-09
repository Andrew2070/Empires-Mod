package EmpiresMod.API.permissions;

import java.util.Arrays;
import java.util.List;

import EmpiresMod.API.permissions.Bridges.ForgeEssentialsPermissionBridge;
import net.minecraftforge.fml.common.Optional;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/**
 * Command model which instantiates all base commands that need to be registered
 * to Minecraft
 */
@Optional.InterfaceList({
		@Optional.Interface(iface = "net.minecraftforge.permission.PermissionObject", modid = "ForgeEssentials") })
public class CommandModel extends CommandBase implements PermissionObject {

	private CommandTree commandTree;

	public CommandModel(CommandTree commandTree) {
		this.commandTree = commandTree;
	}


	public List getCommandAliases() {
		return Arrays.asList(commandTree.getRoot().getAnnotation().alias());
	}


	public String getCommandName() {
		return commandTree.getRoot().getLocalizedName();
	}

	public String getCommandUsage(ICommandSender sender) {
		return commandTree.getRoot().getLocalizedSyntax();
	}

	/**
	 * Processes the command by calling the method that was linked to it.
	 * @throws CommandException 
	 */

	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		commandTree.commandCall(sender, Arrays.asList(args));
	}


	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		CommandTreeNode node = commandTree.getNodeFromArgs(Arrays.asList(args));

		int argumentNumber = commandTree.getArgumentNumber(Arrays.asList(args));
		if (argumentNumber < 0)
			return null;

		return node.getTabCompletionList(argumentNumber, args[args.length - 1]);
	}

	/**
	 * This method does not have enough arguments to check for subcommands down
	 * the command trees therefore it always returns true. The check is moved
	 * directly to the processCommand method.
	 */

	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public String getPermissionNode() {
		return commandTree.getRoot().getAnnotation().permission();
	}

	@Override
	@Optional.Method(modid = "ForgeEssentials")
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel
				.fromBoolean(!(PermissionProxy.getPermissionManager() instanceof ForgeEssentialsPermissionBridge));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(ICommandSender sender, String[] args) throws CommandException {
		// TODO Auto-generated method stub
		
	}
}
