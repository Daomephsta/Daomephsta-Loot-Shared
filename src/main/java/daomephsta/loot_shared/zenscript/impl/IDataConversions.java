package daomephsta.loot_shared.zenscript.impl;

import java.util.Collections;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import daomephsta.loot_shared.ErrorHandler;
import daomephsta.loot_shared.zenscript.api.ZenLootCondition;
import daomephsta.loot_shared.zenscript.api.ZenLootFunction;
import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenExpansion;


@ZenRegister
@ZenExpansion("crafttweaker.data.IData")
public class IDataConversions
{
    private static Impl IMPLEMENTATION = new Impl(JsonMapConversions.IMPLEMENTATION, new ErrorHandler.CraftTweakerLog());

    @ZenCaster
    public static ZenLootCondition asLootCondition(IData json)
    {
        return IMPLEMENTATION.asLootCondition(json);
    }

    @ZenCaster
    public static ZenLootFunction asLootFunction(IData json)
    {
        return IMPLEMENTATION.asLootFunction(json);
    }

    @VisibleForTesting
    public static class Impl
    {
        private final JsonMapConversions.Impl jsonMapConversions;
        private final ErrorHandler errorHandler;

        public Impl(JsonMapConversions.Impl jsonMapConversions, ErrorHandler errorHandler)
        {
            this.jsonMapConversions = jsonMapConversions;
            this.errorHandler = errorHandler;
        }

        @VisibleForTesting
        public ZenLootCondition asLootCondition(IData json)
        {
            return jsonMapConversions.asLootCondition(asStringKeyedMap(json));
        }

        @VisibleForTesting
        public ZenLootFunction asLootFunction(IData json)
        {
            return jsonMapConversions.asLootFunction(asStringKeyedMap(json));
        }

        private Map<String, IData> asStringKeyedMap(IData json)
        {
            Map<String, IData> map = json.asMap();
            if (map == null)
            {
                errorHandler.error("Expected map, got %s", CraftTweakerAPI.getScriptFileAndLine(), json);
                return Collections.emptyMap();
            }
            return map;
        }
    }
}
