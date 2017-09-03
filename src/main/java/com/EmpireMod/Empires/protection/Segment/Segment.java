package com.EmpireMod.Empires.protection.Segment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.EmpireMod.Empires.Empires;
import com.EmpireMod.Empires.API.JSON.API.SerializerTemplate;
import com.EmpireMod.Empires.Datasource.EmpiresUniverse;
import com.EmpireMod.Empires.entities.Empire.Citizen;
import com.EmpireMod.Empires.entities.Flags.FlagType;
import com.EmpireMod.Empires.entities.Misc.Volume;
import com.EmpireMod.Empires.exceptions.Empires.ConditionException;
import com.EmpireMod.Empires.exceptions.Protection.GetterException;
import com.EmpireMod.Empires.exceptions.Protection.ProtectionParseException;
import com.EmpireMod.Empires.protection.ProtectionManager;
import com.EmpireMod.Empires.protection.Segment.Caller.Caller;
import com.EmpireMod.Empires.protection.Segment.Caller.CallerFormula;
import com.EmpireMod.Empires.protection.Segment.Enums.BlockType;
import com.EmpireMod.Empires.protection.Segment.Enums.EntityType;
import com.EmpireMod.Empires.protection.Segment.Enums.ItemType;
import com.EmpireMod.Empires.protection.Segment.Enums.Priority;
import com.EmpireMod.Empires.protection.Segment.Getter.Getter;
import com.EmpireMod.Empires.protection.Segment.Getter.GetterDynamic;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.internal.LazilyParsedNumber;

import net.minecraft.entity.player.EntityPlayer;

/**
 * A part of the protection that protects against a specific thing.
 */
public abstract class Segment {
	protected boolean isDisabled = false;
	protected Priority priority = Priority.NORMAL;
	protected Class<?> checkClass;
	protected Condition condition;
	protected final List<FlagType<Boolean>> flags = new ArrayList<FlagType<Boolean>>();
	protected final Getter.Container getters = new Getter.Container();

	public boolean isDisabled() {
		return isDisabled;
	}

	public boolean shouldCheckType(Class<?> clazz) {
		return checkClass.isAssignableFrom(clazz);
	}

	public Class<?> getCheckClass() {
		return this.checkClass;
	}

	public Priority getPriority() {
		return this.priority;
	}

	public Citizen getOwner(Object object) {
		if (!getters.contains("owner")) {
			return null;
		}
		try {
			Object ownerObj = null;
			ownerObj = getters.get("owner").invoke(Object.class, object, object);
			if (ownerObj instanceof EntityPlayer) {
				return EmpiresUniverse.instance.getOrMakeCitizen((EntityPlayer) ownerObj);
			} else if (ownerObj instanceof String) {
				String username = (String) ownerObj;
				if (username.length() == 36 && (username.split("-", -1).length - 1) == 4) {
					UUID uuid = UUID.fromString(username);
					return EmpiresUniverse.instance.getOrMakeCitizen(uuid);
				}
				return EmpiresUniverse.instance.getOrMakeCitizen(username);
			} else if (ownerObj instanceof UUID) {
				return EmpiresUniverse.instance.getOrMakeCitizen((UUID) ownerObj);
			}
		} catch (GetterException ex) {
		}

		// Changed to EmpiresUniverse.instance from Empires.instance in above
		// lines^ (5 of them)
		return null;
	}

	protected boolean hasPermissionAtLocation(Citizen res, int dim, int x, int y, int z) {
		if (res != null && res.getFakePlayer()) {
			if (!ProtectionManager.hasPermission(res, FlagType.FAKERS, dim, x, y, z)) {
				return false;
			}
		} else {
			for (FlagType<Boolean> flagType : flags) {
				if (!ProtectionManager.hasPermission(res, flagType, dim, x, y, z)) {
					return false;
				}
			}
		}
		return true;
	}

	protected boolean hasPermissionAtLocation(Citizen res, int dim, Volume volume) {
		if (res != null && res.getFakePlayer()) {
			if (!ProtectionManager.hasPermission(res, FlagType.FAKERS, dim, volume)) {
				return false;
			}
		} else {
			for (FlagType<Boolean> flagType : flags) {
				if (!ProtectionManager.hasPermission(res, flagType, dim, volume)) {
					return false;
				}
			}
		}
		return true;
	}

