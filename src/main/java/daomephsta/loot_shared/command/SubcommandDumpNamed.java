package daomephsta.loot_shared.command;

import java.io.File;
import java.util.List;

import daomephsta.loot_shared.DaomephstaLootShared;
import daomephsta.loot_shared.utility.Texts;
import daomephsta.loot_shared.utility.loot.LootTableFinder;
import daomephsta.loot_shared.utility.loot.dump.LootTableDumper;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;


public class SubcommandDumpNamed implements Subcommand
{
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length < 2)
        {
            sender.sendMessage(DaomephstaLootShared.translation(".commands.missingName"));
            return;
        }
        ResourceLocation tableId = new ResourceLocation(args[1]);
        if (!LootTableFinder.DEFAULT.exists(tableId))
        {
            sender.sendMessage(DaomephstaLootShared.translation(".commands.invalidName"));
            return;
        }
        File dump = LootTableDumper.DEFAULT.dump(sender.getEntityWorld(), tableId);
        if (!server.isDedicatedServer()) linkDumpFileInChat(sender, dump, tableId);
    }

    private static void linkDumpFileInChat(ICommandSender sender, File dump, ResourceLocation tableId)
    {
        sender.sendMessage(DaomephstaLootShared.translation(".commands.dump.dumpLink",
            Texts.styledAsString(tableId, style -> style.setUnderlined(true)), Texts.fileLink(dump)));
    }

    @Override
    public List<String> getCompletions(MinecraftServer server, ICommandSender sender, String[] args,
        BlockPos targetPos)
    {
        return Subcommand.suggestTableIds(args[0]);
    }
}
