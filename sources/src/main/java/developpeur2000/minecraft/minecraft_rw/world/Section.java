package developpeur2000.minecraft.minecraft_rw.world;

import java.util.logging.Logger;
import java.util.logging.Level;

import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBTException;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTCompoundProcessor;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

/**
 * Represents a chunk section, consisting of 16x16x16 blocks.
 */
@NBTCompoundType
public class Section implements NBTCompoundProcessor {
    private static final Logger LOGGER = Logger.getLogger("Section");
    
    private static final int AIR = 0;

    public static final int BLOCKS = Chunk.BLOCKS;
    private static final int BLOCKS_SQ = Chunk.BLOCKS * Chunk.BLOCKS;

    private static int xzy1D(int x, int z, int y) {
        return y * BLOCKS_SQ + z * BLOCKS + x;
    }
    private static byte nib4(byte[] arr, int i) {
        final int x = arr[i / 2] & 0xff;
		//careful that lower index data (even one) will be on the right half-byte
        return (i % 2 != 0) ? (byte) (x >>> 4) : (byte) (x & 0x0F);
    }
    private static void ntob4(byte[] arr, int i, byte value) {
		int halfi = i / 2;
		assert (value & 0x0F) == value;
		//careful that lower index data (even one) will be on the right half-byte
		arr[halfi] |= (i % 2 != 0) ? value << 4 : value ;
    }
    private static byte[] byteArrayToHalfByteArray(byte[] byteArr) {
    	byte [] halfByteArr = new byte[byteArr.length/2];
    	for(int i = 0; i < byteArr.length; i++)
    	{
    		ntob4(halfByteArr,i,byteArr[i]);
    	}
    	return halfByteArr;
    }

    public static int blockInSection(int b) {
        b %= BLOCKS;
        if (b < 0) {
            b += BLOCKS;
        }

        return b;
    }

    private byte y;
    private final Block[][][] blocks = new Block[BLOCKS][BLOCKS][BLOCKS];
    private final byte[] skyLight = new byte[BLOCKS*BLOCKS*BLOCKS / 2];
    private final byte[] blockLight = new byte[BLOCKS*BLOCKS*BLOCKS / 2];

    private transient int numNonAir = 0;

    /**
     * Constructs a new, empty section.
     */
    public Section() {
    }

    @Override
    public void unmarshalCompound(CompoundTag nbt) {
        y = nbt.getByte("Y");

        final byte[] data = nbt.getByteArray("Data");
        final byte[] skyLight = nbt.getByteArray("SkyLight");
        final byte[] blockLight = nbt.getByteArray("BlockLight");
        final byte[] blocks = nbt.getByteArray("Blocks");
        
        if ( (data.length != BLOCKS * BLOCKS * BLOCKS / 2)
        		|| (skyLight.length != BLOCKS * BLOCKS * BLOCKS / 2) 
        		|| (blockLight.length != BLOCKS * BLOCKS * BLOCKS / 2) 
        		|| (blocks.length != BLOCKS * BLOCKS * BLOCKS) ) {
        	throw new NBTException("Section has an incorrect byte length");
        }
        
        System.arraycopy(skyLight, 0, this.skyLight, 0, BLOCKS * BLOCKS * BLOCKS / 2);
        System.arraycopy(blockLight, 0, this.blockLight, 0, BLOCKS * BLOCKS * BLOCKS / 2);

        final byte[] add;
        if (nbt.contains("Add")) {
            add = nbt.getByteArray("Add");
            if (add.length != BLOCKS * BLOCKS * BLOCKS / 2) {
            	throw new NBTException("Section an incorrect byte length");
            }
        } else {
            add = null;
        }
        
        numNonAir = 0;
        for (int y = 0; y < BLOCKS; y++) {
            for (int z = 0; z < BLOCKS; z++) {
                for (int x = 0; x < BLOCKS; x++) {
                    final int i = xzy1D(x,z,y);

                    int id = blocks[i];
                    if (id < 0) {
                        id += 256;
                    }

                    int addValue;
                    if ( (add != null) && ((addValue = nib4(add, i)) > 0 ) ) {
                        id += addValue << 8;
                    }
                    
                    if(i == 3245)
                    {
                    	x = x + 1;
                    	x = x - 1;
                    }

                    if (id != AIR) {
                    	if (id >= BlockType.values().length) {
                    		LOGGER.log(Level.WARNING, "found a block with an unknown id : " + id
                    				+ " at position " + x + "," + y + "," + z);
                    	} else {
	                        final Block block = new Block();
	                        block.setType(BlockType.values()[id]);
	                        block.setData(nib4(data, i));
	                        this.blocks[x][y][z] = block;
	
	                        numNonAir++;
                    	}
                    }
                }
            }
        }
    }

