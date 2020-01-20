/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.ArrayList;
import java.util.Map;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MinecraftMap;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

/**
 * 
 * the following class offers methods to generate tree in the map
 *  
 */
public class TreeBlockDefinition extends BlockDefinition {
	
	enum TreeType {
		OAK,
		BIRCH,
		ACACIA,
		SPRUCE,
		PINE,
		ORCHARD,
		OLIVE,
		NUTS,
		CULTIVATED
	}
	
	enum Section {
		ONE,
		FIVE,//1-3-1 star
		SIX_TO_EIGHT,//1-3-1 star + some random corners
		TWENTYONE,//3-5-5-5-3 star
		TWENTYONE_TO_TWENTYFIVE,//3-5-5-5-3 star + some random corners
		THIRTYSEVEN_TO_FOURTYONE,//3-5-7-7-7-5-3 round + some random corners
	}
	enum TreeTypeSections {
		OAK(new Section[] { Section.FIVE, Section.SIX_TO_EIGHT, Section.TWENTYONE_TO_TWENTYFIVE, Section.TWENTYONE_TO_TWENTYFIVE }),
		OAKBRANCH(new Section[] { Section.SIX_TO_EIGHT, Section.TWENTYONE_TO_TWENTYFIVE, Section.TWENTYONE_TO_TWENTYFIVE, Section.SIX_TO_EIGHT }),
		BIRCH(new Section[] { Section.FIVE, Section.SIX_TO_EIGHT, Section.TWENTYONE_TO_TWENTYFIVE, Section.TWENTYONE_TO_TWENTYFIVE }),
		ACACIA(new Section[] { Section.SIX_TO_EIGHT, Section.TWENTYONE_TO_TWENTYFIVE }),
		SPRUCE_CLASSIC(new Section[] { Section.FIVE, Section.ONE, Section.FIVE, Section.TWENTYONE, Section.FIVE, Section.TWENTYONE, Section.FIVE, Section.TWENTYONE }),
		SPRUCE_WIDESHORT(new Section[] { Section.FIVE, Section.ONE, Section.FIVE, Section.TWENTYONE, Section.THIRTYSEVEN_TO_FOURTYONE, Section.FIVE }),
		SPRUCE_WIDETALL(new Section[] { Section.FIVE, Section.ONE, Section.FIVE, Section.TWENTYONE, Section.THIRTYSEVEN_TO_FOURTYONE, Section.TWENTYONE, Section.THIRTYSEVEN_TO_FOURTYONE, Section.FIVE }),
		PINE(new Section[] { Section.FIVE, Section.TWENTYONE_TO_TWENTYFIVE, Section.TWENTYONE }),
		WIDEPINE(new Section[] { Section.FIVE, Section.TWENTYONE_TO_TWENTYFIVE, Section.THIRTYSEVEN_TO_FOURTYONE, Section.TWENTYONE }),
		ROUNDPINE(new Section[] { Section.FIVE, Section.TWENTYONE_TO_TWENTYFIVE, Section.TWENTYONE_TO_TWENTYFIVE, Section.TWENTYONE }),
		ORCHARD(new Section[] { Section.ONE, Section.SIX_TO_EIGHT, Section.FIVE }),
		OLIVE(new Section[] { Section.ONE, Section.SIX_TO_EIGHT, Section.SIX_TO_EIGHT }),
		NUTS(new Section[] { Section.FIVE, Section.SIX_TO_EIGHT, Section.TWENTYONE, Section.THIRTYSEVEN_TO_FOURTYONE }),
		CULTIVATED(new Section[] { Section.ONE, Section.FIVE, Section.ONE }),
		;
		
		final Section[] sections;
		
		TreeTypeSections(Section[] sections) {
			this.sections = sections;
		}
	}
	enum BranchDirection {
		NONE(new int[0], false),//indicates the trunk

