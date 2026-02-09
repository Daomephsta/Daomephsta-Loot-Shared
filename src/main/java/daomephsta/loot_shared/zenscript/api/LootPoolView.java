package daomephsta.loot_shared.zenscript.api;

import java.util.List;

import crafttweaker.annotations.ZenRegister;
import daomephsta.loot_shared.DaomephstaLootShared;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass(DaomephstaLootShared.ZEN_PACKAGE + ".LootPoolView")
public interface LootPoolView
{
    @ZenMethod
    @ZenGetter("conditions")
    public List<ZenLootCondition> getConditions();
}
