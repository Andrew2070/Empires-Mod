package com.EmpireMod.Empires.entities.Permissions;


import com.google.gson.*;
import com.EmpireMod.Empires.API.JSON.API.SerializerTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * Variables inside permission strings.
 */
public class Meta {

    public final String permission;
    public final int metadata;

    public Meta(String permission, int metadata) {
        this.permission = permission;
        this.metadata = metadata;
    }

    public static class Container extends ArrayList<Meta> {

        public Meta get(String permission) {
            for(Meta item : this) {
                if(item.permission.equals(permission)) {
                    return item;
                }
            }
            return null;
        }

        /**
         * Since Meta is represented by a "key":value format in Json it needs to stay in the Container rather than in the Meta class
         */
        public static class Serializer extends SerializerTemplate<Container> {

            @Override
            public void register(GsonBuilder builder) {
                builder.registerTypeAdapter(Container.class, this);
            }

            @Override
            public Container deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();

                Container container = new Container();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    container.add(new Meta(entry.getKey(), entry.getValue().getAsInt()));
                }

                return container;
            }

            @Override
            public JsonElement serialize(Container container, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();

                for (Meta meta : container) {
                    jsonObject.addProperty(meta.permission, meta.metadata);
                }

                return jsonObject;
            }
        }
    }
}