		SHORT_N(new int[] {0,-1}, true),
		SHORT_NE(new int[] {1,-1}, true),
		SHORT_E(new int[] {1,0}, false),
		SHORT_SE(new int[] {1,1}, false),
		SHORT_S(new int[] {0,1}, true),
		SHORT_SW(new int[] {-1,1}, true),
		SHORT_W(new int[] {-1,0}, false),
		SHORT_NW(new int[] {-1,-1}, false),
		LONG_N(new int[] {0,-1, 0,-2}, true),
		LONG_NNE(new int[] {0,-1, 1,-2}, true),
		LONG_NE(new int[] {1,-1, 2,-2}, true),
		LONG_ENE(new int[] {1,0, 2,-1}, false),
		LONG_E(new int[] {1,0, 2,0}, false),
		LONG_ESE(new int[] {1,0, 2,1}, false),
		LONG_SE(new int[] {1,1, 2,2}, false),
		LONG_SSE(new int[] {0,1, 1,2}, true),
		LONG_S(new int[] {0,1, 0,2}, true),
		LONG_SSW(new int[] {0,1, -1,2}, true),
		LONG_SW(new int[] {-1,1, -2,2}, true),
		LONG_WSW(new int[] {-1,0, -2,1}, false),
		LONG_W(new int[] {-1,0, -2,0}, false),
		LONG_WNW(new int[] {-1,0, -2,-1}, false),
		LONG_NW(new int[] {-1,-1, -2,-2}, false),
		LONG_NNW(new int[] {0,-1, -1,-2}, true),
		
		SHORT_UP_N(SHORT_N, true),
		SHORT_UP_NE(SHORT_NE, true),
		SHORT_UP_E(SHORT_E, true),
		SHORT_UP_SE(SHORT_SE, true),
		SHORT_UP_S(SHORT_S, true),
		SHORT_UP_SW(SHORT_SW, true),
		SHORT_UP_W(SHORT_W, true),
		SHORT_UP_NW(SHORT_NW, true),
		LONG_UP_N(LONG_N, true),
		LONG_UP_NNE(LONG_NNE, true),
		LONG_UP_NE(LONG_NE, true),
		LONG_UP_ENE(LONG_ENE, true),
		LONG_UP_E(LONG_E, true),
		LONG_UP_ESE(LONG_ESE, true),
		LONG_UP_SE(LONG_SE, true),
		LONG_UP_SSE(LONG_SSE, true),
		LONG_UP_S(LONG_S, true),
		LONG_UP_SSW(LONG_SSW, true),
		LONG_UP_SW(LONG_SW, true),
		LONG_UP_WSW(LONG_WSW, true),
		LONG_UP_W(LONG_W, true),
		LONG_UP_WNW(LONG_WNW, true),
		LONG_UP_NW(LONG_NW, true),
		LONG_UP_NNW(LONG_WNW, true),

		SHORT_DOWN_N(SHORT_N, false),
		SHORT_DOWN_NE(SHORT_NE, false),
		SHORT_DOWN_E(SHORT_E, false),
		SHORT_DOWN_SE(SHORT_SE, false),
		SHORT_DOWN_S(SHORT_S, false),
		SHORT_DOWN_SW(SHORT_SW, false),
		SHORT_DOWN_W(SHORT_W, false),
		SHORT_DOWN_NW(SHORT_NW, false),
		LONG_DOWN_N(LONG_N, false),
		LONG_DOWN_NNE(LONG_NNE, false),
		LONG_DOWN_NE(LONG_NE, false),
		LONG_DOWN_ENE(LONG_ENE, false),
		LONG_DOWN_E(LONG_E, false),
		LONG_DOWN_ESE(LONG_ESE, false),
		LONG_DOWN_SE(LONG_SE, false),
		LONG_DOWN_SSE(LONG_SSE, false),
		LONG_DOWN_S(LONG_S, false),
		LONG_DOWN_SSW(LONG_SSW, false),
		LONG_DOWN_SW(LONG_SW, false),
		LONG_DOWN_WSW(LONG_WSW, false),
		LONG_DOWN_W(LONG_W, false),
		LONG_DOWN_WNW(LONG_WNW, false),
		LONG_DOWN_NW(LONG_NW, false),
		LONG_DOWN_NNW(LONG_WNW, false),
		;
		
		final int[] xzShifts;//stored as x,z,x,z,...
		final int[] yShifts;
		final boolean isNS;
		
		BranchDirection(int[] shifts, boolean ns) {
			assert (shifts.length % 2) == 0;
			this.xzShifts = shifts;
			isNS = ns;
			yShifts = new int[shifts.length / 2];//no y shift
		}
		BranchDirection(BranchDirection copyFrom, boolean up) {
			xzShifts = copyFrom.xzShifts;
			isNS = copyFrom.isNS;
			yShifts = new int[xzShifts.length / 2];
			yShifts[xzShifts.length / 2 - 1] = up ? 1 : -1;
		}
		
	}
	