	protected boolean shouldCheck(Object object) {
		try {
			if (condition != null && !condition.execute(object, getters)) {
				return false;
			}
		} catch (GetterException ex) {
			Empires.instance.LOG.error("Encountered error when checking condition for {}", checkClass.getSimpleName());
			Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
			disable();
		} catch (ConditionException ex) {
			Empires.instance.LOG.error("Encountered error when checking condition for {}", checkClass.getSimpleName());
			Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
			disable();
		}
		return true;
	}

	protected int getRange(Object object) {
		if (!getters.contains("range")) {
			return 0;
		}
		try {
			Object rangeObj = null;
			rangeObj = getters.get("range").invoke(Object.class, object, object);
			if (rangeObj instanceof LazilyParsedNumber) {
				return ((LazilyParsedNumber) rangeObj).intValue();
			} else if (rangeObj instanceof Double) {
				return (int) ((Double) rangeObj + 0.5);
			} else if (rangeObj instanceof Integer) {
				return (Integer) rangeObj;
			}
		} catch (GetterException ex) {
		}
		return 0;
	}

	protected void disable() {
		Empires.instance.LOG.error("Disabling segment for {}", checkClass.getName());
		Empires.instance.LOG.info("Reload protections to enable it again.");
		this.isDisabled = true;
	}

	public static class Serializer extends SerializerTemplate<Segment> {

		@Override
		public void register(GsonBuilder builder) {
			builder.registerTypeAdapter(Segment.class, this);
			new Getter.Serializer().register(builder);
			new Volume.Serializer().register(builder);
			new FlagType.Serializer().register(builder);
		}

		@Override
		public JsonElement serialize(Segment segment, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			json.addProperty("class", segment.checkClass.getName());

			if (segment instanceof SegmentSpecialBlock) {
				json.addProperty("type", "specialBlock");
				serializeSpecialBlock((SegmentSpecialBlock) segment, json, context);
			} else {
				if (segment instanceof SegmentBlock) {
					json.addProperty("type", "block");
					serializeBlock((SegmentBlock) segment, json, context);
				} else if (segment instanceof SegmentEntity) {
					json.addProperty("type", "entity");
					serializeEntity((SegmentEntity) segment, json, context);
				} else if (segment instanceof SegmentItem) {
					json.addProperty("type", "item");
					serializeItem((SegmentItem) segment, json, context);
				} else if (segment instanceof SegmentTileEntity) {
					json.addProperty("type", "tileEntity");
					serializeTileEntity((SegmentTileEntity) segment, json, context);
				}

				json.add("flags", serializeAsElementOrArray(segment.flags, context));

				if (segment.condition != null) {
					json.addProperty("condition", segment.condition.toString());
				}
				if (segment.priority != Priority.NORMAL) {
					json.addProperty("priority", segment.priority.toString());
				}
				for (Getter getter : segment.getters) {
					json.add(getter.getName(), context.serialize(getter, Getter.class));
				}
			}

			return json;
		}

		private <T> JsonElement serializeAsElementOrArray(List<T> items, JsonSerializationContext context) {
			if (items.isEmpty()) {
				return null;
			}

			if (items.size() == 1) {
				return context.serialize(items.get(0));
			} else {
				return context.serialize(items);
			}
		}

		private void serializeBlock(SegmentBlock segment, JsonObject json, JsonSerializationContext context) {
			json.add("actions", serializeAsElementOrArray(segment.types, context));
			json.addProperty("meta", segment.getMeta());
			if (segment.clientUpdate != null) {
				JsonObject jsonUpdate = new JsonObject();
				jsonUpdate.add("coords", context.serialize(segment.clientUpdate.relativeCoords));
				json.add("clientUpdate", jsonUpdate);
			}
		}

		private void serializeSpecialBlock(SegmentSpecialBlock segment, JsonObject json,
				JsonSerializationContext context) {
			json.addProperty("meta", segment.getMeta());
			json.addProperty("isAlwaysBreakable", segment.isAlwaysBreakable);
		}

		private void serializeEntity(SegmentEntity segment, JsonObject json, JsonSerializationContext context) {
			json.add("actions", serializeAsElementOrArray(segment.types, context));
		}

