package EmpiresMod.Configuration;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import EmpiresMod.API.JSON.Configuration.JsonConfig;
import EmpiresMod.API.permissions.Bridges.EmpiresBridge;
import EmpiresMod.entities.Permissions.Meta;
import EmpiresMod.entities.Permissions.User;

public class UserConfig extends JsonConfig<User, User.Container> {

	private EmpiresBridge permissionsManager;

	public UserConfig(String path, EmpiresBridge permissionsManager) {
		super(path, "UserConfig");
		this.permissionsManager = permissionsManager;
		this.gsonType = new TypeToken<User.Container>() {
		}.getType();
		this.gson = new GsonBuilder().registerTypeAdapter(User.class, new User.Serializer())
				.registerTypeAdapter(Meta.Container.class, new Meta.Container.Serializer()).setPrettyPrinting()
				.create();
	}

	@Override
	protected User.Container newList() {
		return new User.Container();
	}

	@Override
	public User.Container read() {
		User.Container users = super.read();
		permissionsManager.users.addAll(users);
		return users;
	}
}