/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ignfab.minetest;

public class LuaModGenerator {
	public static String getInitLua(int spawnX, int spawnY, int spawnZ, boolean plainUnderground) {
		/*int undergroundLimit;
		
		if(plainUnderground) {
			undergroundLimit = 0; // Go from surface y down to 0
		} else {
			undergroundLimit = UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT; // Go from surface y down to UNDERGROUND_DEFINITION_LIMIT
			// (then from UNDERGROUND_DEFINITION_LIMIT down to 0, it is a pregenerated MC underground which is already written)
		}*/
		
		String ignfabBlocks = "minetest.register_node(\"ignfab:ignlogo\", {\n" + 
				"	description = \"IGN logo\",\n" + 
				"	drawtype = \"signlike\",\n" + 
				"	walkable = false,\n" + 
				"	tiles = {\"logo-ign.png\"},\n" + 
				"	wield_image =  \"logo-ign.png\",\n" + 
				"	inventory_image =  \"logo-ign.png\",\n" + 
				"	paramtype = \"light\",\n" + 
				"	paramtype2 = \"wallmounted\",\n" + 
				"	selection_box = {\n" + 
				"		type = \"wallmounted\",\n" + 
				"	},\n" + 
				"	groups = {oddly_breakable_by_hand = 3, not_in_creative_inventory = 1}, \n" + 
				"});\n" + 
				"\n" + 
				"minetest.register_node(\"ignfab:ignfablogo\", {\n" + 
				"	description = \"IGNfab logo\",\n" + 
				"	drawtype = \"signlike\",\n" + 
				"	walkable = false,\n" + 
				"	tiles = {\"logo-ignfab.png\"},\n" + 
				"	wield_image =  \"logo-ignfab.png\",\n" + 
				"	inventory_image =  \"logo-ignfab.png\",\n" + 
				"	paramtype = \"light\",\n" + 
				"	paramtype2 = \"wallmounted\",\n" + 
				"	selection_box = {\n" + 
				"		type = \"wallmounted\",\n" + 
				"	},\n" + 
				"	groups = {oddly_breakable_by_hand = 3, not_in_creative_inventory = 1}, \n" + 
				"});\n" +
				"\n";
		
		ignfabBlocks += "\n\nminetest.register_node(\"ignfab:overview\", {\n" + 
				"	description = \"IGN Map Overview\",\n" + 
				"	visual_scale = 3.0,\n" + 
				"	drawtype = \"signlike\",\n" + 
				"	walkable = false,\n" + 
				"	tiles = {\"overview.png\"},\n" + 
				"	wield_image =  \"overview.png\",\n" + 
				"	inventory_image =  \"overview.png\",\n" + 
				"	paramtype = \"light\",\n" + 
				"	paramtype2 = \"wallmounted\",\n" + 
				"	selection_box = {\n" + 
				"		type = \"wallmounted\",\n" + 
				"	},\n" + 
				"	groups = {oddly_breakable_by_hand = 3}, \n" + 
				"});\n" +
				"\n" +
				"minetest.register_lbm({\n" + 
				"	name = \"ignfab:overview_rotation\",\n" + 
				"	nodenames = {\"ignfab:overview\",\"ignfab:ignlogo\",\"ignfab:ignfablogo\"},\n" + 
				"	run_at_every_load = true,\n" + 
				"	action = function (pos,node)\n" + 
				"		minetest.swap_node({x="+ spawnX +", y="+(spawnY+3)+", z="+-spawnZ+"}, {name=\"ignfab:overview\",param2=4})\n" +
				"		minetest.swap_node({x="+ (spawnX+2) +", y="+(spawnY+4)+", z="+-spawnZ+"}, {name=\"ignfab:ignlogo\",param2=4})\n" +
				"		--minetest.swap_node({x="+ (spawnX+2) +", y="+(spawnY+2)+", z="+-spawnZ+"}, {name=\"ignfab:ignfablogo\",param2=4})\n" +
				"		--minetest.set_node(pos, {name=node.name,param1=0,param2=4})\n" + 
				"	end,\n" + 
				"})\n" +
				"\n" +
				"local data_buffer = {}\n"+
				"\n"; //"\n" +
		
		/*if(!plainUnderground) {
			ignfabBlocks += 
						"minetest.register_on_generated(function(minp,maxp,seed)\n" + 
						"	createUnderground(minp,maxp)\n" + 
						"end)\n" +
						"\n" +
						"function createUnderground(minp, maxp)\n" + 
						"	--[[if minp.y < 0 then\n" + 
						"		return\n" + 
						"	end]]\n" + 
						"\n" + 
						"		--local vm, emin, emax = minetest.get_mapgen_object\"voxelmanip\" \n" +
						"		local vm = minetest.get_voxel_manip()\n" + 
						"		local minp_loaded = {x = minp.x,y = -1,z = minp.z}\n" + 
						"		local emin, emax = vm:read_from_map(minp_loaded,maxp)\n" +
						"\n" + 
						"	local area = VoxelArea:new{MinEdge=emin, MaxEdge=emax}\n" + 
						"	emin, emax = vm:get_emerged_area()\n" + 
						"	local data = vm:get_data(data_buffer)\n" +
						"	\n" + 
						"	local c_stone = minetest.get_content_id\"default:stone\"\n" + 
						"	local c_underground = minetest.get_content_id\"default:cloud\"\n" + 
						" \n" + 
						"	for i in area:iter(\n" + 
						"		emin.x, emin.y, emin.z,\n" + 
						"		emax.x, emax.y, emax.z\n" +
						"	) do\n" + 
						"		if data[i] == c_underground then\n" + 
						"			--for n = area:position(i).y-1, math.max(0,minp.y), -1 do\n" + 
						"			for n = area:position(i).y, math.max(" + undergroundLimit + ",minp_loaded.y), -1 do\n" + 
						"					data[area:index(area:position(i).x, n, area:position(i).z)] = c_stone;\n" + 
						"			end\n" + 
						"\n" +
						"		end\n" + 
						"	end\n" + 
						" \n" + 
						"	vm:set_data(data)\n" + 
						"	vm:set_lighting{day=15, night=0}\n" + 
						"	vm:calc_lighting()\n" + 
						"	vm:write_to_map()\n" + 
						"end";
		}*/
		return ignfabBlocks;
	}
}
