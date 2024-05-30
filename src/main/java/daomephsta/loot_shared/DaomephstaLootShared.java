package daomephsta.loot_shared;

import crafttweaker.mc1120.commands.CTChatCommand;
import daomephsta.loot_shared.command.CommandLootTables;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
    modid = DaomephstaLootShared.ID, name = DaomephstaLootShared.NAME, version = DaomephstaLootShared.VERSION,
    dependencies = "required-after:crafttweaker@[4.1.20,); before:jeresources; required:forge@[14.23.5.2779,);"
    )
public class DaomephstaLootShared
{
    public static final String NAME = "Daomephsta Loot Shared";
    public static final String ID = "daomephsta_loot_shared";
    public static final String VERSION = "@VERSION@";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        CTChatCommand.registerCommand(new CommandLootTables());
    }

    public static TextComponentTranslation translation(String keySuffix, Object... args)
    {
        return new TextComponentTranslation(ID + keySuffix, args);
    }
}
