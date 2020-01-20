/*
 * =====================================================================
 *        Version:  1.0
 *        Created:  25.08.2012 10:55:29
 *         Author:  Miroslav Bendík
 *        Company:  LinuxOS.sk
 * =====================================================================
 */

#ifndef PIXELATTRIBUTES_H_ADZ35GYF
#define PIXELATTRIBUTES_H_ADZ35GYF

#include <limits>
#include <stdint.h>
#include "config.h"

struct PixelAttribute {
	PixelAttribute(): height(std::numeric_limits<int>::min()), thickness(0) {};
	int height;
	uint8_t thickness;
	inline bool valid_height() {
		return height != std::numeric_limits<int>::min();
	}
};

class PixelAttributes
{
public:
	PixelAttributes();
	virtual ~PixelAttributes();
	void setWidth(int width);
	void scroll();
	inline PixelAttribute &attribute(int z, int x) { return m_pixelAttributes[z + 1][x + 1]; };

private:
	void freeAttributes();

private:
	enum Line {
		FirstLine = 0,
		LastLine = BLOCK_SIZE,
		EmptyLine = BLOCK_SIZE + 1,
		LineCount = BLOCK_SIZE + 2
	};
	PixelAttribute *m_pixelAttributes[BLOCK_SIZE + 2]; // 1px gradient + empty
	int m_width;
};

#endif /* end of include guard: PIXELATTRIBUTES_H_ADZ35GYF */

