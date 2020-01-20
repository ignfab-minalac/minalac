
local S
if minetest.get_modpath("intllib") then
	S = intllib.Getter()
else
	S = function(s) return s end
end

local colors = {
	"White", "Black", "Light Blue", "Green", "Red",
	"Light Gray", "Purple", "Lime", "Magenta", "Orange",
	"Brown", "Blue", "Yellow", "Pink", "Cyan", "Gray"
}

minetest.register_node("hardenedclay:hardened_clay", {
	description = S("Hardened Clay"),
	tiles = {"hardenedclay_regular.png"},
	groups = {cracky=3},
})

minetest.register_craft({
	type = "cooking",
	output = "hardenedclay:hardened_clay",
	recipe = "default:clay",
})

for _, color in pairs(colors) do
	local nodecolor = color:lower():gsub(" ", "_")
	minetest.register_node("hardenedclay:hardened_clay_"..nodecolor, {
		description = S("%s Hardened Clay"):format(S(color)),
		tiles = {"hardenedclay_"..nodecolor..".png"},
		groups = {cracky=3},
	})
	minetest.register_craft({
		type = "shapeless",
		output = 'hardenedclay:hardened_clay_'..nodecolor,
		recipe = {
			'dye:'..nodecolor, 'hardenedclay:hardened_clay',
		}
	})
	minetest.register_alias("hardenedclay:"..nodecolor.."_hardened_clay",
			"hardenedclay:hardened_clay_"..nodecolor)
end

