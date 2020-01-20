#include <cstdio>
#include <cerrno>
#include <cstring>
#include <iostream>
#include <sstream>
#include <stdexcept>
#include <gd.h>
#include <gdfontmb.h>

#include "Image.h"

#ifndef NDEBUG
#define SIZECHECK(x, y) do { \
		if((x) < 0 || (x) >= m_width) \
			throw std::out_of_range("sizecheck x"); \
		if((y) < 0 || (y) >= m_height) \
			throw std::out_of_range("sizecheck y"); \
	} while(0)
#else
#define SIZECHECK(x, y) do {} while(0)
#endif

// ARGB but with inverted alpha

static inline int color2int(Color c)
{
	u8 a = 255 - c.a;
	return (a << 24) | (c.r << 16) | (c.g << 8) | c.b;
}

static inline Color int2color(int c)
{
	Color c2;
	u8 a;
	c2.b = c & 0xff;
	c2.g = (c >> 8) & 0xff;
	c2.r = (c >> 16) & 0xff;
	a = (c >> 24) & 0xff;
	c2.a = 255 - a;
	return c2;
}


Image::Image(int width, int height) :
	m_width(width), m_height(height), m_image(NULL)
{
	m_image = gdImageCreateTrueColor(m_width, m_height);
}

Image::~Image()
{
	gdImageDestroy(m_image);
}

void Image::setPixel(int x, int y, const Color &c)
{
	SIZECHECK(x, y);
	m_image->tpixels[y][x] = color2int(c);
}

Color Image::getPixel(int x, int y)
{
#ifndef NDEBUG
	if(x < 0 || x > m_width || y < 0 || y > m_height)
		throw std::out_of_range("sizecheck");
#endif
	return int2color(m_image->tpixels[y][x]);
}

void Image::drawLine(int x1, int y1, int x2, int y2, const Color &c)
{
	SIZECHECK(x1, y1);
	SIZECHECK(x2, y2);
	gdImageLine(m_image, x1, y1, x2, y2, color2int(c));
}

void Image::drawText(int x, int y, const std::string &s, const Color &c)
{
	SIZECHECK(x, y);
	gdImageString(m_image, gdFontGetMediumBold(), x, y, (unsigned char*) s.c_str(), color2int(c));
}

void Image::drawFilledRect(int x, int y, int w, int h, const Color &c)
{
	SIZECHECK(x, y);
	SIZECHECK(x + w - 1, y + h - 1);
	gdImageFilledRectangle(m_image, x, y, x + w - 1, y + h - 1, color2int(c));
}

void Image::drawCircle(int x, int y, int diameter, const Color &c)
{
	SIZECHECK(x, y);
	gdImageArc(m_image, x, y, diameter, diameter, 0, 360, color2int(c));
}

void Image::save(const std::string &filename)
{
	FILE *f = fopen(filename.c_str(), "wb");
	if(!f) {
		std::ostringstream oss;
		oss << "Error writing image file: " << std::strerror(errno);
		throw std::runtime_error(oss.str());
	}
	gdImagePng(m_image, f); // other formats?
	fclose(f);
}
