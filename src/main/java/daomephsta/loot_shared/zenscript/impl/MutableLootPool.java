package daomephsta.loot_shared.zenscript.impl;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import com.google.common.collect.Lists;

import crafttweaker.api.data.DataMap;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import daomephsta.loot_shared.DaomephstaLootShared;
import daomephsta.loot_shared.ErrorHandler;
import daomephsta.loot_shared.utility.RandomValueRanges;
import daomephsta.loot_shared.utility.loot.LootConditions;
import daomephsta.loot_shared.utility.loot.LootFunctions;
import daomephsta.loot_shared.zenscript.api.EditableLootPool;
import daomephsta.loot_shared.zenscript.api.ZenLootCondition;
import daomephsta.loot_shared.zenscript.api.ZenLootFunction;
import daomephsta.loot_shared.zenscript.impl.entry.MutableLootEntry;
import daomephsta.loot_shared.zenscript.impl.entry.MutableLootEntryEmpty;
import daomephsta.loot_shared.zenscript.impl.entry.MutableLootEntryItem;
import daomephsta.loot_shared.zenscript.impl.entry.MutableLootEntryTable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetDamage;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraft.world.storage.loot.functions.SetNBT;
import stanhebben.zenscript.annotations.Optional;


public class MutableLootPool implements EditableLootPool
{
    private static final String ENTRY_NAME_PREFIX = DaomephstaLootShared.ID + "#";
    private static final int DEFAULT_QUALITY = 0;
    private QualifiedPoolIdentifier qualifiedName;
    private Map<String, MutableLootEntry> entries;
    private List<LootCondition> conditions;
    private RandomValueRange rolls, bonusRolls;
    private final ErrorHandler errorHandler;
    private int nextEntryNameId = 1;

    public MutableLootPool(QualifiedPoolIdentifier name, Map<String, MutableLootEntry> entries, List<LootCondition> conditions,
        RandomValueRange rolls, RandomValueRange bonusRolls, ErrorHandler errorHandler)
    {
        this.qualifiedName = name;
        this.entries = entries;
        this.conditions = conditions;
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
        this.errorHandler = errorHandler;
    }

