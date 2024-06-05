package daomephsta.loot_shared.utility.loot;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import net.minecraft.util.ResourceLocation;

public class LootLoadingContext
{
	private static final ThreadLocal<Deque<LootLoadingContext>> contexts = ThreadLocal.withInitial(ArrayDeque::new);
    public final ResourceLocation tableId;
    public final LootNameFixer nameFixer = new LootNameFixer(this);

    private LootLoadingContext(ResourceLocation tableId)
    {
		this.tableId = tableId;
	}

	public static boolean available()
    {
        return !contexts.get().isEmpty();
    }

    public static void ifAvailableOrElse(Consumer<LootLoadingContext> ifAvailable, Runnable orElse)
    {
        if (!contexts.get().isEmpty())
        	ifAvailable.accept(contexts.get().peek());
        else
        	orElse.run();
    }

    public static LootLoadingContext push(ResourceLocation id)
    {
        LootLoadingContext context = new LootLoadingContext(id);
        contexts.get().push(context);
        return context;
    }

    public static LootLoadingContext pop()
    {
        return contexts.get().pop();
    }
}
