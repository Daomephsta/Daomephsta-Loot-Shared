package daomephsta.loot_shared.mixin;

import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import daomephsta.loot_shared.duck.LootOriginAwareContainer;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

@Mixin(EntityMinecartContainer.class)
public class EntityMinecartContainerMixin implements LootOriginAwareContainer
{
    @Unique
    private ResourceLocation daomephsta_loot_shared$lootOrigin;
    @Shadow
    private ResourceLocation lootTable;

    @Inject(method = "readEntityFromNBT", at = @At("HEAD"))
    private void daomephsta_loot_shared$readNbt(NBTTagCompound nbt, CallbackInfo info)
    {
        if (nbt.hasKey(LootOriginAwareContainer.NBT_KEY, NBT.TAG_STRING))
            this.daomephsta_loot_shared$lootOrigin = new ResourceLocation(nbt.getString(LootOriginAwareContainer.NBT_KEY));
    }

    @Inject(method = "writeEntityToNBT", at = @At("HEAD"))
    private void daomephsta_loot_shared$writeNbt(NBTTagCompound nbt, CallbackInfo info)
    {
        if (daomephsta_loot_shared$lootOrigin != null)
            nbt.setString(LootOriginAwareContainer.NBT_KEY, daomephsta_loot_shared$lootOrigin.toString());
    }

    @Inject(method = "addLoot", at = @At(value = "FIELD",
        target = "Lnet/minecraft/entity/item/EntityMinecartContainer;lootTable:Lnet/minecraft/util/ResourceLocation;", opcode = Opcodes.PUTFIELD))
    private void daomephsta_loot_shared$setGeneratedFrom(@Nullable EntityPlayer player, CallbackInfo info)
    {
        this.daomephsta_loot_shared$lootOrigin = this.lootTable;
    }

    @Override
    public ResourceLocation daomephsta_loot_shared$getLootOrigin()
    {
        return daomephsta_loot_shared$lootOrigin;
    }
}
