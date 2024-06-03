package daomephsta.loot_shared.utility.loot;

import static com.google.common.base.Predicates.not;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import daomephsta.loot_shared.compatibility.PlaceboCompatibility;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;


public class LootTableFinder
{
    public static final LootTableFinder DEFAULT = new LootTableFinder();
    private static final Logger LOGGER = LogManager.getLogger();
    private final Set<ResourceLocation> fileBacked = new HashSet<>();
    private boolean fullScanPerformed = false;

    public boolean exists(ResourceLocation tableId)
    {
        if (fileBacked.contains(tableId) ||
            LootTableList.getAll().contains(tableId) ||
            PlaceboCompatibility.tableExists(tableId))
            return true;

        //Cache
        String assetLocation = "assets/" + tableId.getNamespace() + "/loot_tables/" + tableId.getPath() + ".json";
        if (getClass().getClassLoader().getResource(assetLocation) != null)
        {
            add(tableId);
            return true;
        }
        else
            return false;
    }

    private boolean add(ResourceLocation tableId)
    {
        return fileBacked.add(tableId);
    }

    public boolean fullScanPerformed()
    {
        return fullScanPerformed;
    }

    public Iterable<ResourceLocation> findAll()
    {
        if (!fullScanPerformed)
        {
            LOGGER.info("Locating all existing loot tables");
            ClassLoader modClassLoader = Loader.instance().getModClassLoader();
            Set<Path> visitedSources = new HashSet<>();
            for (ModContainer mod : Loader.instance().getActiveModList())
            {
                //Skip mods with bogus JAR locations
                if (mod.getSource() == null)
                {
                    LOGGER.info("Skipped {} ({}) as it reported a null source", mod.getModId(), mod.getName());
                    continue;
                }
                if (!mod.getSource().exists())
                {
                    //Log at debug level instead of info when it's MCP or Minecraft, to avoid user confusion
                    Level level = mod.getModId().equals("minecraft") || mod.getModId().equals("mcp")
                        ? Level.DEBUG
                        : Level.INFO;
                    LOGGER.log(level, "Skipped {} ({}) as it reported a nonexistent source {}", mod.getModId(),
                        mod.getName(), mod.getSource().getAbsolutePath());
                    continue;
                }
                Path sourcePath = mod.getSource().toPath();
                //Skip already visited sources
                if (visitedSources.contains(sourcePath)) continue;
                LOGGER.debug("Visiting source of {} at {}", mod.getModId(), mod.getSource());
                try (Stream<ResourceLocation> idStream = getLootTableIdsFromJar(modClassLoader, sourcePath))
                {
					idStream.forEach(this::add);
                }
                visitedSources.add(sourcePath);
            }
            LOGGER.info("All existing loot tables located");
            fullScanPerformed = true;
        }
        // Cannot cache as mods may add to LootTableList at any time due to static init order
        return Stream.of(fileBacked, LootTableList.getAll(), PlaceboCompatibility.getAll())
            .reduce(Sets::union)
            .get(); // Optional cannot be empty as the stream is non-empty
    }

    private Stream<ResourceLocation> getLootTableIdsFromJar(ClassLoader modClassLoader, Path sourcePath)
    {
        try
        {
            if (Files.isDirectory(sourcePath))
            {
                return walkAssetsDirectory(FileSystems.getDefault());
            }
            FileSystem fs = FileSystems.newFileSystem(sourcePath, modClassLoader);
            return walkAssetsDirectory(fs).onClose(() ->
			{
				try
				{
					fs.close();
				}
				catch (IOException e)
				{
					LOGGER.error("Failed to close {} filesystem", sourcePath.toAbsolutePath(), e);
				}
			});
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to walk {}", sourcePath.toAbsolutePath(), e);
        	return Stream.empty();
        }
    }

	private Stream<ResourceLocation> walkAssetsDirectory(FileSystem fs) throws IOException
    {
        Path assetsDir = fs.getPath("assets");
        if (!Files.exists(assetsDir))
        	return Stream.empty();
    	return Files.walk(assetsDir, /*maxDepth*/ 1)
    		.filter(not(assetsDir::equals))
        	.map(domain -> domain.resolve("loot_tables"))
        	.filter(Files::exists)
        	.flatMap(this::walkLootTablesDirectory);
    }

    private Stream<ResourceLocation> walkLootTablesDirectory(Path lootTablesDir)
    {
        try
        {
        	return Files.walk(lootTablesDir)
            	.filter(lootTable ->
            	{
    	            //Just to be extra sure it's a loot table
    	            String extension = FilenameUtils.getExtension(lootTable.getFileName().toString());
					return !lootTablesDir.equals(lootTable) && extension.equals("json");
				})
            	.map(lootTable ->
    	        {
    	            String namespace = lootTablesDir.getName(1).toString();
    	            String path = FilenameUtils.removeExtension(lootTablesDir.relativize(lootTable).toString());
    	            return new ResourceLocation(namespace, path);
    	        });
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to walk {}", lootTablesDir.toAbsolutePath(), e);
        	return Stream.empty();
		}
    }
}
