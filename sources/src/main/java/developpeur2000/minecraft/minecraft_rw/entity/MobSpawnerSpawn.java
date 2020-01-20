package developpeur2000.minecraft.minecraft_rw.entity;

import java.util.HashMap;

import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.StringTag;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

@NBTCompoundType
public class MobSpawnerSpawn {
    @NBTProperty(upperCase = true)
    private HashMap<String, NBT> entity;

    @NBTProperty(upperCase = true)
    private int weight;
    
    //for compatibility with 1.8
    @NBTProperty(upperCase = true, optional = true)
    private String type;
    
    public MobSpawnerSpawn() {
    }

    public MobSpawnerSpawn(HashMap<String, NBT> entity) {
    	setEntity(entity);
    	weight = 1;
    }

    public MobSpawnerSpawn(MobSpawnerSpawn src) {
    	setEntity(src.entity);
    	weight = src.weight;
    }
    
    public HashMap<String, NBT> getEntity() {
        return entity;
    }
    public void setEntity(HashMap<String, NBT> entity) {
        this.entity = entity;
        //copy to 1.8 format
        this.type = (String) entity.get("id").getValue();
    }

    public int getWeight() {
        return weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
        //copy to 1.9 format
    	entity = new HashMap<String, NBT>();
    	entity.put("id", new StringTag(type));
    }
}