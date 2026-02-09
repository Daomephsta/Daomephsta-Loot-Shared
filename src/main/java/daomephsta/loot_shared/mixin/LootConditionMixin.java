package daomephsta.loot_shared.mixin;

import org.spongepowered.asm.mixin.Mixin;

import daomephsta.loot_shared.zenscript.api.ZenLootCondition;
import net.minecraft.world.storage.loot.conditions.LootCondition;


@Mixin(LootCondition.class)
public interface LootConditionMixin extends ZenLootCondition
{
    
}