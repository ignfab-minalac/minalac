package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for minecart with command block entities.
 */
@NBTCompoundType
public class MinecartCommandBlock extends Minecart {
    @NBTProperty(upperCase = true)
    private String command;

    @NBTProperty(upperCase = true)
    private int successCount;

    @NBTProperty(upperCase = true)
    private String lastOutput;

    @NBTProperty(upperCase = true)
    private boolean trackOutput;

    /**
     * Constructs a new blank entity (to use when loading from file)
     */
    public MinecartCommandBlock() {
    	super("MinecartCommandBlock");
    	command = "";
    	successCount = 0;
    	lastOutput = "";
    	trackOutput = false;
    }

    /**
     * copy constructor
     */
    public MinecartCommandBlock(MinecartCommandBlock src) {
    	super(src);
    	command = src.command;
    	successCount = src.successCount;
    	lastOutput = src.lastOutput;
    	trackOutput = src.trackOutput;
    }
    
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public String getLastOutput() {
        return lastOutput;
    }

    public void setLastOutput(String lastOutput) {
        this.lastOutput = lastOutput;
    }

    public boolean getTrackOutput() {
        return trackOutput;
    }

    public void setTrackOutput(boolean trackOutput) {
        this.trackOutput = trackOutput;
    }
}
