package daomephsta.loot_shared;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.mc1120.commands.CTChatCommand;
import daomephsta.loot_shared.command.CommandLootTables;
import daomephsta.loot_shared.utility.EventBusInspector;
import daomephsta.loot_shared.utility.Texts;
import daomephsta.loot_shared.utility.loot.LootTableFinder;
import daomephsta.loot_shared.utility.loot.dump.LootTableDumper;
import daomephsta.loot_shared.utility.loot.fix.LootFixer;
import daomephsta.loot_shared.zenscript.api.factory.ZenLambdaLootCondition;
import daomephsta.loot_shared.zenscript.api.factory.ZenLambdaLootFunction;
import daomephsta.loot_shared.zenscript.impl.MutableLootTable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
@Mod(
    modid = DaomephstaLootShared.ID, name = DaomephstaLootShared.NAME, version = DaomephstaLootShared.VERSION,
    dependencies = "required-after:crafttweaker@[4.1.20,); before:jeresources; required:forge@[14.23.5.2779,);"
    )
public class DaomephstaLootShared
{
    public static final String NAME = "Daomephsta Loot Shared";
    public static final String ID = "daomephsta_loot_shared";
    public static final String VERSION = "@VERSION@";
    public static final String ZEN_PACKAGE = "mods." + ID;
    private static final Logger LOGGER = LogManager.getLogger(NAME);
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LootFunctionManager.registerFunction(ZenLambdaLootFunction.SERIALISER);
        LootConditionManager.registerCondition(ZenLambdaLootCondition.SERIALISER);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        CTChatCommand.registerCommand(new CommandLootTables());
    }

    public static TextComponentTranslation translation(String keySuffix, Object... args)
    {
        return new TextComponentTranslation(ID + keySuffix, args);
    }

    public static TextComponentTranslation translation(String keySuffix, Consumer<Style> styler, Object... args)
    {
        return Texts.styled(new TextComponentTranslation(ID + keySuffix, args), styler);
    }

    /**
     * @return the names of LootTweaker and Loot Carpenter, if they're installed
     */
    public static Stream<String> getLoadedConsumerNames()
    {
		Map<String, ModContainer> modList = Loader.instance().getIndexedModList();
    	return Stream.of("loottweaker", "loot_carpenter")
    		.filter(Loader::isModLoaded)
    		.map(id -> modList.get(id).getName());
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
    	customTableOverrideWarnings(LootTableFinder.getWorldLootTablesFolder(event.getServer()));
    	writeNewLootTables(event.getServer());
    }
    
	private void customTableOverrideWarnings(Path worldLootTables) 
	{
		for (LootTableTweakManager manager : LootTableTweakManager.getManagers()) 
		{
			for (ResourceLocation tableId : manager.getEditedTableIds()) 
			{
				Path customTable = LootTableFinder.DEFAULT.findCustomTable(worldLootTables, tableId);
				if (customTable != null)
					CraftTweakerAPI.logError(String.format("Cannot edit %s as it is overridden by %s", tableId, customTable.toAbsolutePath()));
			}
		}
	}

	private void writeNewLootTables(MinecraftServer server) 
	{
		Path worldLootTables = LootTableFinder.getWorldLootTablesFolder(server);
		LootTableDumper dumper = LootTableDumper.robust(worldLootTables.toFile());
		for (LootTableTweakManager manager : LootTableTweakManager.getManagers()) 
		{
			Iterator<MutableLootTable> newTables = manager.yieldNewTables();
			while (newTables.hasNext())
			{
				MutableLootTable mutableTable = newTables.next();
				dumper.dump(server, mutableTable.toImmutable(), mutableTable.getId());
			}
		}
	}

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        EventBusInspector.getListeners(MinecraftForge.EVENT_BUS)
            .filter(listener ->
            {
            	if (listener.owner == null)
            	{
            		LOGGER.error("Null owning mod container for listener {}", listener);
            		return false;
            	}
            	if (listener.owner.getModId() == null)
            	{
            		LOGGER.error("Null mod id for owning mod container {} of listener {}", listener.owner, listener);
            		return false;
            	}
                return listener.eventType == LootTableLoadEvent.class &&
                    listener.priority == EventPriority.LOWEST &&
                    !listener.owner.getModId().equals(DaomephstaLootShared.ID);
            })
            .peek(listener ->
            {
                CraftTweakerAPI.logInfo(String.format("Found listener for LootTableLoadEvent at lowest priority: %s", listener));
            })
            .map(listener -> listener.owner)
            .distinct()
            .forEach(mod ->
            {
                CraftTweakerAPI.logInfo(String.format("%1$s listens to LootTableLoadEvent at lowest priority. Any loot added by %1$s cannot be edited by {}.",
                		mod.getName(), getLoadedConsumerNames().collect(Collectors.joining(" or "))));
            });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTableLoad(LootTableLoadEvent event)
    {
		CTLoggingErrorHandler errorHandler = new CTLoggingErrorHandler();
        if (event.getTable().isFrozen())
        {
            LOGGER.debug("Skipped modifying loot table {} because it is frozen", event.getName());
            return;
        }
        // Avoid creating a mutable loot table unless it's necessary
        MutableLootTable mutable = null;
        for (LootTableTweakManager manager : LootTableTweakManager.getManagers())
        {
        	if (manager.getEditedTableIds().contains(event.getName()))
        	{
        		if (mutable == null) 
        		{
        	    	// Custom tables don't fire this event
        	        LootTable table = LootFixer.fixTable(event.getTable(), event.getName(), false);
					mutable = MutableLootTable.fromTable(table, event.getName(), errorHandler);
				} 
            	manager.applyEdits(mutable);
        	}
        }
        if (mutable != null)
        	event.setTable(mutable.toImmutable());
    }
}
