package developpeur2000.minecraft.minecraft_rw.world;

import java.util.Date;

import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTCompoundProcessor;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

/**
 * Represents a level.dat file.
 */
@NBTCompoundType
public class Level implements NBTCompoundProcessor {
	
	private final static int LEVEL_FILE_VERSION = 19133;

    enum GameType {
    	SURVIVAL_MODE,
    	CREATIVE_MODE,
    	ADVENTURE_MODE,
    	SPECTATOR_MODE,
    }
    enum Difficulty {
    	PEACEFUL,
    	EASY,
    	NORMAL,
    	HARD
    }

    private int version;
    private String levelName;
    private String generatorName;
    private int generatorVersion;
    private String generatorOptions;
    private long randomSeed;
    private boolean mapFeatures;
    private long lastPlayed;
    private long sizeOnDisk;
    private boolean allowCommands;
    private boolean hardcore;
    private GameType gameType;
    private Difficulty difficulty;
    private boolean difficultyLocked;
    private long time;
    private long dayTime;
    private int spawnX;
    private int spawnY;
    private int spawnZ;
    private double borderCenterX;
    private double borderCenterZ;
    private double borderSize;
    private double borderSafeZone;
    private double borderWarningBlocks;
    private double borderWarningTime;
    private double borderSizeLerpTarget;
    private long borderSizeLerpTime;
    private double borderDamagePerBlock;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private int clearWeatherTime;
    /* game rules part */
    private boolean gameRulesCommandBlockOutput;
    private boolean gameRulesDoDaylightCycle;
    private boolean gameRulesDoFireTick;
    private boolean gameRulesDoMobLoot;
    private boolean gameRulesDoMobSpawning;
    private boolean gameRulesDoTileDrops;
    private boolean gameRulesKeepInventory;
    private boolean gameRulesLogAdminCommands;
    private boolean gameRulesMobGriefing;
    private boolean gameRulesNaturalRegeneration;
    private String gameRulesRandomTickSpeed;
    private boolean gameRulesSendCommandFeedback;
    private boolean gameRulesShowDeathMessages;
    
    public transient boolean dirty = true;

    /**
     * Constructs a new level with default values.
     */
    public Level() {
        version = LEVEL_FILE_VERSION;
        levelName = "";
        generatorName = "default";
        generatorVersion = 0;
        generatorOptions = "";
        randomSeed = 0;
        mapFeatures = false;
    	Date date= new Date();
        lastPlayed = date.getTime();
        sizeOnDisk = 0;
        allowCommands = true;
        hardcore = false;
        gameType = GameType.CREATIVE_MODE;
        difficulty = Difficulty.NORMAL;
        difficultyLocked = false;
        time = 0;
        dayTime = 0;
        spawnX = 0;
        spawnY = 0;
        spawnZ = 0;
        borderCenterX = 0;
        borderCenterZ = 0;
        borderSize = 60000000;
        borderSafeZone = 5;
        borderWarningBlocks = 5;
        borderWarningTime = 15;
        borderSizeLerpTarget = 60000000;
        borderSizeLerpTime = 0;
        borderDamagePerBlock = 0.2;
        raining = false;
        rainTime = 0x7FFFFFFF;
        thundering = false;
        thunderTime = 0x7FFFFFFF;
        clearWeatherTime = 0x7FFFFFFF;
        /* game rules part */
        gameRulesCommandBlockOutput = true;
        gameRulesDoDaylightCycle = true;
        gameRulesDoFireTick = true;
        gameRulesDoMobLoot = true;
        gameRulesDoMobSpawning = false;
        gameRulesDoTileDrops = true;
        gameRulesKeepInventory = false;
        gameRulesLogAdminCommands = true;
        gameRulesMobGriefing = true;
        gameRulesNaturalRegeneration = true;
        gameRulesRandomTickSpeed = "3";
        gameRulesSendCommandFeedback = true;
        gameRulesShowDeathMessages = true;
    }

