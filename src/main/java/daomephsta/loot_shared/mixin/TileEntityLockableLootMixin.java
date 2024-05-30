package daomephsta.loot_shared.mixin;

import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import daomephsta.loot_shared.duck.LootOriginAwareContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

@Mixin(TileEntityLockableLoot.class)
public class TileEntityLockableLootMixin implements LootOriginAwareContainer
{
    @Unique
    private ResourceLocation loot_carpenter$lootOrigin;
    @Shadow
    protected ResourceLocation lootTable;

    @Inject(method = "checkLootAndRead", at = @At("HEAD"))
    private void loot_carpenter$readNbt(NBTTagCompound nbt, CallbackInfoReturnable<Boolean> info)
    {
        if (nbt.hasKey(LootOriginAwareContainer.NBT_KEY, NBT.TAG_STRING))
            this.loot_carpenter$lootOrigin = new ResourceLocation(nbt.getString(LootOriginAwareContainer.NBT_KEY));
    }

    @Inject(method = "checkLootAndWrite", at = @At("HEAD"))
    private void loot_carpenter$writeNbt(NBTTagCompound nbt, CallbackInfoReturnable<Boolean> info)
    {
        if (loot_carpenter$lootOrigin != null)
            nbt.setString(LootOriginAwareContainer.NBT_KEY, loot_carpenter$lootOrigin.toString());
    }

    @Inject(method = "fillWithLoot", at = @At(value = "FIELD",
        target = "Lnet/minecraft/tileentity/TileEntityLockableLoot;lootTable:Lnet/minecraft/util/ResourceLocation;", opcode = Opcodes.PUTFIELD))
    private void loot_carpenter$setGeneratedFrom(@Nullable EntityPlayer player, CallbackInfo info)
    {
        this.loot_carpenter$lootOrigin = this.lootTable;
    }

    @Override
    public ResourceLocation loot_carpenter$getLootOrigin()
    {
        return loot_carpenter$lootOrigin;
    }
}
