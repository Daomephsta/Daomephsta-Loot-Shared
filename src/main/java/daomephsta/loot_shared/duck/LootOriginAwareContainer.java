package daomephsta.loot_shared.duck;

import daomephsta.loot_shared.DaomephstaLootShared;
import net.minecraft.util.ResourceLocation;

public interface LootOriginAwareContainer
{
    static final String NBT_KEY = DaomephstaLootShared.ID + ":loot_origin";

    public ResourceLocation daomephsta_loot_shared$getLootOrigin();
}
