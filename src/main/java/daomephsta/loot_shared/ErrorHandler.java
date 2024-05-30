package daomephsta.loot_shared;

import java.lang.reflect.Array;

import crafttweaker.CraftTweakerAPI;

public interface ErrorHandler
{
    public default void error(String format, Object... args)
    {
        error(String.format(format, args));
    }

    public void error(String message);

    public default void warn(String format, Object... args)
    {
        warn(String.format(format, args));
    }

    public void warn(String message);

    public default boolean nonNull(Object... args)
    {
        if (args.length % 2 != 0) throw new IllegalArgumentException("Expected list of name value pairs");
        boolean argsNonNull = true;
        for (int i = 0; i < args.length; i += 2)
        {
            String name = (String) args[i];
            Object value = args[i + 1];
            if (value == null)
            {
                error("%s cannot be null", name);
                argsNonNull = false;
            }
            else if (value.getClass().isArray())
            {
                for (int j = 0; j < Array.getLength(value); j++)
                {
                    if (Array.get(value, j) == null)
                    {
                        error("%s[%d] cannot be null", name, j);
                        argsNonNull = false;
                    }
                }
            }
        }
        return argsNonNull;
    }

    public static class CraftTweakerLog implements ErrorHandler
    {
        @Override
        public void error(String message)
        {
            CraftTweakerAPI.logError(addFileAndLine(message));
        }

        @Override
        public void warn(String message)
        {
            CraftTweakerAPI.logWarning(addFileAndLine(message));
        }

        private static String addFileAndLine(String message)
        {
            return CraftTweakerAPI.getScriptFileAndLine() + " > " + message;
        }
    }
}
