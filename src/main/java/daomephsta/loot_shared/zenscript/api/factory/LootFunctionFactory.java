package daomephsta.loot_shared.zenscript.api.factory;

import java.util.List;

import com.google.common.collect.Lists;

import crafttweaker.api.data.IData;
import crafttweaker.mc1120.data.NBTConverter;
import daomephsta.loot_shared.ErrorHandler;
import daomephsta.loot_shared.utility.RandomValueRanges;
import daomephsta.loot_shared.utility.loot.LootConditions;
import daomephsta.loot_shared.zenscript.api.ZenLootFunction;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.loot.functions.EnchantRandomly;
import net.minecraft.world.storage.loot.functions.EnchantWithLevels;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetDamage;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraft.world.storage.loot.functions.SetNBT;
import net.minecraft.world.storage.loot.functions.Smelt;
import stanhebben.zenscript.annotations.ZenMethod;


public class LootFunctionFactory
{
    private final ErrorHandler errorHandler;

    public LootFunctionFactory(ErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    @ZenMethod
    public ZenLootFunction enchantRandomly(String[] enchantIDList)
    {
        if (!errorHandler.nonNull("enchantment IDs", enchantIDList))
            return ZenLootFunction.INVALID;
        List<Enchantment> enchantments = Lists.newArrayListWithCapacity(enchantIDList.length);
        for (String id : enchantIDList)
        {
            Enchantment ench = Enchantment.getEnchantmentByLocation(id);
            if (ench == null)
            {
                errorHandler.error("%s is not a valid enchantment id", id);
                continue;
            }
            enchantments.add(ench);
        }
        return (ZenLootFunction) new EnchantRandomly(LootConditions.NONE, enchantments);
    }

    @ZenMethod
    public ZenLootFunction enchantWithLevels(int min, int max, boolean isTreasure)
    {
        return (ZenLootFunction) new EnchantWithLevels(LootConditions.NONE,
            RandomValueRanges.checked(errorHandler, min, max), isTreasure);
    }

    @ZenMethod
    public ZenLootFunction lootingEnchantBonus(int min, int max, int limit)
    {
        return (ZenLootFunction) new LootingEnchantBonus(LootConditions.NONE,
            RandomValueRanges.checked(errorHandler, min, max), limit);
    }

    @ZenMethod
    public ZenLootFunction setCount(int min, int max)
    {
        return (ZenLootFunction) new SetCount(LootConditions.NONE, RandomValueRanges.checked(errorHandler, min, max));
    }

    @ZenMethod
    public ZenLootFunction setDamage(float min, float max)
    {
        if (max > 1.0F)
        {
            errorHandler.error("Items cannot recieve more than 100% damage!");
            return ZenLootFunction.INVALID;
        }
        return (ZenLootFunction) new SetDamage(LootConditions.NONE, RandomValueRanges.checked(errorHandler, min, max));
    }

    @ZenMethod
    public ZenLootFunction setMetadata(int min, int max)
    {
        return (ZenLootFunction) new SetMetadata(LootConditions.NONE, RandomValueRanges.checked(errorHandler, min, max));
    }

    @ZenMethod
    public ZenLootFunction setNBT(IData nbtData)
    {
        NBTBase nbt = NBTConverter.from(nbtData);
        if (!(nbt instanceof NBTTagCompound))
        {
            errorHandler.error("Expected compound nbt tag, got %s", nbtData);
            return ZenLootFunction.INVALID;
        }
        return (ZenLootFunction) new SetNBT(LootConditions.NONE, (NBTTagCompound) nbt);
    }

    @ZenMethod
    public ZenLootFunction smelt()
    {
        return (ZenLootFunction) new Smelt(LootConditions.NONE);
    }

    @ZenMethod
    public ZenLootFunction zenscript(ZenLambdaLootFunction.Delegate delegate)
    {
        return (ZenLootFunction) new ZenLambdaLootFunction(delegate, LootConditions.NONE);
    }
}
