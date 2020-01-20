/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import developpeur2000.minecraft.minecraft_rw.world.Block;

/**
 * this class implement a block definition that is placed as an overlay
 *  that means it can use the previously set surface block,
 *  or at least keep it as part of an identified group before replacing it
 *  
 *  the class implements PhotoTreatedBlockDefinition
 *   for the case in which the base block is implementing PhotoTreatedBlockDefinition
 * 
 * @author David Frémont
 *
 */
public abstract class OverlaySimpleBlockDefinition extends SimpleBlockDefinition implements PhotoTreatedBlockDefinition {
	protected BlockDefinition baseBlockDefinition;

	public OverlaySimpleBlockDefinition(Block block, BlockDefinition baseBlockDefinition) {
		super(block);
		this.baseBlockDefinition = baseBlockDefinition;
	}
	
	public BlockDefinition getBaseBlockDefinition() {
		return baseBlockDefinition;
	}
	
	@Override
	public void applyPhotoColor(int x, int z, PhotoColor color) {
		if (baseBlockDefinition != null && PhotoTreatedBlockDefinition.class.isAssignableFrom(baseBlockDefinition.getClass())) {
			((PhotoTreatedBlockDefinition) baseBlockDefinition).applyPhotoColor(x, z, color);
		}
	}

	@Override
	public boolean isSameDefinition(int bufferX, int bufferY, int bufferSize, BlockDefinition other) {
		return other == this || (baseBlockDefinition != null && other == baseBlockDefinition);
	}

}
