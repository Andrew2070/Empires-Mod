package EmpiresMod.API.permissions.Bridges;

import java.util.UUID;

import EmpiresMod.Constants;
import EmpiresMod.API.permissions.IPermissionBridge;
import EmpiresMod.Configuration.GroupConfig;
import EmpiresMod.Configuration.UserConfig;
import EmpiresMod.entities.Permissions.Group;
import EmpiresMod.entities.Permissions.User;

public class EmpiresBridge implements IPermissionBridge {

	private static final String DEFAULT_GROUP_NAME = "default";

	public final Group.Container groups = new Group.Container();
	public final User.Container users = new User.Container();

	public final GroupConfig groupConfig = new GroupConfig(Constants.CONFIG_FOLDER + "JSON/GroupConfig.json", this);
	public final UserConfig userConfig = new UserConfig(Constants.CONFIG_FOLDER + "JSON/UserConfig.json", this);

	public EmpiresBridge() {
	}

	public void loadConfigs() {
		groups.clear();
		users.clear();

		groupConfig.init(groups);
		userConfig.init(users);
	}

	public void saveConfigs() {
		groupConfig.write(groups);
		userConfig.write(users);
	}

	public void saveGroups() {
		groupConfig.write(groups);
	}

	public void saveUsers() {
		userConfig.write(users);
	}

	@Override
	public boolean hasPermission(UUID uuid, String permission) {
		User user = users.get(uuid);

		return user != null && user.hasPermission(permission);
	}
}