		private void serializeItem(SegmentItem segment, JsonObject json, JsonSerializationContext context) {
			json.add("actions", serializeAsElementOrArray(segment.types, context));
			json.addProperty("damage", segment.getDamage());
			json.addProperty("isAdjacent", segment.isAdjacent);
			if (segment.clientUpdate != null) {
				JsonObject jsonUpdate = new JsonObject();
				jsonUpdate.add("coords", context.serialize(segment.clientUpdate.relativeCoords));
				jsonUpdate.addProperty("directional", segment.directionalClientUpdate);
				json.add("clientUpdate", jsonUpdate);
			}
			if (segment.inventoryUpdate != null) {
				JsonObject jsonUpdate = new JsonObject();
				if (segment.inventoryUpdate.getMode() == 2)
					jsonUpdate.addProperty("hand", true);
				else if (segment.inventoryUpdate.getMode() == 1)
					jsonUpdate.addProperty("full", true);
				json.add("inventoryUpdate", jsonUpdate);
			}
		}

		private void serializeTileEntity(SegmentTileEntity segment, JsonObject json, JsonSerializationContext context) {
			json.addProperty("retainsOwner", segment.retainsOwner);
		}

		@Override
		public Segment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if (!json.getAsJsonObject().has("class")) {
				throw new ProtectionParseException("One of the segments is missing a class identifier");
			}

			JsonObject jsonObject = json.getAsJsonObject();
			String classString = jsonObject.get("class").getAsString();

			if (!json.getAsJsonObject().has("type")) {
				throw new ProtectionParseException("Segment for " + classString + " is missing a type");
			}
			String type = jsonObject.get("type").getAsString();
			jsonObject.remove("type");

			Segment segment = null;
			if ("specialBlock".equals(type)) {
				segment = deserializeSpecialBlock(jsonObject, context);
			} else if ("block".equals(type)) {
				segment = deserializeBlock(jsonObject, context);
			} else if ("entity".equals(type)) {
				segment = deserializeEntity(jsonObject, context);
			} else if ("item".equals(type)) {
				segment = deserializeItem(jsonObject, context);
			} else if ("tileEntity".equals(type)) {
				segment = deserializeTileEntity(jsonObject, context);
			}

			if (segment == null) {
				throw new ProtectionParseException("Identifier type is invalid");
			}

			try {
				segment.checkClass = Class.forName(classString);
			} catch (ClassNotFoundException ex) {
				// throw new ProtectionParseException("Invalid class identifier:
				// " + classString);
				Empires.instance.LOG.error("Invalid class identifier {" + classString + "}: >>> Segment Rejected <<<");
				return null;
			}
			jsonObject.remove("class");

			if (!(segment instanceof SegmentSpecialBlock)) {
				if (!json.getAsJsonObject().has("flags")) {
					throw new ProtectionParseException("Segment for " + classString + " is missing flags");
				}
				segment.flags.addAll(
						deserializeAsArray(jsonObject.get("flags"), context, new TypeToken<FlagType<Boolean>>() {
						}, new TypeToken<List<FlagType<Boolean>>>() {
						}.getType()));
				jsonObject.remove("flags");

				if (jsonObject.has("condition")) {
					segment.condition = new Condition(jsonObject.get("condition").getAsString());
					jsonObject.remove("condition");
				}

				if (jsonObject.has("priority")) {
					segment.priority = Priority.valueOf(jsonObject.get("priority").getAsString());
					jsonObject.remove("priority");
				}

				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					Getter getter = context.deserialize(entry.getValue(), Getter.class);
					getter.setName(entry.getKey());
					getter.setClass(segment.checkClass);
					segment.getters.add(getter);
				}

				for (Getter getter : segment.getters) {
					if (getter instanceof GetterDynamic) {
						GetterDynamic getterDymanic = (GetterDynamic) getter;
						for (Caller caller : getterDymanic.callers) {
							if (caller instanceof CallerFormula) {
								((CallerFormula) caller).setGetters(segment.getters);
							}
						}
					}
				}
			}

