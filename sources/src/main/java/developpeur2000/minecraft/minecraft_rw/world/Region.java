package developpeur2000.minecraft.minecraft_rw.world;

import developpeur2000.minecraft.minecraft_rw.entity.BlockEntity;
import developpeur2000.minecraft.minecraft_rw.entity.Entity;
import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.NBTException;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTMarshal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Represents a region, consisting of 32x32 chunks.
 */
public class Region {
    public static final int CHUNKS = 32;
    public static final int BLOCKS = CHUNKS * Chunk.BLOCKS;

    private static int blockToRegionChunk(int b) {
        b %= BLOCKS;
        if (b < 0) {
            b += BLOCKS;
        }

        return b / Chunk.BLOCKS;
    }
    private static int blockToAbsoluteChunk(int b) {
        return (b >= 0) ? (b / Chunk.BLOCKS) : ((b + 1) / Chunk.BLOCKS - 1);
    }

    private final Path file;

    private long[][] chunkFileOffsets = null;
    private int[][] chunkTimestamps = new int[CHUNKS][CHUNKS];
	private byte[][][] compressedChunksCache = new byte[CHUNKS][CHUNKS][]; /* [x][z] array of compressed data as byte[] */
    private Chunk[][] chunks = new Chunk[CHUNKS][CHUNKS];

    public transient boolean dirty = false;

