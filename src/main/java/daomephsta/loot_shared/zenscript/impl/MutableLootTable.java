package daomephsta.loot_shared.zenscript.impl;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import daomephsta.loot_shared.ErrorHandler;
import daomephsta.loot_shared.mixin.LootPoolAccessors;
import daomephsta.loot_shared.mixin.LootTableAccessors;
import daomephsta.loot_shared.utility.RandomValueRanges;
import daomephsta.loot_shared.zenscript.api.EditableLootPool;
import daomephsta.loot_shared.zenscript.api.EditableLootTable;
import daomephsta.loot_shared.zenscript.impl.MutableLootPool.QualifiedPoolIdentifier;
import daomephsta.loot_shared.zenscript.impl.entry.MutableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class MutableLootTable implements EditableLootTable
{
    private final ResourceLocation id;
    private final Map<String, MutableLootPool> pools;
    private final ErrorHandler errorHandler;

    public MutableLootTable(ResourceLocation id, Map<String, MutableLootPool> pools, ErrorHandler errorHandler)
    {
        this.id = id;
        this.pools = pools;
        this.errorHandler = errorHandler;
    }

    public static MutableLootTable fromTable(LootTable table, ResourceLocation tableName, ErrorHandler errorHandler)
    {
        Function<LootPool, MutableLootPool> toMutablePool = (pool) ->
        {
            Map<String, MutableLootEntry> entries = new LinkedHashMap<>();
            for (LootEntry entry : ((LootPoolAccessors) pool).getEntries())
            {
                MutableLootEntry mutableEntry = MutableLootEntry.from(entry);
                entries.put(mutableEntry.getName(), mutableEntry);
            }
            List<LootCondition> conditions = ((LootPoolAccessors) pool).getConditions();
            return new MutableLootPool(
                new QualifiedPoolIdentifier(tableName, pool.getName()),
                entries,
                conditions,
                pool.getRolls(),
                pool.getBonusRolls(), errorHandler);
        };

        Map<String, MutableLootPool>  pools = new LinkedHashMap<>();
        for (LootPool pool : ((LootTableAccessors) table).getPools())
        {
            MutableLootPool mutablePool = toMutablePool.apply(pool);
            pools.put(mutablePool.getName(), mutablePool);
        }
        return new MutableLootTable(tableName, pools, errorHandler);
    }

    public MutableLootTable deepClone()
    {
        //Can never be duplicate entries when deep cloning, but be informative just in case
        BinaryOperator<MutableLootPool> mergeFunction = (a, b) ->
        {
            throw new IllegalStateException(String.format(
                "Unexpected duplicate pool '%s' while deep cloning mutable table '%s'. Report this to the mod author",
                a.getName(), id));
        };
        Map<String, MutableLootPool> poolsDeepClone = pools.entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, e -> e.getValue().deepClone(), mergeFunction, HashMap::new));
        return new MutableLootTable(id, poolsDeepClone, errorHandler);
    }

    public LootTable toImmutable()
    {
        LootPool[] poolsArray = pools.values().stream().map(MutableLootPool::toImmutable).toArray(LootPool[]::new);
        return new LootTable(poolsArray);
    }

    public ResourceLocation getId()
    {
        return id;
    }

    public Map<String, MutableLootPool> getPools()
    {
        return pools;
    }

    @Override
    public MutableLootPool getPool(String name)
    {
        MutableLootPool pool = getPoolNullable(name);
        if (pool == null)
            errorHandler.error("No loot pool with name %s exists in table %s!", name, id);
        return pool;
    }

	public MutableLootPool getPoolNullable(String name) 
	{
		return pools.get(name);
	}

    @Override
    public EditableLootPool addPool(String name, float minRolls, float maxRolls, float minBonusRolls, float maxBonusRolls)
    {
        return addPool(new MutableLootPool(new QualifiedPoolIdentifier(id, name), new LinkedHashMap<>(), new LinkedList<>(),
            RandomValueRanges.checked(errorHandler, minRolls, maxRolls),
            RandomValueRanges.checked(errorHandler, minBonusRolls, maxBonusRolls), errorHandler));
    }

    public MutableLootPool addPool(MutableLootPool pool)
    {
        MutableLootPool existing = pools.putIfAbsent(pool.getName(), pool);
        if (existing != null)
            errorHandler.error("Duplicate pool name '%s' in table '%s'", pool.getName(), id);
        return pool;
    }

    @Override
    public void removePool(String name)
    {
        if (pools.remove(name) == null)
            errorHandler.error("No loot pool with name %s exists in table %s!", name, id);
    }

    @Override
    public void removeAllPools()
    {
        pools.clear();
    }
}
