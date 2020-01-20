/*
 * =====================================================================
 *        Version:  1.0
 *        Created:  18.09.2012 10:20:51
 *         Author:  Miroslav Bendík
 *        Company:  LinuxOS.sk
 * =====================================================================
 */

#ifndef ZLIBDECOMPRESSOR_H_ZQL1PN8Q
#define ZLIBDECOMPRESSOR_H_ZQL1PN8Q

#include <cstdlib>
#include <string>
#include "types.h"


class ZlibDecompressor
{
public:
	class DecompressError {
	};

	ZlibDecompressor(const unsigned char *data, std::size_t size);
	~ZlibDecompressor();
	void setSeekPos(std::size_t seekPos);
	std::size_t seekPos() const;
	ustring decompress();

private:
	const unsigned char *m_data;
	std::size_t m_seekPos;
	std::size_t m_size;
}; /* -----  end of class ZlibDecompressor  ----- */

#endif /* end of include guard: ZLIBDECOMPRESSOR_H_ZQL1PN8Q */

