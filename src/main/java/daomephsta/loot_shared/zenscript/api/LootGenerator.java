package daomephsta.loot_shared.zenscript.api;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.damage.IDamageSource;
import crafttweaker.api.entity.IEntity;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IWorld;
import daomephsta.loot_shared.DaomephstaLootShared;
import daomephsta.loot_shared.ErrorHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass(DaomephstaLootShared.ZEN_PACKAGE + ".LootGenerator")
public class LootGenerator 
{
	private final WorldServer world;
    private final LootContext.Builder contextBuilder;
    private final ErrorHandler errorHandler;

	private LootGenerator(WorldServer world, ErrorHandler errorHandler) 
	{
		this.world = world;
		this.errorHandler = errorHandler;
		this.contextBuilder = new LootContext.Builder(world);
	}
	
	public static LootGenerator create(IWorld world, ErrorHandler errorHandler)
	{
		if (world.isRemote())
		{
			errorHandler.error("Client world provided, but a server world is required to generate loot");
			return null;
		}
		return new LootGenerator((WorldServer) CraftTweakerMC.getWorld(world), errorHandler);
	}
	
	@ZenMethod	
	public LootGenerator luck(float luck)
	{
		contextBuilder.withLuck(luck);
		return this;
	}

	@ZenMethod
	public LootGenerator lootedEntity(IEntity entity)
	{
		contextBuilder.withLootedEntity(CraftTweakerMC.getEntity(entity));
		return this;
	}

	@ZenMethod
	public LootGenerator player(IPlayer player)
	{
		contextBuilder.withPlayer(CraftTweakerMC.getPlayer(player));
		return this;
	}

	@ZenMethod
	public LootGenerator damageSource(IDamageSource damageSource)
	{
		contextBuilder.withDamageSource(CraftTweakerMC.getDamageSource(damageSource));
		return this;
	}

	@ZenMethod
	public IItemStack[] generate(String tableId)
	{
		LootTable table = getTable(tableId);
		if (table == null)
			return new IItemStack[0];
		return table.generateLootForPools(world.rand, contextBuilder.build()).stream()
				.map(CraftTweakerMC::getIItemStack)
				.toArray(IItemStack[]::new);
	}

	private static enum Overflow { VOID, DROP }
	
	private static enum Face 
	{
	    TOP(EnumFacing.UP),
	    BOTTOM(EnumFacing.DOWN),
	    NORTH(EnumFacing.NORTH),
	    EAST(EnumFacing.EAST),
	    SOUTH(EnumFacing.SOUTH),
	    WEST(EnumFacing.WEST),
	    NONE(null);
		
		private EnumFacing vanillaFacing;

		private Face(EnumFacing vanillaFacing) 
		{
			this.vanillaFacing = vanillaFacing;
		}
	}

	@ZenMethod
	public void generateInto(String tableId, IEntity iEntity, @Optional String face, @Optional String overflow)
	{
		
		LootTable table = getTable(tableId);
		if (table == null)
			return;
		Entity entity = CraftTweakerMC.getEntity(iEntity);
		generateInto(table, entity, entity.getPosition(), face, overflow);
	}
	
	@ZenMethod
	public void generateInto(String tableId, IBlockPos inventoryPos, @Optional String face, @Optional String overflow)
	{
		
		LootTable table = getTable(tableId);
		if (table == null)
			return;
		BlockPos pos = CraftTweakerMC.getBlockPos(inventoryPos);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity == null)
		{
			errorHandler.error("No tile entity found at " + pos);
			return;
		}
		generateInto(table, tileEntity, pos, face, overflow);
	}

	private void generateInto(LootTable table, ICapabilityProvider capabilityProvider, BlockPos pos, String face, String overflow) 
	{
		Face facingValue = getEnumValue(Face.class, face, Face.NONE);
		Overflow overflowValue = getEnumValue(Overflow.class, overflow, Overflow.VOID);
		if (facingValue == null || overflowValue == null)
			return;
		IItemHandler itemHandler = capabilityProvider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facingValue.vanillaFacing);
		if (itemHandler == null)
		{
			errorHandler.error(String.format("%s at %d, %d, %d has no inventory for face %s",
					capabilityProvider.getClass().getSimpleName(), pos.getX(), pos.getY(), pos.getZ(), face));
			return;
		}
		List<ItemStack> loot = table.generateLootForPools(world.rand, contextBuilder.build());
		for (ItemStack itemStack : loot)
		{
			for (int s = 0; s < itemHandler.getSlots(); s++)
			{
				ItemStack simulatedRemainder = itemHandler.insertItem(s, itemStack, true);
				if (simulatedRemainder.getCount() < itemStack.getCount())
					itemStack = itemHandler.insertItem(s, itemStack, false);
				if (itemStack.isEmpty())
					break;
			}
			// Overflow
			if (!itemStack.isEmpty())
			{
				switch (overflowValue) 
				{
				case VOID:
					itemStack.setCount(0);
					break;
				case DROP:
					if (!world.isRemote)
					{
						BlockPos dropPos = pos.up();
						EntityItem item = new EntityItem(world, dropPos.getX() + 0.5, dropPos.getY() + 0.5, dropPos.getZ(), itemStack);
						world.spawnEntity(item);
					}
					break;
				}
			}
		}
	}

	private <E extends Enum<E>> E getEnumValue(Class<E> enumClass, String name, E nullValue)
	{
		if (name == null)
			return nullValue;
		try 
		{
			return Enum.valueOf(enumClass, name.toUpperCase(Locale.ROOT));
		} 
		catch (IllegalArgumentException e) 
		{
			String validValues = Arrays.stream(enumClass.getEnumConstants())
				.map(c -> c.name())
				.collect(joining(", "));
			errorHandler.error(String.format("%s is an invalid value for %s. Valid: %s",
					name, enumClass.getSimpleName(), validValues));
			return null;
		}
	}
	
	private LootTable getTable(String tableId) 
	{
		LootTable table = world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(tableId));
		if (table == null)
		{
			errorHandler.error("No loot table with name %s exists!", tableId);
			return null;
		}
		return table;
	}
}
