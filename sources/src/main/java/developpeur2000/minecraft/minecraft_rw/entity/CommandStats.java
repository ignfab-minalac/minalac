package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Contains information identifying scoreboard parameters to modify relative to the last command run.
 *
 * Mapped according to http://minecraft.gamepedia.com/Chunk_format#Entity_Format.
 */
@NBTCompoundType
public class CommandStats {
    @NBTProperty(upperCase = true)
    private String successCountName;

    @NBTProperty(upperCase = true)
    private String successCountObjective;

    @NBTProperty(upperCase = true)
    private String affectedBlocksName;

    @NBTProperty(upperCase = true)
    private String affectedBlocksObjective;

    @NBTProperty(upperCase = true)
    private String affectedEntitiesName;

    @NBTProperty(upperCase = true)
    private String affectedEntitiesObjective;

    @NBTProperty(upperCase = true)
    private String affectedItemsName;

    @NBTProperty(upperCase = true)
    private String affectedItemsObjective;

    public String getSuccessCountName() {
        return successCountName;
    }

    public void setSuccessCountName(String successCountName) {
        this.successCountName = successCountName;
    }

    public String getSuccessCountObjective() {
        return successCountObjective;
    }

    public void setSuccessCountObjective(String successCountObjective) {
        this.successCountObjective = successCountObjective;
    }

    public String getAffectedBlocksName() {
        return affectedBlocksName;
    }

    public void setAffectedBlocksName(String affectedBlocksName) {
        this.affectedBlocksName = affectedBlocksName;
    }

    public String getAffectedBlocksObjective() {
        return affectedBlocksObjective;
    }

    public void setAffectedBlocksObjective(String affectedBlocksObjective) {
        this.affectedBlocksObjective = affectedBlocksObjective;
    }

    public String getAffectedEntitiesName() {
        return affectedEntitiesName;
    }

    public void setAffectedEntitiesName(String affectedEntitiesName) {
        this.affectedEntitiesName = affectedEntitiesName;
    }

    public String getAffectedEntitiesObjective() {
        return affectedEntitiesObjective;
    }

    public void setAffectedEntitiesObjective(String affectedEntitiesObjective) {
        this.affectedEntitiesObjective = affectedEntitiesObjective;
    }

    public String getAffectedItemsName() {
        return affectedItemsName;
    }

    public void setAffectedItemsName(String affectedItemsName) {
        this.affectedItemsName = affectedItemsName;
    }

    public String getAffectedItemsObjective() {
        return affectedItemsObjective;
    }

    public void setAffectedItemsObjective(String affectedItemsObjective) {
        this.affectedItemsObjective = affectedItemsObjective;
    }
}
