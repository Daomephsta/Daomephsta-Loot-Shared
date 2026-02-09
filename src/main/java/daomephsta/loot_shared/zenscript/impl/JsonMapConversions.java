package daomephsta.loot_shared.zenscript.impl;

import java.util.Map;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import daomephsta.loot_shared.ErrorHandler;
import daomephsta.loot_shared.mixin.LootTableManagerAccessors;
import daomephsta.loot_shared.utility.JsonConverter;
import daomephsta.loot_shared.zenscript.api.ZenLootCondition;
import daomephsta.loot_shared.zenscript.api.ZenLootFunction;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenExpansion;


@ZenRegister
@ZenExpansion("any[any]")
public class JsonMapConversions
{
    static Impl IMPLEMENTATION = new Impl(new ErrorHandler.CraftTweakerLog());

    @ZenCaster
    public static ZenLootCondition asLootCondition(Map<String, IData> json)
    {
        return IMPLEMENTATION.asLootCondition(json);
    }

    @ZenCaster
    public static ZenLootFunction asLootFunction(Map<String, IData> json)
    {
        return IMPLEMENTATION.asLootFunction(json);
    }

    @VisibleForTesting
    public static class Impl
    {
        private final Gson
            lootDeserialiser = LootTableManagerAccessors.getGsonInstance(),
            jsonElementSerialiser = new GsonBuilder()
            .registerTypeHierarchyAdapter(IData.class,
                (JsonSerializer<IData>) (src, type, context) -> JsonConverter.from(src))
            .create();
        private final ErrorHandler errorHandler;

        @VisibleForTesting
        public Impl(ErrorHandler errorHandler)
        {
            this.errorHandler = errorHandler;
        }

        @VisibleForTesting
        public ZenLootCondition asLootCondition(Map<String, ?> json)
        {
            return parse(json, LootCondition.class)
                .map(ZenLootCondition.class::cast)
                .orElse(ZenLootCondition.INVALID);
        }

        @VisibleForTesting
        public ZenLootFunction asLootFunction(Map<String, ?> json)
        {
            return parse(json, LootFunction.class)
                .map(ZenLootFunction.class::cast)
                .orElse(ZenLootFunction.INVALID);
        }

        private <T> Optional<T> parse(Map<String, ?> data, Class<T> clazz)
        {
            if (!errorHandler.nonNull("json", data)) return Optional.empty();
            try
            {
                return Optional.of(lootDeserialiser.fromJson(jsonElementSerialiser.toJsonTree(data), clazz));
            }
            catch (JsonSyntaxException e)
            {
                errorHandler.error(e.getMessage());
                return Optional.empty();
            }
        }
    }
}
