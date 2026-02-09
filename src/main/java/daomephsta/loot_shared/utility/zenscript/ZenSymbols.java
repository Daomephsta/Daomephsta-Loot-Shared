package daomephsta.loot_shared.utility.zenscript;

import crafttweaker.zenscript.GlobalRegistry;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolPackage;

public class ZenSymbols 
{
	public static <T extends IZenSymbol> T getSymbol(String name, Class<T> tClass) 
	{
		Object symbol = null;
		for (String nameSegment : name.split("\\.")) 
    	{
    		if (symbol == null)
    			symbol = GlobalRegistry.getRoot().get(nameSegment);
    		else
    			symbol = ((SymbolPackage) symbol).get(nameSegment);
    		if ((symbol instanceof SymbolPackage) == false)
    			break;
		}
		if (symbol == null)
			throw new RuntimeException("No symbol found named " + name);
		return castSymbol(name, tClass, symbol);
	}

	private static <T extends IZenSymbol> T castSymbol(String name, Class<T> tClass, Object symbol) 
	{
		if (!tClass.isInstance(symbol))
		{
			throw new IllegalArgumentException(String.format("Expected %s to be %s, was %s", 
					name, tClass.getSimpleName(), symbol.getClass().getSimpleName()));
		}
		return tClass.cast(symbol);
	}
	
	public static SymbolPackage createPackage(String name)
	{
		SymbolPackage newPackage = (SymbolPackage) GlobalRegistry.getRoot();
		int start = 0;
		while (start < name.length())
		{
			int end = name.indexOf('.', start);
			if (end == -1)
				end = name.length();
			String prefix = name.substring(0, end);
			String simpleName = name.substring(start, end);
			IZenSymbol symbol = newPackage.get(simpleName);
			if (symbol != null)
				newPackage = castSymbol(prefix, SymbolPackage.class, symbol);
			else
			{
				SymbolPackage subPackage = new SymbolPackage(prefix);
				newPackage.put(simpleName, subPackage, GlobalRegistry.getErrors());
				newPackage = subPackage;
			}
			start = end + 1;
		}
        return newPackage;
	}
}
