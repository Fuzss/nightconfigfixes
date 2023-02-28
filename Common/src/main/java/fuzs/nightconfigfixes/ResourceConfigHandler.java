package fuzs.nightconfigfixes;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.io.FileReader;
import java.util.Map;

public class ResourceConfigHandler {
    private static final Map<String, PackSelectionOverride> OVERRIDES_BY_ID = Maps.newHashMap();
    private static PackSelectionOverride defaultOverride;

    public static PackSelectionOverride getOverride(String id) {
        if (defaultOverride == null) load();
        return OVERRIDES_BY_ID.getOrDefault(id, defaultOverride);
    }

    private static void load() {
        defaultOverride = PackSelectionOverride.EMPTY;
        JsonConfigFileUtil.getAndLoad("resource_pack_overrides.json", file -> {}, ResourceConfigHandler::deserializeAllOverrides);
    }

    private static void deserializeAllOverrides(FileReader reader) {
        OVERRIDES_BY_ID.clear();
        JsonElement jsonElement = JsonConfigFileUtil.GSON.fromJson(reader, JsonElement.class);
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "resource pack override");
        if (jsonObject.has("default")) {
            defaultOverride = deserializeOverrideEntry(jsonObject.get("default"));
        }
        JsonObject overrides = jsonObject.getAsJsonObject("overrides");
        for (Map.Entry<String, JsonElement> entry : overrides.entrySet()) {
            OVERRIDES_BY_ID.put(entry.getKey(), deserializeOverrideEntry(entry.getValue()));
        }
    }

    private static PackSelectionOverride deserializeOverrideEntry(JsonElement jsonElement) {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "resource pack override");
        boolean forceCompatible = GsonHelper.getAsBoolean(jsonObject, "force_compatible", false);
        boolean fixedPosition = GsonHelper.getAsBoolean(jsonObject, "fixed_position", false);
        boolean required = GsonHelper.getAsBoolean(jsonObject, "required", false);
        boolean hidden = GsonHelper.getAsBoolean(jsonObject, "hidden", false);
        return new PackSelectionOverride(forceCompatible, fixedPosition, required, hidden);
    }

    public record PackSelectionOverride(boolean forceCompatible, boolean fixedPosition, boolean required, boolean hidden) {

        public static final PackSelectionOverride EMPTY = new PackSelectionOverride(false, false, false, false);
    }
}
