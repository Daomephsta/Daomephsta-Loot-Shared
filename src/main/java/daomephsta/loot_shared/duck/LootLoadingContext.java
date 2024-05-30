package daomephsta.loot_shared.duck;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.util.ResourceLocation;

public class LootLoadingContext
{
    private static final ThreadLocal<Deque<LootLoadingContext>> LootLoadingContexts = ThreadLocal.withInitial(ArrayDeque::new);
    public ResourceLocation tableId;
    private int poolDiscriminator = 0,
                entryDiscriminator = 0;

    public static LootLoadingContext get()
    {
        return LootLoadingContexts.get().peek();
    }

    public static LootLoadingContext push()
    {
        LootLoadingContext LootLoadingContext = new LootLoadingContext();
        LootLoadingContexts.get().push(LootLoadingContext);
        return LootLoadingContext;
    }

    public static LootLoadingContext pop()
    {
        return LootLoadingContexts.get().pop();
    }

    public int getPoolDiscriminator()
    {
        return poolDiscriminator++;
    }

    public int getEntryDiscriminator()
    {
        return entryDiscriminator++;
    }
}
