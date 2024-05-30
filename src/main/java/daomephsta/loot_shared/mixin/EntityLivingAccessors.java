package daomephsta.loot_shared.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;


@Mixin(EntityLiving.class)
public interface EntityLivingAccessors
{
    @Invoker
    public ResourceLocation callGetLootTable();
}
