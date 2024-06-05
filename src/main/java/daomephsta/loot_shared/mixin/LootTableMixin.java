package daomephsta.loot_shared.mixin;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import daomephsta.loot_shared.DaomephstaLootShared;
import daomephsta.loot_shared.utility.loot.LootLoadingContext;
import daomephsta.loot_shared.utility.loot.LootNameFixer;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;

@Mixin(LootTable.class)
public class LootTableMixin
{
    @Unique
    private static final Logger ZEN_LOOT_SANITY_LOGGER = LogManager.getLogger(DaomephstaLootShared.ID + ".sanity_checks");

    @Inject(method = "<init>", at = @At("TAIL"))
    public void loot_carpenter$uniquePoolNames(LootPool[] pools, CallbackInfo info)
    {
        if (!LootLoadingContext.available())
            LootNameFixer.ignoreManualTable();
        LootLoadingContext.ifAvailableOrElse(context ->
        {
            Set<String> usedNames = new HashSet<>();
            for (LootPool pool : pools)
            {
                if (!usedNames.add(pool.getName()))
                    context.nameFixer.deduplicatePoolName(pool);
            }
        },
        LootNameFixer::ignoreManualTable);
    }
}