    @Override
    public void unmarshalCompound(CompoundTag nbt) {
    	if(nbt.getBoolean("initialized") == false) {
    		throw new WorldException("level file has is marked as not correctly initialized");
    	}
        version = nbt.getInt("version");
        if(version != LEVEL_FILE_VERSION) {
        	throw new WorldException("level file version is not supported");
        }
        levelName = nbt.getString("LevelName");
        generatorName = nbt.getString("generatorName");
        generatorVersion = nbt.getInt("generatorVersion");
        generatorOptions = nbt.getString("generatorOptions");
        randomSeed = nbt.getLong("RandomSeed");
        mapFeatures = nbt.getBoolean("MapFeatures");
        lastPlayed = nbt.getLong("LastPlayed");
        sizeOnDisk = nbt.getLong("SizeOnDisk");
        allowCommands = nbt.getBoolean("allowCommands");
        hardcore = nbt.getBoolean("hardcore");
        gameType = GameType.values()[ nbt.getInt("GameType") ];
        difficulty = Difficulty.values()[ nbt.getByte("Difficulty") ];
        difficultyLocked = nbt.getBoolean("DifficultyLocked");
        time = nbt.getLong("Time");
        dayTime = nbt.getLong("DayTime");
        spawnX = nbt.getInt("SpawnX");
        spawnY = nbt.getInt("SpawnY");
        spawnZ = nbt.getInt("SpawnZ");
        borderCenterX = nbt.getDouble("BorderCenterX");
        borderCenterZ = nbt.getDouble("BorderCenterZ");
        borderSize = nbt.getDouble("BorderSize");
        borderSafeZone = nbt.getDouble("BorderSafeZone");
        borderWarningBlocks = nbt.getDouble("BorderWarningBlocks");
        borderWarningTime = nbt.getDouble("BorderWarningTime");
        borderSizeLerpTarget = nbt.getDouble("BorderSizeLerpTarget");
        borderSizeLerpTime = nbt.getLong("BorderSizeLerpTime");
        borderDamagePerBlock = nbt.getDouble("BorderDamagePerBlock");
        raining = nbt.getBoolean("raining");
        rainTime = nbt.getInt("rainTime");
        thundering = nbt.getBoolean("thundering");
        thunderTime = nbt.getInt("thunderTime");
        clearWeatherTime = nbt.getInt("clearWeatherTime");
        /* game rules part */
        CompoundTag gameRules = nbt.getCompound("GameRules");
        gameRulesCommandBlockOutput = gameRules.getString("commandBlockOutput").equalsIgnoreCase("true");
        gameRulesDoDaylightCycle = gameRules.getString("doDaylightCycle").equalsIgnoreCase("true");
        gameRulesDoFireTick = gameRules.getString("doFireTick").equalsIgnoreCase("true");
        gameRulesDoMobLoot = gameRules.getString("doMobLoot").equalsIgnoreCase("true");
        gameRulesDoMobSpawning = gameRules.getString("doMobSpawning").equalsIgnoreCase("true");
        gameRulesDoTileDrops = gameRules.getString("doTileDrops").equalsIgnoreCase("true");
        gameRulesKeepInventory = gameRules.getString("keepInventory").equalsIgnoreCase("true");
        gameRulesLogAdminCommands = gameRules.getString("logAdminCommands").equalsIgnoreCase("true");
        gameRulesMobGriefing = gameRules.getString("mobGriefing").equalsIgnoreCase("true");
        gameRulesNaturalRegeneration = gameRules.getString("naturalRegeneration").equalsIgnoreCase("true");
        gameRulesRandomTickSpeed = gameRules.getString("randomTickSpeed");
        gameRulesSendCommandFeedback = gameRules.getString("sendCommandFeedback").equalsIgnoreCase("true");
        gameRulesShowDeathMessages = gameRules.getString("showDeathMessages").equalsIgnoreCase("true");
        
        dirty = false;
    }

    @Override
    public CompoundTag marshalCompound() {
        CompoundTag root = new CompoundTag();
        root.put("initialized", true);
        root.put("version", version);
        root.put("LevelName", levelName);
        root.put("generatorName", generatorName);
        root.put("generatorVersion", generatorVersion);
        root.put("generatorOptions", generatorOptions);
        root.put("RandomSeed", randomSeed);
        root.put("MapFeatures", mapFeatures);
        root.put("LastPlayed", lastPlayed);
        root.put("SizeOnDisk", sizeOnDisk);
        root.put("allowCommands", allowCommands);
        root.put("hardcore", hardcore);
        root.put("GameType", gameType.ordinal());
        root.put("Difficulty", (byte) difficulty.ordinal());
        root.put("DifficultyLocked", difficultyLocked);
        root.put("Time", time);
        root.put("DayTime", dayTime);
        root.put("SpawnX", spawnX);
        root.put("SpawnY", spawnY);
        root.put("SpawnZ", spawnZ);
        root.put("BorderCenterX", borderCenterX);
        root.put("BorderCenterZ", borderCenterZ);
        root.put("BorderSize", borderSize);
        root.put("BorderSafeZone", borderSafeZone);
        root.put("BorderWarningBlocks", borderWarningBlocks);
        root.put("BorderWarningTime", borderWarningTime);
        root.put("BorderSizeLerpTarget", borderSizeLerpTarget);
        root.put("BorderSizeLerpTime", borderSizeLerpTime);
        root.put("BorderDamagePerBlock", borderDamagePerBlock);
        root.put("raining", raining);
        root.put("rainTime", rainTime);
        root.put("thundering", thundering);
        root.put("thunderTime", thunderTime);
        root.put("clearWeatherTime", clearWeatherTime);
        /* game rules part */
        CompoundTag gameRules = new CompoundTag();
        gameRules.put("commandBlockOutput", gameRulesCommandBlockOutput ? "True" : "False");
        gameRules.put("doDaylightCycle", gameRulesDoDaylightCycle ? "True" : "False");
        gameRules.put("doFireTick", gameRulesDoFireTick ? "True" : "False");
        gameRules.put("doMobLoot", gameRulesDoMobLoot ? "True" : "False");
        gameRules.put("doMobSpawning", gameRulesDoMobSpawning ? "True" : "False");
        gameRules.put("doTileDrops", gameRulesDoTileDrops ? "True" : "False");
        gameRules.put("keepInventory", gameRulesKeepInventory ? "True" : "False");
        gameRules.put("logAdminCommands", gameRulesLogAdminCommands ? "True" : "False");
        gameRules.put("mobGriefing", gameRulesMobGriefing ? "True" : "False");
        gameRules.put("naturalRegeneration", gameRulesNaturalRegeneration ? "True" : "False");
        gameRules.put("randomTickSpeed", gameRulesRandomTickSpeed);
        gameRules.put("sendCommandFeedback", gameRulesSendCommandFeedback ? "True" : "False");
        gameRules.put("showDeathMessages", gameRulesShowDeathMessages ? "True" : "False");
        root.put("GameRules", gameRules);

        return root;
    }
    
	public boolean isDirty() {
        return dirty;
    }

	public void saved() {
        this.dirty = false;
    }
	
	public void setName(String name) {
		this.levelName = name;
        this.dirty = true;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
        this.dirty = true;
	}

}
