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
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;

@Mixin(LootPool.class)
public class LootPoolMixin
{
    @Unique
    private static final Logger ZEN_LOOT_SANITY_LOGGER = LogManager.getLogger(DaomephstaLootShared.ID + ".sanity_checks");

    @Inject(method = "<init>", at = @At("TAIL"))
    public void loot_carpenter$uniqueEntryNames(LootEntry[] entries, LootCondition[] conditions,
        RandomValueRange rolls, RandomValueRange bonusRolls, String poolName, CallbackInfo info)
    {
        LootLoadingContext.ifAvailableOrElse(context ->
        {
            Set<String> usedNames = new HashSet<>();
            // Temp variable to make other errors aware of fixed pool names
            String poolName0 = poolName;
            if (poolName0.startsWith("custom#"))
                poolName0 = context.nameFixer.fixCustomPoolName((LootPoolAccessors) this);
            for (LootEntry entry : entries)
            {
                if (!usedNames.add(entry.getEntryName()))
                    context.nameFixer.deduplicateEntryName(poolName0, entry);
                else if (entry.getEntryName().startsWith("custom#"))
                    context.nameFixer.fixCustomEntryName(entry);
            }
        },
        () -> LootNameFixer.ignoreManualPool(poolName));
    }
}
