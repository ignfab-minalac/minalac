/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * this class provides a factory design pattern to create unique IdentifiedBlockDefinition instances
 * given their type and id
 * 
 * @author David Frémont
 * @see IdentifiedBlockFactory
 *
 */
public final class IdentifiedBlockFactory {
	
	final static private HashMap<String, HashMap<Integer, IdentifiedBlockDefinition>> INSTANCES
		= new HashMap<String, HashMap<Integer, IdentifiedBlockDefinition>>();
	
	public static IdentifiedBlockDefinition getBlockDefinition(String blockDefinitionClassName, int id) {
		return getBlockDefinition(blockDefinitionClassName, id, null);
	}
	public static IdentifiedBlockDefinition getBlockDefinition(String blockDefinitionClassName, int id,
																Object[] constructorArgs) {
		String fullClassName = "ign.minecraft.definition."+blockDefinitionClassName;
		try {
			assert (IdentifiedBlockDefinition.class.isAssignableFrom( Class.forName(fullClassName) ));
		} catch (ClassNotFoundException e) {
			assert false;//should also not happen
		}
		
		HashMap<Integer, IdentifiedBlockDefinition> oneTypeInstances;
		IdentifiedBlockDefinition instance = null;
		if(!INSTANCES.containsKey(blockDefinitionClassName)) {
			//first instance of this type
			oneTypeInstances = new HashMap<Integer, IdentifiedBlockDefinition>();
			INSTANCES.put(blockDefinitionClassName, oneTypeInstances);
		} else {
			oneTypeInstances = INSTANCES.get(blockDefinitionClassName);
		}

		if(!oneTypeInstances.containsKey(id)) {
			//first instance of this id in this type
			try {
				Object[] args = new Object[((constructorArgs != null) ? constructorArgs.length : 0 ) + 1];
				args[0] = id;
				if (constructorArgs != null) {
					System.arraycopy(constructorArgs, 0, args, 1, constructorArgs.length);
				}
				Class<?>[] argTypes = new Class<?>[args.length];
				for (int i = 0; i < args.length; i++) {
					argTypes[i] = args[i].getClass();
				}
				instance = (IdentifiedBlockDefinition) Class.forName(fullClassName).getConstructor(argTypes).newInstance(args);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
					| IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				//TODO: remove this temporary trace used for debug purposes
				e.printStackTrace();
				assert false;//should not happen
			}
			if (instance != null) {
				oneTypeInstances.put(id, instance);
			}
		} else {
			instance = oneTypeInstances.get(id);
		}

		
		return instance;
	}
	
	private IdentifiedBlockFactory() {
		//this class is not supposed to be instantiated
		assert false;
	}
}
