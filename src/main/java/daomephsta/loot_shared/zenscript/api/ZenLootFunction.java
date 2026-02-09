package daomephsta.loot_shared.zenscript.api;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import crafttweaker.annotations.ZenRegister;
import daomephsta.loot_shared.DaomephstaLootShared;
import net.minecraft.world.storage.loot.functions.LootFunction;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

@ZenRegister
@ZenClass(DaomephstaLootShared.ZEN_PACKAGE + ".LootFunction")
public interface ZenLootFunction
{
    public static final ZenLootFunction INVALID = new ZenLootFunction()
    {
        @Override
        public ZenLootCondition[] getConditions()
        {
            return new ZenLootCondition[0];
        }

        @Override
        public void setConditions(ZenLootCondition[] conditions) {}

        @Override
        public void addConditions(ZenLootCondition[] conditions) {}

        @Override
        public String toString()
        {
            return "Invalid loot function";
        }
    };

    public static LootFunction[] toVanilla(ZenLootFunction[] conditions)
    {
        return Arrays.stream(conditions)
            .filter(ZenLootFunction.class::isInstance)
            .toArray(LootFunction[]::new);
    }

    @ZenGetter("conditions")
    public ZenLootCondition[] getConditions();

    @ZenSetter("conditions")
    public void setConditions(ZenLootCondition[] conditions);

    @ZenMethod
    public default void addConditions(ZenLootCondition[] conditions)
    {
        setConditions(ArrayUtils.addAll(getConditions(), conditions));
    }
}