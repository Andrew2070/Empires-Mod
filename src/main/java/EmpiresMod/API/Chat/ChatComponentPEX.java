package EmpiresMod.API.Chat;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class ChatComponentPEX {
	
	public static Player getBukkitPlayer(String playername) {
		Player player = Bukkit.getPlayer(playername);
		return player;
	}
	
	public static String getPrefix(Player player) {
		
		PermissionUser user = PermissionsEx.getUser(player);
		String pexPrefix = user.getPrefix();
		return pexPrefix;
	}
	
	public static String getSuffix (Player player) {
		PermissionUser user = PermissionsEx.getUser(player);
		String pexSuffix = user.getSuffix();
		return pexSuffix;
		
	}
	
}

