#ifndef TILEGENERATOR_HEADER
#define TILEGENERATOR_HEADER

#include <gd.h>
#include <iosfwd>
#include <list>
#include <config.h>
#if __cplusplus >= 201103L
#include <unordered_map>
#include <unordered_set>
#else
#include <map>
#include <set>
#endif
#include <stdint.h>
#include <string>
#include "PixelAttributes.h"
#include "Image.h"
#include "db.h"
#include "types.h"

enum {
	SCALE_TOP = (1 << 0),
	SCALE_BOTTOM = (1 << 1),
	SCALE_LEFT = (1 << 2),
	SCALE_RIGHT = (1 << 3),
};

struct ColorEntry {
	ColorEntry(): r(0), g(0), b(0), a(0), t(0) {};
	ColorEntry(uint8_t r, uint8_t g, uint8_t b, uint8_t a, uint8_t t): r(r), g(g), b(b), a(a), t(t) {};
	inline Color to_color() const { return Color(r, g, b, a); }
	uint8_t r;
	uint8_t g;
	uint8_t b;
	uint8_t a;
	uint8_t t;
};


class TileGenerator
{
private:
#if __cplusplus >= 201103L
	typedef std::unordered_map<std::string, ColorEntry> ColorMap;
	typedef std::unordered_map<int, std::string> NameMap;
	typedef std::unordered_set<std::string> NameSet;
#else
	typedef std::map<std::string, ColorEntry> ColorMap;
	typedef std::map<int, std::string> NameMap;
	typedef std::set<std::string> NameSet;
#endif

public:
	TileGenerator();
	~TileGenerator();
	void setBgColor(const std::string &bgColor);
	void setScaleColor(const std::string &scaleColor);
	void setOriginColor(const std::string &originColor);
	void setPlayerColor(const std::string &playerColor); Color parseColor(const std::string &color);
	void setDrawOrigin(bool drawOrigin);
	void setDrawPlayers(bool drawPlayers);
	void setDrawScale(bool drawScale);
	void setDrawAlpha(bool drawAlpha);
	void setShading(bool shading);
	void setGeometry(int x, int y, int w, int h);
	void setMinY(int y);
	void setMaxY(int y);
	void parseColorsFile(const std::string &fileName);
	void setBackend(std::string backend);
	void generate(const std::string &input, const std::string &output);
	void setZoom(int zoom);
	void setScales(uint flags);

private:
	void parseColorsStream(std::istream &in);
	void openDb(const std::string &input);
	void closeDatabase();
	void loadBlocks();
	void createImage();
	void renderMap();
	std::list<int> getZValueList() const;
	void renderMapBlock(const ustring &mapBlock, const BlockPos &pos, int version);
	void renderMapBlockBottom(const BlockPos &pos);
	void renderShading(int zPos);
	void renderScale();
	void renderOrigin();
	void renderPlayers(const std::string &inputPath);
	void writeImage(const std::string &output);
	void printUnknown();
	int getImageX(int val) const;
	int getImageY(int val) const;
	void setZoomed(int x, int y, Color color);

private:
	Color m_bgColor;
	Color m_scaleColor;
	Color m_originColor;
	Color m_playerColor;
	bool m_drawOrigin;
	bool m_drawPlayers;
	bool m_drawScale;
	bool m_drawAlpha;
	bool m_shading;
	std::string m_backend;
	int m_xBorder, m_yBorder;

	DB *m_db;
	Image *m_image;
	PixelAttributes m_blockPixelAttributes;
	int m_xMin;
	int m_xMax;
	int m_zMin;
	int m_zMax;
	int m_yMin;
	int m_yMax;
	int m_geomX;
	int m_geomY;
	int m_geomX2;
	int m_geomY2;
	int m_mapWidth;
	int m_mapHeight;
	std::list<std::pair<int, int> > m_positions;
	NameMap m_nameMap;
	ColorMap m_colorMap;
	uint16_t m_readPixels[16];
	uint16_t m_readInfo[16];
	NameSet m_unknownNodes;
	Color m_color[16][16];
	uint8_t m_thickness[16][16];

	int m_blockAirId;
	int m_blockIgnoreId;
	int m_zoom;
	uint m_scales;
}; // class TileGenerator

#endif // TILEGENERATOR_HEADER
