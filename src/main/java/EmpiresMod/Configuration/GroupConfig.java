package EmpiresMod.Configuration;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import EmpiresMod.API.JSON.Configuration.JsonConfig;
import EmpiresMod.API.permissions.Bridges.MyPermissionsBridge;
import EmpiresMod.entities.Permissions.Group;
import EmpiresMod.entities.Permissions.Meta;

public class GroupConfig extends JsonConfig<Group, Group.Container> {

	private MyPermissionsBridge permissionManager;

	public GroupConfig(String path, MyPermissionsBridge permissionManager) {
		super(path, "GroupConfig");
		this.permissionManager = permissionManager;
		this.gsonType = new TypeToken<Group.Container>() {
		}.getType();
		this.gson = new GsonBuilder().registerTypeAdapter(Group.class, new Group.Serializer())
				.registerTypeAdapter(Meta.Container.class, new Meta.Container.Serializer()).setPrettyPrinting()
				.create();
	}

	@Override
	protected Group.Container newList() {
		return new Group.Container();
	}

	@Override
	public void create(Group.Container items) {
		items.add(new Group());
		super.create(items);
	}

	@Override
	public Group.Container read() {
		Group.Container groups = super.read();

		permissionManager.groups.addAll(groups);

		return groups;
	}

	@Override
	public boolean validate(Group.Container items) {
		if (items.size() == 0) {
			items.add(new Group(Config.instance.defaultGroup.get()));
			return false;
		}

		return true;
	}
}