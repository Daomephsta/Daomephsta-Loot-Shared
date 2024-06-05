package daomephsta.loot_shared.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import daomephsta.loot_shared.utility.loot.LootLoadingContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;

@Mixin(targets = "net.minecraft.world.storage.loot.LootTableManager$Loader")
public abstract class LootTableManagerLoaderMixin
{
    @Inject(method = "load", at = @At("HEAD"))
    private void daomephsta_loot_shared$pushContext(ResourceLocation id, CallbackInfoReturnable<LootTable> info)
    {
        LootLoadingContext.push(id);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void daomephsta_loot_shared$popContext(ResourceLocation id, CallbackInfoReturnable<LootTable> info)
    {
        LootLoadingContext.pop();
    }
}
