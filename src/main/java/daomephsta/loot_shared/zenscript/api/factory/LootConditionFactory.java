package daomephsta.loot_shared.zenscript.api.factory;

import daomephsta.loot_shared.zenscript.api.ZenLootCondition;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import stanhebben.zenscript.annotations.ZenMethod;


public class LootConditionFactory
{
    @ZenMethod
    public ZenLootCondition randomChance(float chance)
    {
        return (ZenLootCondition) new RandomChance(chance);
    }

    @ZenMethod
    public ZenLootCondition randomChanceWithLooting(float chance, float lootingMultiplier)
    {
        return (ZenLootCondition) new RandomChanceWithLooting(chance, lootingMultiplier);
    }

    @ZenMethod
    public ZenLootCondition killedByPlayer()
    {
        return (ZenLootCondition) new KilledByPlayer(false);
    }

    @ZenMethod
    public ZenLootCondition killedByNonPlayer()
    {
        return (ZenLootCondition) new KilledByPlayer(true);
    }

    @ZenMethod
    public ZenLootCondition zenscript(ZenLambdaLootCondition.Delegate delegate)
    {
        return (ZenLootCondition) new ZenLambdaLootCondition(delegate);
    }
}