	private class Branch {
		int length;//height of trunk or length of branch
		BranchDirection direction;//for branches only
		Section[] sections;//description of sections of leaves, from top to bottom
		int sectionShift;//indicate the difference betweem top section and the last log
		//last (upper) log coordinates
		int x;
		int y;
		int z;
		//blocks to be used
		Block logBlock;
		Block leavesBlock;
		
		public Branch(int length, BranchDirection direction, Section[] sections, int sectionShift,
						int x, int y, int z, Block logBlock, Block leavesBlock) {
			this.length = length;
			this.direction = direction;
			this.sections = sections;
			this.sectionShift = sectionShift;
			this.x = x;
			this.y = y;
			this.z = z;
			this.logBlock = logBlock;
			this.leavesBlock = leavesBlock;
		}
	}
	
	private final TreeType type;
	private final boolean forceStandard;
	
	public TreeBlockDefinition(TreeType type) {
		this(type, false);
	}
	public TreeBlockDefinition(TreeType type, boolean forceStandard) {
		this.type = type;
		this.forceStandard = forceStandard;
	}
	
	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MinecraftMap.MapItemColors mapItemColors) {
		int treeHeight;//total height including top leaves
		final ArrayList<Branch> branches = new ArrayList<Branch>();
		Section[] sections;//used if sections definition is not fed directly to the branch creation function
		
		switch(type) {
		case OAK:
			if (forceStandard) {
				treeHeight = 6 + (int) Math.floor(Math.random() * 2);//6 or 7
			} else if (Math.random() < 0.8) { //80% of small oak trees
				treeHeight = 5 + (int) Math.floor(Math.random() * 3);//5 to 7
			} else {
				treeHeight = 5 + (int) Math.floor(Math.random() * 8);//5 to 12
			}
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.OAK.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.OAK | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY) )
							) );
			if(treeHeight > 7) {
				//the tree have branches
				BranchDirection direction;
				for(int branchStartY = treeHeight / 2; branchStartY < treeHeight-3; branchStartY++ ) {
					//random branch direction
					direction = BranchDirection.values()[ 1 + (int) (Math.random() * (BranchDirection.values().length - 1)) ];
					//create branch
					branches.add( new Branch(0, direction,
							TreeTypeSections.OAKBRANCH.sections,
							2, x + direction.xzShifts[direction.xzShifts.length - 2], y + branchStartY + direction.yShifts[direction.yShifts.length - 1], z + direction.xzShifts[direction.xzShifts.length - 1],
							new Block( BlockType.LOG, (byte) (BlockData.LOG.OAK | (direction.isNS ? BlockData.LOG.ORIENTATION_NS : BlockData.LOG.ORIENTATION_EW )) ),
							new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY) )
						) );
				}
			}
			break;
		case BIRCH:
			treeHeight = 5 + (int) Math.floor(Math.random() * 3);//5 to 7
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.BIRCH.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.BIRCH | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.BIRCH | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case ACACIA:
			treeHeight = 6 + (int) Math.floor(Math.random() * 3);//6 to 8
			branches.add( new Branch(treeHeight, BranchDirection.NONE,
								TreeTypeSections.ACACIA.sections,
								1, x, y + treeHeight - 1, z,
								new Block( BlockType.LOG2, (byte) (BlockData.LOG2.ACACIA | BlockData.LOG2.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES2, (byte) (BlockData.LEAVES2.ACACIA | BlockData.LEAVES2.NO_DECAY) )
							) );
			break;
		case PINE:
			treeHeight = 6 + (int) Math.floor(Math.random() * 4);//6 to 9
			sections = TreeTypeSections.PINE.sections;
			if (treeHeight >= 8 && Math.random() < 0.5) {
				sections = TreeTypeSections.WIDEPINE.sections;
			}
			if (treeHeight == 6 && Math.random() < 0.5) {
				sections = TreeTypeSections.ROUNDPINE.sections;
			}
			branches.add( new Branch(treeHeight, BranchDirection.NONE,
								sections,
								1, x, y + treeHeight - 1, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG2.DARKOAK | BlockData.LOG2.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.SPRUCE | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case SPRUCE:
			if (Math.random() < 0.9) { //90% of normal trees, rest might be bigger
				treeHeight = 7 + (int) Math.floor(Math.random() * 3);//7 to 9
			} else {
				treeHeight = 7 + (int) Math.floor(Math.random() * 5);//7 to 11
			}
			//build sections (leave 2 apparent trunk blocks at the end)
			sections = new Section[treeHeight-2];
			int sectionIndex = 0;
			//might add onr or two top single leaves block
			if ((treeHeight == 11) || (Math.random() < 0.5)) {
				sections[sectionIndex] = Section.ONE;
				sectionIndex ++;
				if ((Math.random() < 0.5)) {
					sections[sectionIndex] = Section.ONE;
					sectionIndex ++;
				}
			}
			TreeTypeSections baseSections;
			if (Math.random() < 0.8) {
				baseSections = TreeTypeSections.SPRUCE_CLASSIC;
			} else {
				if ((sections.length - sectionIndex) > 6) {
					baseSections = TreeTypeSections.SPRUCE_WIDETALL;
				} else {
					baseSections = TreeTypeSections.SPRUCE_WIDESHORT;
				}
			}
			System.arraycopy(baseSections.sections, 0, sections, sectionIndex, sections.length - sectionIndex);

			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.SPRUCE | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.SPRUCE | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case ORCHARD:
			treeHeight = 6;
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.ORCHARD.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.OAK | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case OLIVE:
			treeHeight = 5;
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.OLIVE.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG2, (byte) (BlockData.LOG2.DARKOAK | BlockData.LOG2.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.JUNGLE | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case NUTS:
			treeHeight = 7;
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.NUTS.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG2, (byte) (BlockData.LOG2.DARKOAK | BlockData.LOG2.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES2, (byte) (BlockData.LEAVES2.DARKOAK | BlockData.LEAVES2.NO_DECAY) )
							) );
			break;
		case CULTIVATED:
			treeHeight = 4;
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.CULTIVATED.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.OAK | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY) )
							) );
			break;

		}
		//generate the blocks
		for (Branch branch : branches) {
			generateBranch(world, branch);
		}
	}
	
	private void generateBranch(World world, Branch branch) {
		//go from top to bottom
		int topY = branch.y + branch.sectionShift;
		int curY;
		//put logs first
		if (branch.direction == BranchDirection.NONE) {
			for(curY = branch.y - (branch.length - 1); curY <= branch.y; curY++) {
				world.setBlock(branch.x, curY, branch.z, branch.logBlock);
			}
		} else {
			//real branch
			//might not have log if short branch
			if ((branch.direction.yShifts.length > 1) || (Math.random() < 0.5)) {
				int trunkX = branch.x - branch.direction.xzShifts[ branch.direction.xzShifts.length - 2 ];
				int trunkY = branch.y - branch.direction.yShifts[ branch.direction.yShifts.length - 1 ];
				int trunkZ = branch.z - branch.direction.xzShifts[ branch.direction.xzShifts.length - 1 ];
				int xzIndex = 0;
				int yIndex = 0;
				while ((yIndex < branch.direction.yShifts.length) && (xzIndex < branch.direction.xzShifts.length)) {
					world.setBlock(	trunkX + branch.direction.xzShifts[xzIndex],
									trunkY + branch.direction.yShifts[yIndex],
									trunkZ + branch.direction.xzShifts[xzIndex+1],
									branch.logBlock);
					xzIndex += 2;
					yIndex += 1;
				}
			}
		}
		//put leaves
		for (int sectionIndex = 0; sectionIndex < branch.sections.length; sectionIndex++ ) {
			curY = topY - sectionIndex;
			final ArrayList<Integer> leavesXShift = new ArrayList<Integer>(32);
			final ArrayList<Integer> leavesZShift = new ArrayList<Integer>(32);
			//set leaves placement depending on the section and a bit of randomness
			switch (branch.sections[sectionIndex]) {
			case ONE:
				leavesXShift.add(0);
				leavesZShift.add(0);
				break;
				
			case SIX_TO_EIGHT:
				if (Math.random() < 0.5) {
					leavesXShift.add(1);
					leavesZShift.add(1);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(1);
					leavesZShift.add(-1);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-1);
					leavesZShift.add(-1);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-1);
					leavesZShift.add(1);
				}
			case FIVE:
				leavesXShift.add(0);
				leavesZShift.add(0);

				leavesXShift.add(1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(-1);
				break;
				
			case TWENTYONE_TO_TWENTYFIVE:
				if (Math.random() < 0.5) {
					leavesXShift.add(2);
					leavesZShift.add(2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(2);
					leavesZShift.add(-2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-2);
					leavesZShift.add(-2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-2);
					leavesZShift.add(2);
				}
			case TWENTYONE:
				leavesXShift.add(0);
				leavesZShift.add(0);

				leavesXShift.add(1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(-1);
				
				leavesXShift.add(1);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(1);
				leavesXShift.add(1);
				leavesZShift.add(-1);
				leavesXShift.add(-1);
				leavesZShift.add(-1);

				leavesXShift.add(2);
				leavesZShift.add(-1);
				leavesXShift.add(2);
				leavesZShift.add(0);
				leavesXShift.add(2);
				leavesZShift.add(1);
				leavesXShift.add(-2);
				leavesZShift.add(-1);
				leavesXShift.add(-2);
				leavesZShift.add(0);
				leavesXShift.add(-2);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(2);
				leavesXShift.add(0);
				leavesZShift.add(2);
				leavesXShift.add(1);
				leavesZShift.add(2);
				leavesXShift.add(-1);
				leavesZShift.add(-2);
				leavesXShift.add(0);
				leavesZShift.add(-2);
				leavesXShift.add(1);
				leavesZShift.add(-2);

				break;
				
			case THIRTYSEVEN_TO_FOURTYONE:
				leavesXShift.add(0);
				leavesZShift.add(0);

				leavesXShift.add(1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(-1);
				
				leavesXShift.add(1);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(1);
				leavesXShift.add(1);
				leavesZShift.add(-1);
				leavesXShift.add(-1);
				leavesZShift.add(-1);

				leavesXShift.add(2);
				leavesZShift.add(-1);
				leavesXShift.add(2);
				leavesZShift.add(0);
				leavesXShift.add(2);
				leavesZShift.add(1);
				leavesXShift.add(-2);
				leavesZShift.add(-1);
				leavesXShift.add(-2);
				leavesZShift.add(0);
				leavesXShift.add(-2);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(2);
				leavesXShift.add(0);
				leavesZShift.add(2);
				leavesXShift.add(1);
				leavesZShift.add(2);
				leavesXShift.add(-1);
				leavesZShift.add(-2);
				leavesXShift.add(0);
				leavesZShift.add(-2);
				leavesXShift.add(1);
				leavesZShift.add(-2);
				
				leavesXShift.add(2);
				leavesZShift.add(2);
				leavesXShift.add(2);
				leavesZShift.add(-2);
				leavesXShift.add(-2);
				leavesZShift.add(-2);
				leavesXShift.add(-2);
				leavesZShift.add(2);

				leavesXShift.add(3);
				leavesZShift.add(-1);
				leavesXShift.add(3);
				leavesZShift.add(0);
				leavesXShift.add(3);
				leavesZShift.add(1);
				leavesXShift.add(-3);
				leavesZShift.add(-1);
				leavesXShift.add(-3);
				leavesZShift.add(0);
				leavesXShift.add(-3);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(3);
				leavesXShift.add(0);
				leavesZShift.add(3);
				leavesXShift.add(1);
				leavesZShift.add(3);
				leavesXShift.add(-1);
				leavesZShift.add(-3);
				leavesXShift.add(0);
				leavesZShift.add(-3);
				leavesXShift.add(1);
				leavesZShift.add(-3);
				
				if (Math.random() < 0.5) {
					leavesXShift.add(-3);
					leavesZShift.add(2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(3);
					leavesZShift.add(2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-3);
					leavesZShift.add(-2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(3);
					leavesZShift.add(-2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-2);
					leavesZShift.add(3);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-2);
					leavesZShift.add(-3);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(2);
					leavesZShift.add(3);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(2);
					leavesZShift.add(-3);
				}
				break;

			default:
				assert false;//unexpected
			}
			//place the leaves if it's possible
			assert leavesXShift.size() == leavesZShift.size();
			for (int leaveIndex = 0; leaveIndex < leavesXShift.size(); leaveIndex ++) {
				if ( world.getBlock(	branch.x + leavesXShift.get(leaveIndex),
										curY,
										branch.z + leavesZShift.get(leaveIndex)	).getType() == BlockType.AIR ) {
					world.setBlock( branch.x + leavesXShift.get(leaveIndex),
									curY,
									branch.z + leavesZShift.get(leaveIndex),
									branch.leavesBlock );
				}
			}
		}
	}
	
	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize,
			MinecraftMap.MapItemColors mapItemColors) {
		int treeHeight;//total height including top leaves
		final ArrayList<Branch> branches = new ArrayList<Branch>();
		Section[] sections;//used if sections definition is not fed directly to the branch creation function
		
		switch(type) {
		case OAK:
			if (forceStandard) {
				treeHeight = 6 + (int) Math.floor(Math.random() * 2);//6 or 7
			} else if (Math.random() < 0.8) { //80% of small oak trees
				treeHeight = 5 + (int) Math.floor(Math.random() * 3);//5 to 7
			} else {
				treeHeight = 5 + (int) Math.floor(Math.random() * 8);//5 to 12
			}
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.OAK.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.OAK | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY) )
							) );
			if(treeHeight > 7) {
				//the tree have branches
				BranchDirection direction;
				for(int branchStartY = treeHeight / 2; branchStartY < treeHeight-3; branchStartY++ ) {
					//random branch direction
					direction = BranchDirection.values()[ 1 + (int) (Math.random() * (BranchDirection.values().length - 1)) ];
					//create branch
					branches.add( new Branch(0, direction,
							TreeTypeSections.OAKBRANCH.sections,
							2, x + direction.xzShifts[direction.xzShifts.length - 2], y + branchStartY + direction.yShifts[direction.yShifts.length - 1], z + direction.xzShifts[direction.xzShifts.length - 1],
							new Block( BlockType.LOG, (byte) (BlockData.LOG.OAK | (direction.isNS ? BlockData.LOG.ORIENTATION_NS : BlockData.LOG.ORIENTATION_EW )) ),
							new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY) )
						) );
				}
			}
			break;
		case BIRCH:
			treeHeight = 5 + (int) Math.floor(Math.random() * 3);//5 to 7
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.BIRCH.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.BIRCH | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.BIRCH | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case ACACIA:
			treeHeight = 6 + (int) Math.floor(Math.random() * 3);//6 to 8
			branches.add( new Branch(treeHeight, BranchDirection.NONE,
								TreeTypeSections.ACACIA.sections,
								1, x, y + treeHeight - 1, z,
								new Block( BlockType.LOG2, (byte) (BlockData.LOG2.ACACIA | BlockData.LOG2.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES2, (byte) (BlockData.LEAVES2.ACACIA | BlockData.LEAVES2.NO_DECAY) )
							) );
			break;
		case PINE:
			treeHeight = 6 + (int) Math.floor(Math.random() * 4);//6 to 9
			sections = TreeTypeSections.PINE.sections;
			if (treeHeight >= 8 && Math.random() < 0.5) {
				sections = TreeTypeSections.WIDEPINE.sections;
			}
			if (treeHeight == 6 && Math.random() < 0.5) {
				sections = TreeTypeSections.ROUNDPINE.sections;
			}
			branches.add( new Branch(treeHeight, BranchDirection.NONE,
								sections,
								1, x, y + treeHeight - 1, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG2.DARKOAK | BlockData.LOG2.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.SPRUCE | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case SPRUCE:
			if (Math.random() < 0.9) { //90% of normal trees, rest might be bigger
				treeHeight = 7 + (int) Math.floor(Math.random() * 3);//7 to 9
			} else {
				treeHeight = 7 + (int) Math.floor(Math.random() * 5);//7 to 11
			}
			//build sections (leave 2 apparent trunk blocks at the end)
			sections = new Section[treeHeight-2];
			int sectionIndex = 0;
			//might add onr or two top single leaves block
			if ((treeHeight == 11) || (Math.random() < 0.5)) {
				sections[sectionIndex] = Section.ONE;
				sectionIndex ++;
				if ((Math.random() < 0.5)) {
					sections[sectionIndex] = Section.ONE;
					sectionIndex ++;
				}
			}
			TreeTypeSections baseSections;
			if (Math.random() < 0.8) {
				baseSections = TreeTypeSections.SPRUCE_CLASSIC;
			} else {
				if ((sections.length - sectionIndex) > 6) {
					baseSections = TreeTypeSections.SPRUCE_WIDETALL;
				} else {
					baseSections = TreeTypeSections.SPRUCE_WIDESHORT;
				}
			}
			System.arraycopy(baseSections.sections, 0, sections, sectionIndex, sections.length - sectionIndex);

			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.SPRUCE | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.SPRUCE | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case ORCHARD:
			treeHeight = 6;
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.ORCHARD.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.OAK | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case OLIVE:
			treeHeight = 5;
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.OLIVE.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG2, (byte) (BlockData.LOG2.DARKOAK | BlockData.LOG2.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.JUNGLE | BlockData.LEAVES.NO_DECAY) )
							) );
			break;
		case NUTS:
			treeHeight = 7;
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.NUTS.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG2, (byte) (BlockData.LOG2.DARKOAK | BlockData.LOG2.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES2, (byte) (BlockData.LEAVES2.DARKOAK | BlockData.LEAVES2.NO_DECAY) )
							) );
			break;
		case CULTIVATED:
			treeHeight = 4;
			//create trunk branch, always stop the log one block before the top
			branches.add( new Branch(treeHeight - 1, BranchDirection.NONE,
								TreeTypeSections.CULTIVATED.sections,
								1, x, y + treeHeight - 2, z,
								new Block( BlockType.LOG, (byte) (BlockData.LOG.OAK | BlockData.LOG.ORIENTATION_UD) ),
								new Block( BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY) )
							) );
			break;

		}
		//generate the blocks
		for (Branch branch : branches) {
			generateBranch(blockList, branch);
		}
		
	}
	
	private void generateBranch(Map<BlockMT,Object> blockList, Branch branch) {
		//go from top to bottom
		int topY = branch.y + branch.sectionShift;
		int curY;
		//put logs first
		if (branch.direction == BranchDirection.NONE) {
			for(curY = branch.y - (branch.length - 1); curY <= branch.y; curY++) {
				new BlockMT(branch.x, curY, branch.z, BlockTypeConverter.convert(branch.logBlock.getType())).addTo(blockList);
			}
		} else {
			//real branch
			//might not have log if short branch
			if ((branch.direction.yShifts.length > 1) || (Math.random() < 0.5)) {
				int trunkX = branch.x - branch.direction.xzShifts[ branch.direction.xzShifts.length - 2 ];
				int trunkY = branch.y - branch.direction.yShifts[ branch.direction.yShifts.length - 1 ];
				int trunkZ = branch.z - branch.direction.xzShifts[ branch.direction.xzShifts.length - 1 ];
				int xzIndex = 0;
				int yIndex = 0;
				while ((yIndex < branch.direction.yShifts.length) && (xzIndex < branch.direction.xzShifts.length)) {
					new BlockMT(trunkX + branch.direction.xzShifts[xzIndex],
								trunkY + branch.direction.yShifts[yIndex],
								trunkZ + branch.direction.xzShifts[xzIndex+1],
								BlockTypeConverter.convert(branch.logBlock.getType())).addTo(blockList);
					xzIndex += 2;
					yIndex += 1;
				}
			}
		}
		//put leaves
		for (int sectionIndex = 0; sectionIndex < branch.sections.length; sectionIndex++ ) {
			curY = topY - sectionIndex;
			final ArrayList<Integer> leavesXShift = new ArrayList<Integer>(32);
			final ArrayList<Integer> leavesZShift = new ArrayList<Integer>(32);
			//set leaves placement depending on the section and a bit of randomness
			switch (branch.sections[sectionIndex]) {
			case ONE:
				leavesXShift.add(0);
				leavesZShift.add(0);
				break;
				
			case SIX_TO_EIGHT:
				if (Math.random() < 0.5) {
					leavesXShift.add(1);
					leavesZShift.add(1);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(1);
					leavesZShift.add(-1);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-1);
					leavesZShift.add(-1);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-1);
					leavesZShift.add(1);
				}
			case FIVE:
				leavesXShift.add(0);
				leavesZShift.add(0);

				leavesXShift.add(1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(-1);
				break;
				
			case TWENTYONE_TO_TWENTYFIVE:
				if (Math.random() < 0.5) {
					leavesXShift.add(2);
					leavesZShift.add(2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(2);
					leavesZShift.add(-2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-2);
					leavesZShift.add(-2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-2);
					leavesZShift.add(2);
				}
			case TWENTYONE:
				leavesXShift.add(0);
				leavesZShift.add(0);

				leavesXShift.add(1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(-1);
				
				leavesXShift.add(1);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(1);
				leavesXShift.add(1);
				leavesZShift.add(-1);
				leavesXShift.add(-1);
				leavesZShift.add(-1);

				leavesXShift.add(2);
				leavesZShift.add(-1);
				leavesXShift.add(2);
				leavesZShift.add(0);
				leavesXShift.add(2);
				leavesZShift.add(1);
				leavesXShift.add(-2);
				leavesZShift.add(-1);
				leavesXShift.add(-2);
				leavesZShift.add(0);
				leavesXShift.add(-2);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(2);
				leavesXShift.add(0);
				leavesZShift.add(2);
				leavesXShift.add(1);
				leavesZShift.add(2);
				leavesXShift.add(-1);
				leavesZShift.add(-2);
				leavesXShift.add(0);
				leavesZShift.add(-2);
				leavesXShift.add(1);
				leavesZShift.add(-2);

				break;
				
			case THIRTYSEVEN_TO_FOURTYONE:
				leavesXShift.add(0);
				leavesZShift.add(0);

				leavesXShift.add(1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(0);
				leavesXShift.add(0);
				leavesZShift.add(-1);
				
				leavesXShift.add(1);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(1);
				leavesXShift.add(1);
				leavesZShift.add(-1);
				leavesXShift.add(-1);
				leavesZShift.add(-1);

				leavesXShift.add(2);
				leavesZShift.add(-1);
				leavesXShift.add(2);
				leavesZShift.add(0);
				leavesXShift.add(2);
				leavesZShift.add(1);
				leavesXShift.add(-2);
				leavesZShift.add(-1);
				leavesXShift.add(-2);
				leavesZShift.add(0);
				leavesXShift.add(-2);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(2);
				leavesXShift.add(0);
				leavesZShift.add(2);
				leavesXShift.add(1);
				leavesZShift.add(2);
				leavesXShift.add(-1);
				leavesZShift.add(-2);
				leavesXShift.add(0);
				leavesZShift.add(-2);
				leavesXShift.add(1);
				leavesZShift.add(-2);
				
				leavesXShift.add(2);
				leavesZShift.add(2);
				leavesXShift.add(2);
				leavesZShift.add(-2);
				leavesXShift.add(-2);
				leavesZShift.add(-2);
				leavesXShift.add(-2);
				leavesZShift.add(2);

				leavesXShift.add(3);
				leavesZShift.add(-1);
				leavesXShift.add(3);
				leavesZShift.add(0);
				leavesXShift.add(3);
				leavesZShift.add(1);
				leavesXShift.add(-3);
				leavesZShift.add(-1);
				leavesXShift.add(-3);
				leavesZShift.add(0);
				leavesXShift.add(-3);
				leavesZShift.add(1);
				leavesXShift.add(-1);
				leavesZShift.add(3);
				leavesXShift.add(0);
				leavesZShift.add(3);
				leavesXShift.add(1);
				leavesZShift.add(3);
				leavesXShift.add(-1);
				leavesZShift.add(-3);
				leavesXShift.add(0);
				leavesZShift.add(-3);
				leavesXShift.add(1);
				leavesZShift.add(-3);
				
				if (Math.random() < 0.5) {
					leavesXShift.add(-3);
					leavesZShift.add(2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(3);
					leavesZShift.add(2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-3);
					leavesZShift.add(-2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(3);
					leavesZShift.add(-2);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-2);
					leavesZShift.add(3);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(-2);
					leavesZShift.add(-3);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(2);
					leavesZShift.add(3);
				}
				if (Math.random() < 0.5) {
					leavesXShift.add(2);
					leavesZShift.add(-3);
				}
				break;

			default:
				assert false;//unexpected
			}
			//place the leaves if it's possible
			assert leavesXShift.size() == leavesZShift.size();
			for (int leaveIndex = 0; leaveIndex < leavesXShift.size(); leaveIndex ++) {
				if(!blockList.containsKey(new BlockMT(branch.x + leavesXShift.get(leaveIndex),
										curY,
										branch.z + leavesZShift.get(leaveIndex),
										BlockTypeConverter.convert(BlockType.AIR)))) {
					// Due to the hashcode & equals this means : If there are no blocks with the same X Y Z coordinates (no blocks <=> air)
					new BlockMT( branch.x + leavesXShift.get(leaveIndex),
									curY,
									branch.z + leavesZShift.get(leaveIndex),
									BlockTypeConverter.convert(branch.leavesBlock.getType() )).addTo(blockList);
				}
			}
		}
	}

}