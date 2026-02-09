package daomephsta.loot_shared;

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
import daomephsta.loot_shared.zenscript.api.factory.ZenLambdaLootCondition;
import daomephsta.loot_shared.zenscript.api.factory.ZenLambdaLootFunction;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;

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
                boolean whitelisted = listener.owner.getModId().equals("loottweaker") ||
                    listener.owner.getModId().equals("loot_carpenter");
                return !whitelisted && listener.eventType == LootTableLoadEvent.class &&
                    listener.priority == EventPriority.LOWEST;
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
}
