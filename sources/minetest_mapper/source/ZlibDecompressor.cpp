/*
 * =====================================================================
 *        Version:  1.0
 *        Created:  18.09.2012 10:20:47
 *         Author:  Miroslav Bendík
 *        Company:  LinuxOS.sk
 * =====================================================================
 */

#include <zlib.h>
#include <stdint.h>
#include "ZlibDecompressor.h"

ZlibDecompressor::ZlibDecompressor(const unsigned char *data, std::size_t size):
	m_data(data),
	m_seekPos(0),
	m_size(size)
{
}

ZlibDecompressor::~ZlibDecompressor()
{
}

void ZlibDecompressor::setSeekPos(std::size_t seekPos)
{
	m_seekPos = seekPos;
}

std::size_t ZlibDecompressor::seekPos() const
{
	return m_seekPos;
}

ustring ZlibDecompressor::decompress()
{
	const unsigned char *data = m_data + m_seekPos;
	const std::size_t size = m_size - m_seekPos;

	ustring buffer;
	const size_t BUFSIZE = 128 * 1024;
	uint8_t temp_buffer[BUFSIZE];

	z_stream strm;
	strm.zalloc = Z_NULL;
	strm.zfree = Z_NULL;
	strm.opaque = Z_NULL;
	strm.next_in = Z_NULL;
	strm.avail_in = size;

	if (inflateInit(&strm) != Z_OK) {
		throw DecompressError();
	}

	strm.next_in = const_cast<unsigned char *>(data);
	int ret = 0;
	do {
		strm.avail_out = BUFSIZE;
		strm.next_out = temp_buffer;
		ret = inflate(&strm, Z_NO_FLUSH);
		buffer += ustring(reinterpret_cast<unsigned char *>(temp_buffer), BUFSIZE - strm.avail_out);
	} while (ret == Z_OK);
	if (ret != Z_STREAM_END) {
		throw DecompressError();
	}
	m_seekPos += strm.next_in - data;
	(void)inflateEnd(&strm);

	return buffer;
}

