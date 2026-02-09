package daomephsta.loot_shared.zenscript.api;

import crafttweaker.annotations.ZenRegister;
import daomephsta.loot_shared.DaomephstaLootShared;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass(DaomephstaLootShared.ZEN_PACKAGE + ".LootTableView")
public interface LootTableView
{
    @ZenMethod
    public LootPoolView getPool(String name);
}