    public MutableLootPool deepClone()
    {
        //Can never be duplicate entries when deep cloning, but be informative just in case
        BinaryOperator<MutableLootEntry> mergeFunction = (a, b) ->
        {
            throw new IllegalStateException(String.format(
                "Unexpected duplicate entry '%s' while deep cloning mutable pool '%s'. Report this to the mod author",
                a.getName(), getName()));
        };
        Map<String, MutableLootEntry> entriesDeepClone = entries.entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, e -> e.getValue().deepClone(), mergeFunction, HashMap::new));
        return new MutableLootPool(qualifiedName, entriesDeepClone, LootConditions.deepClone(conditions), rolls,
            bonusRolls, errorHandler);
    }

    public LootPool toImmutable()
    {
        LootEntry[] entriesArray = entries.values()
            .stream()
            .map(MutableLootEntry::toImmutable)
            .toArray(LootEntry[]::new);
        return new LootPool(entriesArray, conditions.toArray(LootConditions.NONE), rolls, bonusRolls, qualifiedName.getPoolName());
    }

    public String getName()
    {
        return qualifiedName.getPoolName();
    }

    public void setName(String name)
    {
        this.qualifiedName = new QualifiedPoolIdentifier(this.qualifiedName.getTableId(), name);
    }

    public Map<String, MutableLootEntry> getEntries()
    {
        return entries;
    }

    public MutableLootEntry getEntry(String name)
    {
        return entries.get(name);
    }

    @Override
    public void addItemEntry(IItemStack stack, int weight, @Optional String name)
    {
        if (!errorHandler.nonNull("stack", stack)) return;
        addItemEntryInternal(stack, weight, DEFAULT_QUALITY, LootConditions.NONE, LootFunctions.NONE, name);
    }

    @Override
    public void addItemEntry(IItemStack stack, int weight, int quality, @Optional String name)
    {
        if (!errorHandler.nonNull("stack", stack)) return;
        addItemEntryInternal(stack, weight, quality, LootConditions.NONE, LootFunctions.NONE, name);
    }

    @Override
    public void addItemEntry(IItemStack stack, int weight, int quality, ZenLootFunction[] functions,
        ZenLootCondition[] conditions, @Optional String name)
    {
        if (!errorHandler.nonNull("stack", stack, "functions", functions, "conditions", conditions)) return;
        addItemEntryInternal(stack, weight, quality, ZenLootCondition.toVanilla(conditions), ZenLootFunction.toVanilla(functions), name);
    }

    private void addItemEntryInternal(IItemStack stack, int weight, int quality,
        LootCondition[] conditions, LootFunction[] functions, @Optional String name)
    {
        if (stack == null) return;
        String entryName = name != null ? name : generateName();
        Item item = CraftTweakerMC.getItemStack(stack).getItem();
        addEntry(new MutableLootEntryItem(item, weight, quality,
            Lists.newArrayList(conditions), withStackFunctions(stack, functions), entryName));
    }

    /* Adds loot functions equivalent to the damage, stacksize and NBT of the input
     * stack to the passed in array, if loot functions of the same type are not
     * present. */
    private List<LootFunction> withStackFunctions(IItemStack iStack, LootFunction[] existingFunctions)
    {
        ItemStack stack = CraftTweakerMC.getItemStack(iStack);
        boolean sizeFuncExists = false, damageFuncExists = false, nbtFuncExists = false;
        for (LootFunction lootFunction : existingFunctions)
        {
            if (lootFunction instanceof SetCount) sizeFuncExists = true;
            if (lootFunction instanceof SetDamage || lootFunction instanceof SetMetadata) damageFuncExists = true;
            if (lootFunction instanceof SetNBT) nbtFuncExists = true;
        }
        List<LootFunction> functionsOut = Lists.newArrayListWithCapacity(existingFunctions.length + 3);
        Collections.addAll(functionsOut, existingFunctions);
        if (iStack.getAmount() > 1 && !sizeFuncExists)
            functionsOut.add(new SetCount(LootConditions.NONE, new RandomValueRange(iStack.getAmount())));
        if (iStack.getDamage() > 0 && !damageFuncExists) functionsOut.add(stack.isItemStackDamageable()
            // SetDamage takes a percentage, not a number
            ? new SetDamage(LootConditions.NONE,
                new RandomValueRange((float) stack.getItemDamage() / (float) stack.getMaxDamage()))
                : new SetMetadata(LootConditions.NONE, new RandomValueRange(iStack.getDamage())));
        if (iStack.getTag() != DataMap.EMPTY && !nbtFuncExists)
            functionsOut.add(new SetNBT(LootConditions.NONE, CraftTweakerMC.getNBTCompound(iStack.getTag())));
        return functionsOut;
    }

    @Override
    public void addLootTableEntry(String tableName, int weight, @Optional String name)
    {
        addLootTableEntryInternal(tableName, weight, DEFAULT_QUALITY, LootConditions.NONE, name);
    }

    @Override
    public void addLootTableEntry(String tableName, int weight, int quality, @Optional String name)
    {
        addLootTableEntryInternal(tableName, weight, quality, LootConditions.NONE, name);
    }

    @Override
    public void addLootTableEntry(String tableName, int weight, int quality,
        ZenLootCondition[] conditions, @Optional String name)
    {
        addLootTableEntryInternal(tableName, weight, quality, ZenLootCondition.toVanilla(conditions), name);
    }

    private void addLootTableEntryInternal(String tableName, int weight, int quality, LootCondition[] conditions, @Optional String name)
    {
        String entryName = name != null ? name : generateName();
        addEntry(new MutableLootEntryTable(entryName, weight, quality, conditions, new ResourceLocation(tableName)));
    }

    @Override
    public void addEmptyEntry(int weight, @Optional String name)
    {
        addEmptyEntryInternal(weight, DEFAULT_QUALITY, LootConditions.NONE, name);
    }

    @Override
    public void addEmptyEntry(int weight, int quality, @Optional String name)
    {
        addEmptyEntryInternal(weight, quality, LootConditions.NONE, name);
    }

    @Override
    public void addEmptyEntry(int weight, int quality, ZenLootCondition[] conditions, @Optional String name)
    {
        addEmptyEntryInternal(weight, quality, ZenLootCondition.toVanilla(conditions), name);
    }

    private void addEmptyEntryInternal(int weight, int quality, LootCondition[] conditions, @Optional String name)
    {
        String entryName = name != null ? name : generateName();
        addEntry(new MutableLootEntryEmpty(entryName, weight, quality, conditions));
    }

    public void addEntry(MutableLootEntry entry)
    {
        if (entries.putIfAbsent(entry.getName(), entry) != null) throw new IllegalArgumentException(
            String.format("Duplicate entry name '%s' in pool '%s'", entry.getName(), this.getName()));
    }

    @Override
    public MutableLootEntry removeEntry(String entryName)
    {
        MutableLootEntry removed = entries.remove(entryName);
		if (removed == null)
            errorHandler.error("No entry with name %s exists in %s", entryName, qualifiedName);
		return removed;
    }

    @Override
    public void clearEntries()
    {
        entries.clear();
    }

    private String generateName()
    {
        return ENTRY_NAME_PREFIX + nextEntryNameId++;
    }

    @Override
    public List<ZenLootCondition> getConditions()
    {
        return Lists.transform(conditions, c -> (ZenLootCondition) c);
    }

    @Override
    public void setConditions(ZenLootCondition[] conditions)
    {
        this.conditions = ZenLootCondition.streamToVanilla(conditions)
            .collect(toCollection(ArrayList::new));
    }

    @Override
    public void clearConditions()
    {
        this.conditions.clear();
    }

    @Override
    public void addConditions(ZenLootCondition[] conditions)
    {
        for (ZenLootCondition condition : conditions)
            if (condition instanceof LootCondition)
                this.conditions.add((LootCondition) condition);
    }

    @Override
    public void setRolls(float minRolls, float maxRolls)
    {
        this.rolls = RandomValueRanges.checked(errorHandler, minRolls, maxRolls);
    }

    @Override
    public void setBonusRolls(float minBonusRolls, float maxBonusRolls)
    {
        this.bonusRolls = RandomValueRanges.checked(errorHandler, minBonusRolls, maxBonusRolls);
    }

    /**
     * The "path" to a specific loot pool
     *
     * @author Daomephsta
     */
    public static class QualifiedPoolIdentifier
    {
        private final ResourceLocation tableId;
        private final String poolId;

        public QualifiedPoolIdentifier(ResourceLocation tableId, String poolId)
        {
            this.tableId = tableId;
            this.poolId = poolId;
        }

        public ResourceLocation getTableId()
        {
            return tableId;
        }

        public String getPoolName()
        {
            return poolId;
        }

        @Override
        public String toString()
        {
            return String.format("pool '%s' of table '%s'", poolId, tableId);
        }
    }
}
