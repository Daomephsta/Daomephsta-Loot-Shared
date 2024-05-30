package daomephsta.loot_shared.utility;

import daomephsta.loot_shared.ErrorHandler;
import net.minecraft.world.storage.loot.RandomValueRange;


public class RandomValueRanges
{
    public static RandomValueRange checked(ErrorHandler errorHandler, float min, float max)
    {
        if (min > max) errorHandler.error("Minimum (%f) must be less than or equal to maximum (%f)", min, max);
        return new RandomValueRange(min, max);
    }
}