    /**
     * Constructs a new, empty region.
     */
    Region(Path file) throws IOException {
        this.file = file;

        if (Files.exists(file)) {
        	chunkFileOffsets = new long[CHUNKS][CHUNKS];
            try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
            	//read chunks locations in file, indicated in the first 4096 bytes of the file
                for (int z = 0; z < CHUNKS; z++) {
                    for (int x = 0; x < CHUNKS; x++) {
                        final int location = in.readInt();
                        //third first bytes indicates the offset, by block of 4096(2^12)bytes
                        //we store the offset in bytes
                        chunkFileOffsets[x][z] = (location >> 8) << 12;
                    }
                }
                //read chunks timestamps
                for (int z = 0; z < CHUNKS; z++) {
                    for (int x = 0; x < CHUNKS; x++) {
                    	chunkTimestamps[x][z] = in.readInt();
                    }
                }

            }

            dirty = false;
        } else {
            dirty = true;
        }
    }

	boolean isDirty() {
        return dirty;
    }

    void save() throws IOException {
    	save(file);
    }

    void save(Path saveFile) throws IOException {
    	boolean hasInputFile = file.toFile().exists();
    	boolean saveToTempFile = false;
    	boolean saveOperation = saveFile.equals(file);
    	if(hasInputFile && saveOperation) {
    		//save to a temporary file that will replace the region file,
    		// because we might need to read from the old file
    		saveToTempFile = true;
    		saveFile = file.resolveSibling("tmp." + UUID.randomUUID() + "." + file.getFileName());
    	}
    	
        try (	final RandomAccessFile inputFile = hasInputFile ? new RandomAccessFile(file.toString(), "r") : null;
        		final ByteArrayOutputStream chunkCompressedDataBytes = new ByteArrayOutputStream();
        		final DataOutputStream chunkCompressedDataStream = new DataOutputStream (chunkCompressedDataBytes);
        		final DataOutputStream outputStream = new DataOutputStream (new FileOutputStream(saveFile.toString())) ) {
            //write chunk compressed data to a memory stream and store offsets
        	int[][] saveChunkOffsets = new int[BLOCKS][BLOCKS];
            long offset;
            int chunkSize;
            byte compression = NBT.getDefaultSaveCompression();
            byte[] chunkData = null;
            byte[] chunkPaddedData = new byte[4096];//empty data to finish filling a chunk to reach a 4kB sector multiple size
            int chunkSectorSize, chunkPaddingSize;
            for (int z = 0; z < CHUNKS; z++) {
                for (int x = 0; x < CHUNKS; x++) {
                    if ( (chunkCompressedDataStream.size()!=Integer.MAX_VALUE) && ((chunkCompressedDataStream.size() % 4096) != 0) ) {
                    	throw new WorldException("chunk offset mismatch in region file " + saveFile.toString());
                	}
                	offset = (int) (chunkCompressedDataStream.size()>>12) + 2; // add 2 for offset and timestamps 4kB sectors
                	saveChunkOffsets[x][z] = (int) (offset << 8);
                	
                	//generate chunk data
                    if((chunks[x][z] != null) && chunks[x][z].isDirty()) {
                    	//change chunk timestamps & update sector count
                    	Date date= new Date();
                    	chunkTimestamps[x][z] = (int) (date.getTime()/1000);
                   		//compress the chunk data and save it in cache
                 		compressedChunksCache[x][z] = generateChunkCompressedData(chunks[x][z]);
                 		//consider the chunk is saved
                    	if(saveOperation) {
                    		chunks[x][z].saved();
                    	}
                    }

                	if (compressedChunksCache[x][z] == null) {
                		if(inputFile != null && chunkFileOffsets != null && chunkFileOffsets[x][z] != 0) {
	                		//copy chunk data from original file
	                		inputFile.seek(chunkFileOffsets[x][z]);
	                		chunkSize = inputFile.readInt();
	                        compression = inputFile.readByte();
	                		if (chunkSize > 0) {
		                        chunkData = new byte[chunkSize - 1];
		                        inputFile.read(chunkData);
	                		}
                		} else {
                			//indicate empty chunk data, chunk won't be saved
                			chunkSize = 0;
                		}
                	} else {
                		chunkSize = compressedChunksCache[x][z].length;
                		chunkData = compressedChunksCache[x][z];
                	}
                	if(chunkSize > 0) {
	                	//write the data
	                	chunkCompressedDataStream.writeInt(chunkSize);
	                	chunkCompressedDataStream.writeByte(compression);
	                	chunkCompressedDataStream.write(chunkData);
	                	//round up to sector size the chunk compressed length
	                	chunkSectorSize = ((chunkData.length+5) / 4096) + 1;// add 5 for chunkSize and compression byte
	                	chunkPaddingSize = 4096 * chunkSectorSize - (chunkData.length+5);
	                	chunkCompressedDataStream.write(chunkPaddedData, 0, chunkPaddingSize);
	                	//save chunk sector length
	                	if(chunkSectorSize > 0x0f) {
	                		throw new WorldException("chunk size over one byte : " + chunkSectorSize);
	                	}
                	} else {
                		chunkSectorSize = 0;
            			saveChunkOffsets[x][z] = 0;
                	}
                	saveChunkOffsets[x][z] |= chunkSectorSize;

                }
            }

        	//first write offsets
            for (int z = 0; z < CHUNKS; z++) {
                for (int x = 0; x < CHUNKS; x++) {
                	outputStream.writeInt( saveChunkOffsets[x][z] );
                }
            }
            //write timestamps
            for (int z = 0; z < CHUNKS; z++) {
                for (int x = 0; x < CHUNKS; x++) {
                	outputStream.writeInt( chunkTimestamps[x][z] );
                }
            }
            //and finally write chunk compressed data
            if ( (outputStream.size()!=Integer.MAX_VALUE) && ((outputStream.size()>>12)!=2) ) {
            	throw new WorldException("chunk offset mismatch in region file " + saveFile.toString());
        	}
            outputStream.write(chunkCompressedDataBytes.toByteArray());
            
            if(saveOperation) {
            	dirty = false;
            }
        }
        
        if (saveToTempFile) {
        	//replace file by temp file
        	try {
        		Files.delete(file);
        	} catch (Exception e) {
        		//unexpected
        		assert false;
        	}
        	
        	if ( ! saveFile.toFile().renameTo(file.toFile()) ) {
        		throw new WorldException("failed to rename temp region file " + saveFile.toString() + " to region file " + file.toString());
        	}
        	
        }
    }

    private Chunk getChunkAt(int x, int z) {
    	int absoluteX = x;
    	int absoluteZ = z;
    	
        x = blockToRegionChunk(x);
        z = blockToRegionChunk(z);

        Chunk chunk = chunks[x][z];
        if (chunk == null) {

            if (file != null && chunkFileOffsets != null && chunkFileOffsets[x][z] > 0) {
                //load chunk
                try (final RandomAccessFile raf = new RandomAccessFile(file.toString(), "r")) {
                    raf.seek(chunkFileOffsets[x][z]);
                    final int size = raf.readInt();
                    final int compression = raf.readByte();

                    if (size > 0) {
	                    compressedChunksCache[x][z] = new byte[size];
	                    raf.read(compressedChunksCache[x][z]);
	
	                    final CompoundTag chunkNbt;
	                    try (final ByteArrayInputStream bis = new ByteArrayInputStream(compressedChunksCache[x][z])) {
	                        switch (compression) {
	                            case NBT.COMPRESSION_GZIP:
	                                chunkNbt = NBT.loadDirect(new GZIPInputStream(bis));
	                                break;
	
	                            case NBT.COMPRESSION_7ZIP:
	                                chunkNbt = NBT.loadDirect(new InflaterInputStream(bis));
	                                break;
	
	                            default:
	                                throw new UnsupportedOperationException(
	                                        "Unsupported compression type " + compression);
	                        }
	                    }
	
	                    chunk = NBTMarshal.unmarshal(Chunk.class, chunkNbt.getCompound("Level"));
                    } else {
                        chunk = new Chunk(blockToAbsoluteChunk(absoluteX),blockToAbsoluteChunk(absoluteZ));
                    }
                } catch (IOException ex) {
                    throw new WorldException("Failed to load chunk " + x +"," + z
                    			+ " (block " + absoluteX + "," + absoluteZ + ")"
                    			+ " (offset " + chunkFileOffsets[x][z] + " in region file " + file.toString()
                    		, ex);
                }
            } else {
                chunk = new Chunk(blockToAbsoluteChunk(absoluteX),blockToAbsoluteChunk(absoluteZ));
            }
            
            if((chunk.getX() != blockToAbsoluteChunk(absoluteX)) ||(chunk.getZ() != blockToAbsoluteChunk(absoluteZ))) {
            	throw new WorldException("Loaded chunk at chunk pos "+blockToAbsoluteChunk(absoluteX)+"/"+blockToAbsoluteChunk(absoluteZ)
            								+" has different coordinates values : "+chunk.getX()+"/"+chunk.getZ());
            }


            chunks[x][z] = chunk;
        }

        return chunk;
    }

    Block getBlock(int x, int y, int z) {
        return getChunkAt(x, z).getBlock(x, y, z);
    }

    public void setBlock(int x, int y, int z, Block block) throws NBTException {
        final Chunk chunk = getChunkAt(x, z);
        chunk.setBlock(x, y, z, block);
        dirty = (dirty || chunk.isDirty());
    }
    
    void addEntity(double x, double y, double z, Entity entity) {
        final Chunk chunk = getChunkAt((int) x, (int) z);
        chunk.addEntity(x, y, z, entity);
        dirty = (dirty || chunk.isDirty());
    }

	public ArrayList<Entity> listEntitiesInChunk(int x, int z) {
        return getChunkAt(x, z).getEntities();
    }

    void addBlockEntity(int x, int y, int z, BlockEntity blockEntity) {
        final Chunk chunk = getChunkAt((int) x, (int) z);
        chunk.addBlockEntity(x, y, z, blockEntity);
        dirty = (dirty || chunk.isDirty());
    }

	public ArrayList<BlockEntity> listBlockEntitiesInChunk(int x, int z) {
        return getChunkAt(x, z).getBlockEntities();
    }

    
    byte[] generateChunkCompressedData(Chunk chunk) {
        CompoundTag chunkDataNbt = (CompoundTag) NBTMarshal.marshal(chunk);
        CompoundTag chunkNbt = new CompoundTag();
        chunkNbt.put("Level", chunkDataNbt);
        
        ByteArrayOutputStream compressedBytesStream = new ByteArrayOutputStream();
        try {
			NBT.save(compressedBytesStream, chunkNbt);
		} catch (IOException e) {
			throw new NBTException("couldn't create chunk data in memory", e);
		}
        //add a null ending
        compressedBytesStream.write(0);
        return compressedBytesStream.toByteArray();
    }
}
