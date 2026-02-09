package daomephsta.loot_shared.utility.zenscript;

import crafttweaker.zenscript.GlobalRegistry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolPackage;

public class ZenClasses 
{
	public static String getName(Class<?> clazz)
	{
		ZenClass zenClass = clazz.getAnnotation(ZenClass.class);
        if (zenClass == null)
        	throw new IllegalArgumentException(clazz.getName() + " is not a ZenClass");
        
        if (zenClass.value().isEmpty())
        	return clazz.getSimpleName();
        return zenClass.value();
	}
    
	public static void registerAlias(String alias, String aliased)
    {
    	IZenSymbol aliasedSymbol = ZenSymbols.getSymbol(aliased, IZenSymbol.class);
    	int separator = alias.lastIndexOf('.');
    	String aliasPackageName = alias.substring(0, separator);
    	String aliasClassName = alias.substring(separator + 1);
    	ZenSymbols.getSymbol(aliasPackageName, SymbolPackage.class)
    		.put(aliasClassName, aliasedSymbol, GlobalRegistry.getErrors());
    }
}
