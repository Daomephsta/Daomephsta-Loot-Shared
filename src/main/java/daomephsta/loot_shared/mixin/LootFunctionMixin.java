package daomephsta.loot_shared.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import daomephsta.loot_shared.zenscript.api.ZenLootCondition;
import daomephsta.loot_shared.zenscript.api.ZenLootFunction;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;


@Mixin(LootFunction.class)
public class LootFunctionMixin implements ZenLootFunction
{
    @Shadow
    private @Mutable LootCondition[] conditions;

    @Override
    public ZenLootCondition[] getConditions()
    {
        return (ZenLootCondition[]) conditions;
    }

    @Override
    public void setConditions(ZenLootCondition[] conditions)
    {
        this.conditions = ZenLootCondition.toVanilla(conditions);
    }
}