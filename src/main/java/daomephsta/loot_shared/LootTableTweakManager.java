package daomephsta.loot_shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import daomephsta.loot_shared.utility.loot.LootTableFinder;
import daomephsta.loot_shared.zenscript.impl.MutableLootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;

public abstract class LootTableTweakManager 
{
	private static final List<LootTableTweakManager> MANAGERS = new ArrayList<>();
	private final ErrorHandler errorHandler;

	public LootTableTweakManager(ErrorHandler errorHandler) 
	{
		this.errorHandler = errorHandler;
	}

	public static <M extends LootTableTweakManager> M register(M manager)
	{
		MANAGERS.add(manager);
		return manager;
	}
	
	public static List<LootTableTweakManager> getManagers() 
	{
		return MANAGERS;
	}
	
	public abstract Collection<ResourceLocation> getEditedTableIds();
	
	public abstract Collection<ResourceLocation> getNewTableIds();
	
	public abstract void applyEdits(MutableLootTable table);
	
    public LootTable withEdits(LootTable table, ResourceLocation tableId)
    {
		MutableLootTable mutable = MutableLootTable.fromTable(table, tableId, errorHandler);
		applyEdits(mutable);
        return mutable.toImmutable();
    }
    
	public abstract Iterator<MutableLootTable> yieldNewTables();
	
	public boolean validateNewTableName(String name, String tweakerModId, boolean warnMinecraftNamespace)
	{
        ResourceLocation tableName = new ResourceLocation(name);
        if (tableName.getNamespace().equals(tweakerModId))
            errorHandler.warn("Table name '%s' uses the %s namespace, this is discouraged", name, tweakerModId);
        if (warnMinecraftNamespace && tableName.getNamespace().equals("minecraft"))
        {
            if (name.startsWith("minecraft"))
                errorHandler.warn("Table name '%s' explicitly uses the minecraft namespace, this is discouraged", name);
            else
                errorHandler.warn("Table name '%s' implicitly uses the minecraft namespace, this is discouraged", name);
        }
        if (LootTableFinder.DEFAULT.exists(tableName) || getNewTableIds().contains(tableName))
        {
            errorHandler.error("Table name '%s' already in use", tableName);
            return false;
        }
        return true;
	}
}
