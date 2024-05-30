package daomephsta.loot_shared;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.NetworkModHolder;
import net.minecraftforge.fml.common.network.internal.NetworkModHolder.NetworkChecker;
import net.minecraftforge.fml.relauncher.Side;

public abstract class CustomNetworkChecker extends NetworkChecker 
{
    private static final Logger LOGGER = LogManager.getLogger(DaomephstaLootShared.NAME);
    
    protected CustomNetworkChecker(NetworkModHolder parent)
    {
        parent.super();
    }

	public static void install(Function<NetworkModHolder, CustomNetworkChecker> factory, String modId) {
	    ModContainer modContainer = Loader.instance().getIndexedModList().get(DaomephstaLootShared.ID);
	    try
	    {
	        NetworkModHolder holder = getHolderRegistry().get(modContainer);
	        Field checkerField = NetworkModHolder.class.getDeclaredField("checker");
	        checkerField.setAccessible(true);
	        checkerField.set(holder, factory.apply(holder));
	    }
	    catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
	    {
	        throw new RuntimeException("Failed to set network checker", e);
	    }
	    LOGGER.info("Successfully installed custom network checker for {}", modId);
	}

	@Override
	public boolean check(Map<String, String> modVersions, Side remoteSide)
	{
	    return checkCompatible(modVersions, remoteSide) == null;
	}

	@SuppressWarnings("unchecked")
	private static Map<ModContainer, NetworkModHolder> getHolderRegistry()
	{
	    try
	    {
	        Field registryField = NetworkRegistry.class.getDeclaredField("registry");
	        registryField.setAccessible(true);
	        return (Map<ModContainer, NetworkModHolder>) registryField.get(NetworkRegistry.INSTANCE);
	    }
	    catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
	    {
	        throw new RuntimeException("Failed to get holder registry from network registry", e);
	    }
	}
}