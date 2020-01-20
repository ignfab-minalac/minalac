/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Map;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;

public class FieldBlockDefinition extends PhotoTreatedIdentifiedBlockDefinition {
	public static final Block FIELDBASE = new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6);
	
	public enum FieldType {
    	CEREAL(new BlockDefinition[][] {
    			new BlockDefinition[] { FieldBlocks.CEREAL, FieldBlocks.SOIL }
    		}),
    	YELLOWFLOWER(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.YELLOWFLOWER, FieldBlocks.SOIL }
			}),
    	SUNFLOWER(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.SUNFLOWER, FieldBlocks.SOIL }
			}),
    	PLANTS(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.PLANTS, FieldBlocks.SOIL }
			}),
    	SEEDS(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.SEEDS, FieldBlocks.SOIL }
			}),
    	SURFACE_VEGETABLES(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.SURFACE_VEG, FieldBlocks.SOIL }
			}),
    	GROUND_VEGETABLES(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.GROUND_VEG, FieldBlocks.SOIL }
			}),
    	ORCHARD(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.ORCHARDTREE, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
				new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
				new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
				new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL }
			}),
    	VINEYARD(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.VINEYARDLEAF, FieldBlocks.SOIL },
				new BlockDefinition[] { FieldBlocks.VINEYARDPLANT, FieldBlocks.SOIL }
			}),
    	NUTS(new BlockDefinition[][] {
			new BlockDefinition[] { FieldBlocks.NUTSTREE, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL }
			}),
    	OLIVES(new BlockDefinition[][] {
			new BlockDefinition[] { FieldBlocks.OLIVESTREE, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL }
			}),
    	REEDS(new BlockDefinition[][] {
				new BlockDefinition[] { FieldBlocks.REEDS, FieldBlocks.SOIL }
			}),
    	TREES(new BlockDefinition[][] {
			new BlockDefinition[] { FieldBlocks.CULTIVATEDTREE, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL },
			new BlockDefinition[] { FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL, FieldBlocks.SOIL }
			}),
    	MEADOW(new BlockDefinition[][] {
				new BlockDefinition[] { SimpleBlocks.MEADOWZONE.get() }
			})
    	;
		
		private BlockDefinition[][] pattern;
		
		private FieldType(BlockDefinition[][] pattern) {
			this.pattern = pattern;
		}
	}
	
	enum PatternOrientation {
		NORMAL,
		ROT90,
		ROT180,
		ROT270
	}
	
	final private FieldType fieldType;
	final private PatternOrientation orientation = PatternOrientation.values()[ (int) Math.floor( Math.random() * PatternOrientation.values().length ) ];
	
	public FieldBlockDefinition(Integer id, FieldType fieldType) {
		super(id);
		
		this.fieldType = fieldType;
	}
	
	private int[] getCoordInPattern(int bufferX, int bufferY) {
		int[] xyValues = new int[2];

		assert fieldType.pattern.length > 0;
		assert fieldType.pattern[0].length > 0;
		xyValues[0] = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferX % fieldType.pattern[0].length
				: bufferY % fieldType.pattern[0].length;
		xyValues[1] = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferY % fieldType.pattern.length
				: bufferX % fieldType.pattern.length;
		assert fieldType.pattern[ xyValues[1] ].length == fieldType.pattern[0].length;
		
		return xyValues;
	}

	//no specific pre-render
	
	@Override
	protected void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		int[] xyInPattern = getCoordInPattern(bufferX, bufferY);
		
		if (PhotoTreatedSimpleBlockDefinition.class.isAssignableFrom( fieldType.pattern[xyInPattern[1]][xyInPattern[0]].getClass() )) {
			PhotoTreatedSimpleBlockDefinition blockDefinition = 
					((PhotoTreatedSimpleBlockDefinition) fieldType.pattern[xyInPattern[1]][xyInPattern[0]]).clone();
			blockDefinition.applyPhotoColor(bufferX, bufferY, photoColors[xz1D(bufferX, bufferY)]);
			blockDefinition.render(world, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
		} else {
			fieldType.pattern[xyInPattern[1]][xyInPattern[0]].render(world, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
		}
	}
	
	@Override
	protected void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY,
			MineMap.MapItemColors mapItemColors) {
		int[] xyInPattern = getCoordInPattern(bufferX, bufferY);
		
		if (PhotoTreatedSimpleBlockDefinition.class.isAssignableFrom( fieldType.pattern[xyInPattern[1]][xyInPattern[0]].getClass() )) {
			PhotoTreatedSimpleBlockDefinition blockDefinition = 
					((PhotoTreatedSimpleBlockDefinition) fieldType.pattern[xyInPattern[1]][xyInPattern[0]]).clone();
			blockDefinition.applyPhotoColor(bufferX, bufferY, photoColors[xz1D(bufferX, bufferY)]);
			blockDefinition.render(blockList, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
		} else {
			fieldType.pattern[xyInPattern[1]][xyInPattern[0]].render(blockList, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
		}
	}
	
	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		switch (ImporterName) {
		case "BuildingsImporter":
		case "HydroLinesImporter":
		case "HydroSurfacesImporter":
		case "LinearConstructionsImporter":
		case "RoadsImporter":
			return true;
		default:
			switch (fieldType) {
				case MEADOW:
					return true;
				default:
					return false;
			}
		}
	}
	@Override
	public boolean canPutOverlayLayer(int bufferX, int bufferY, int bufferSize) {
		switch (fieldType) {
		case MEADOW:
			return true;
		default:
			return false;
		}
	}
}