    @Override
    public CompoundTag marshalCompound() {
    	assert !isEmpty();
    	
        CompoundTag root = new CompoundTag();

        // transform Block data into section structure data as it is in a region file
        final byte[] data = new byte[BLOCKS*BLOCKS*BLOCKS];
        final byte[] byteBlocks = new byte[BLOCKS*BLOCKS*BLOCKS];
        final byte[] add = new byte[BLOCKS*BLOCKS*BLOCKS];
        boolean needAddData = false;

        for (int y = 0; y < BLOCKS; y++) {
            for (int z = 0; z < BLOCKS; z++) {
                for (int x = 0; x < BLOCKS; x++) {
                    final int i = y * BLOCKS_SQ + z * BLOCKS + x;

                    Block block = blocks[x][y][z];
                    
                    if(block != null)
                    {
                    	data[i] = (byte) (block.getData() & 0x0F);

                    	int blockId = block.getType().ordinal();
                    	if((blockId >> 8) > 0) {
                    		add[i] = (byte) (blockId >> 8);
                        	needAddData = true;
                        }
                    	byteBlocks[i] = (byte) (blockId & 0xff);
                    }
                }
            }
        }
        
        root.put("Blocks", byteBlocks);
        root.put("SkyLight", skyLight);
        root.put("Y", y);
        root.put("BlockLight", blockLight);
        root.put("Data", byteArrayToHalfByteArray(data));
        if(needAddData) {
            root.put("Add", byteArrayToHalfByteArray(add));
        }

        return root;
    }

    boolean isEmpty() {
        return (numNonAir == 0);
    }

    Block getBlock(int x, int y, int z) {
        x = blockInSection(x);
        y = blockInSection(y);
        z = blockInSection(z);
        return blocks[x][y][z];
    }

    Block getBlockInChunk(int xInChunk, int y, int zInChunk) {
        y = blockInSection(y);
        return blocks[xInChunk][y][zInChunk];
    }
   
    void setBlock(int x, int y, int z, Block block) {
        x = blockInSection(x);
        y = blockInSection(y);
        z = blockInSection(z);

        boolean wasAir = (blocks[x][y][z] == null);
        blocks[x][y][z] = block;

        if (wasAir && (block != null)) {
            numNonAir++;
        } else if (!wasAir && (block == null)) {
            numNonAir--;
        }
    }

    byte getBlockLight(int x, int y, int z) {
        x = blockInSection(x);
        y = blockInSection(y);
        z = blockInSection(z);
        return nib4(blockLight, xzy1D(x,z,y));
    }

    void setBlockLight(int x, int y, int z, byte value) {
    	assert (value <= 0x0f);

    	x = blockInSection(x);
        y = blockInSection(y);
        z = blockInSection(z);
        ntob4(blockLight, xzy1D(x,z,y), value);
    }

    byte getSkyLight(int x, int y, int z) {
        x = blockInSection(x);
        y = blockInSection(y);
        z = blockInSection(z);
        return nib4(skyLight, xzy1D(x,z,y));
    }

    void setSkyLight(int x, int y, int z, byte value) {
    	assert (value <= 0x0f);

    	x = blockInSection(x);
        y = blockInSection(y);
        z = blockInSection(z);
        ntob4(skyLight, xzy1D(x,z,y), value);
    }

    public byte getY() {
        return y;
    }

    public void setY(byte y) {
        this.y = y;
    }
}
