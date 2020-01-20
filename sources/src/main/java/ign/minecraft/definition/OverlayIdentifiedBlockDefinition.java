/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

/**
 * this class implement an identified block definition that is placed as an overlay
 *  that means it can use the previously set surface block, or get rid of it
 *  
 *  the class implements PhotoTreatedBlockDefinition
 *   for the case in which the base block is implementing PhotoTreatedBlockDefinition
 * 
 * @author David Frémont
 * @see IdentifiedBlockDefinition
 *
 */
public abstract class OverlayIdentifiedBlockDefinition extends IdentifiedBlockDefinition implements PhotoTreatedBlockDefinition {
	//static common storage of base blocks
	// to avoid having one full array per instance although there is no overlap
	protected static BlockDefinition[] baseBlocks;
	
	public static void setMapSize(int mapSize) {
		baseBlocks = new BlockDefinition[mapSize * mapSize];
	}

	
	public OverlayIdentifiedBlockDefinition(Integer id) {
		super(id);
	}

	public void setBaseBlock(int x, int z, BlockDefinition blockDef) {
		baseBlocks[ xz1D(x,z) ] = blockDef;
	}
	
	@Override
	protected void preRender(int x, int z) {
		if (baseBlocks[xz1D(x,z)] != null
				&& IdentifiedBlockDefinition.class.isAssignableFrom(baseBlocks[xz1D(x,z)].getClass())) {
			IdentifiedBlockDefinition baseBlock = (IdentifiedBlockDefinition) baseBlocks[xz1D(x,z)];
			//init the baseblock if needed
			if (!baseBlock.preRenderInitialized) {
				baseBlock.preRenderInit();
				baseBlock.preRenderInitialized = true;
			}
			baseBlock.preRender(x, z);
		}
	}
	@Override
	protected void preRenderEnd() {
		for (int index = 0; index < baseBlocks.length ; index ++) {
			if (baseBlocks[index] != null
					&& IdentifiedBlockDefinition.class.isAssignableFrom(baseBlocks[index].getClass())) {
				IdentifiedBlockDefinition baseBlock = (IdentifiedBlockDefinition) baseBlocks[index];
				if (!baseBlock.preRenderEnded) {
					baseBlock.preRenderEnd();
					baseBlock.preRenderEnded = true;
				}
			}
		}
	}


	@Override
	public void applyPhotoColor(int x, int z, PhotoColor color) {
		if (PhotoTreatedBlockDefinition.class.isAssignableFrom(baseBlocks[ xz1D(x,z) ].getClass())) {
			((PhotoTreatedBlockDefinition) baseBlocks[ xz1D(x,z) ]).applyPhotoColor(x, z, color);
		}
	}

	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		return baseBlocks[xz1D(bufferX,bufferY)].canBeReplaced(ImporterName, bufferX, bufferY, bufferSize);
	}

	@Override
	public boolean canPutOverlayLayer(int bufferX, int bufferY, int bufferSize) {
		return baseBlocks[xz1D(bufferX,bufferY)].canPutOverlayLayer(bufferX, bufferY, bufferSize);
	}
	@Override
	public boolean canPutOverlayBlock(int bufferX, int bufferY, int bufferSize) {
		return baseBlocks[xz1D(bufferX,bufferY)].canPutOverlayBlock(bufferX, bufferY, bufferSize);
	}
	
	@Override
	public boolean isSameDefinition(int bufferX, int bufferY, int bufferSize, BlockDefinition other) {
		return other == this || other == baseBlocks[xz1D(bufferX,bufferY)];
	}

}