			return segment;
		}

		private <T> List<T> deserializeAsArray(JsonElement json, JsonDeserializationContext context,
				TypeToken<T> typeToken, Type listOfT) {
			if (json.isJsonPrimitive()) {
				List<T> list = new ArrayList<T>();
				list.add((T) context.deserialize(json, typeToken.getType()));
				return list;
			} else {
				return context.deserialize(json, listOfT);
			}
		}

		private SegmentBlock deserializeBlock(JsonObject json, JsonDeserializationContext context) {
			if (!json.has("actions")) {
				throw new ProtectionParseException("Missing actions identifier");
			}
			SegmentBlock segment = new SegmentBlock();
			segment.types.addAll(deserializeAsArray(json.get("actions"), context, new TypeToken<BlockType>() {
			}, new TypeToken<List<BlockType>>() {
			}.getType()));
			json.remove("actions");

			if (json.has("meta")) {
				segment.meta = json.get("meta").getAsInt();
				json.remove("meta");
			}

			if (json.has("clientUpdate")) {
				segment.clientUpdate = new ClientBlockUpdate((Volume) context
						.deserialize(json.get("clientUpdate").getAsJsonObject().get("coords"), Volume.class));
				json.remove("clientUpdate");
			}

			return segment;
		}

		private SegmentSpecialBlock deserializeSpecialBlock(JsonObject json, JsonDeserializationContext context) {
			SegmentSpecialBlock segment = new SegmentSpecialBlock();

			if (json.has("meta")) {
				segment.meta = json.get("meta").getAsInt();
				json.remove("meta");
			}

			if (json.has("isAlwaysBreakable")) {
				segment.isAlwaysBreakable = json.get("isAlwaysBreakable").getAsBoolean();
				json.remove("isAlwaysBreakable");
			}

			return segment;
		}

		private SegmentEntity deserializeEntity(JsonObject json, JsonDeserializationContext context) {
			if (!json.has("actions")) {
				throw new ProtectionParseException("Missing actions identifier");
			}

			SegmentEntity segment = new SegmentEntity();

			segment.types.addAll(deserializeAsArray(json.get("actions"), context, new TypeToken<EntityType>() {
			}, new TypeToken<List<EntityType>>() {
			}.getType()));
			json.remove("actions");

			return segment;
		}

		private SegmentItem deserializeItem(JsonObject json, JsonDeserializationContext context) {
			if (!json.has("actions")) {
				throw new ProtectionParseException("Missing actions identifier");
			}

			SegmentItem segment = new SegmentItem();

			segment.types.addAll(deserializeAsArray(json.get("actions"), context, new TypeToken<ItemType>() {
			}, new TypeToken<List<ItemType>>() {
			}.getType()));
			json.remove("actions");

			if (json.has("damage")) {
				segment.damage = json.get("damage").getAsInt();
				json.remove("damage");
			}

			if (json.has("isAdjacent")) {
				segment.isAdjacent = json.get("isAdjacent").getAsBoolean();
				json.remove("isAdjacent");
			}

			if (json.has("clientUpdate")) {
				JsonObject jsonClientUpdate = json.get("clientUpdate").getAsJsonObject();
				segment.clientUpdate = new ClientBlockUpdate(
						(Volume) context.deserialize(jsonClientUpdate.get("coords"), Volume.class));
				if (jsonClientUpdate.has("directional")) {
					segment.directionalClientUpdate = jsonClientUpdate.get("directional").getAsBoolean();
				}
				json.remove("clientUpdate");
			}

			if (json.has("inventoryUpdate")) {
				JsonObject jsonItemUpdate = json.get("inventoryUpdate").getAsJsonObject();
				int mode = jsonItemUpdate.get("hand").getAsBoolean() ? 2
						: jsonItemUpdate.get("full").getAsBoolean() ? 1 : 0;
				if (mode > 0)
					segment.inventoryUpdate = new ClientInventoryUpdate(mode);
				json.remove("inventoryUpdate");
			}

			return segment;
		}

		private SegmentTileEntity deserializeTileEntity(JsonObject json, JsonDeserializationContext context) {
			SegmentTileEntity segment = new SegmentTileEntity();

			segment.retainsOwner = json.getAsJsonObject().get("retainsOwner").getAsBoolean();
			json.remove("retainsOwner");

			return segment;
		}

		private Object getObjectFromPrimitive(JsonPrimitive json) {
			if (json.isBoolean()) {
				return json.getAsBoolean();
			} else if (json.isString()) {
				return json.getAsString();
			} else if (json.isNumber()) {
				return json.getAsNumber();
			}
			return null;
		}
	}

	public static class Container<T extends Segment> extends ArrayList<T> {

		public List<T> get(Class<?> clazz) {
			List<T> usableSegments = new ArrayList<T>();
			for (Segment segment : this) {
				if (!segment.isDisabled() && segment.shouldCheckType(clazz)) {
					usableSegments.add((T) segment);
				}
			}
			if (usableSegments.size() > 1) {
				Priority highestPriority = Priority.LOWEST;
				for (Segment segment : usableSegments) {
					if (highestPriority.ordinal() < segment.getPriority().ordinal()) {
						highestPriority = segment.getPriority();
					}
				}

				for (Iterator<T> it = usableSegments.iterator(); it.hasNext();) {
					Segment segment = it.next();
					if (segment.getPriority() != highestPriority) {
						it.remove();
					}
				}
			}
			return usableSegments;
		}
	}
}