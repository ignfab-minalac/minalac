/*
 * =====================================================================
 *        Version:  1.0
 *        Created:  01.09.2012 14:38:08
 *         Author:  Miroslav Bendík
 *        Company:  LinuxOS.sk
 * =====================================================================
 */

#ifndef PLAYERATTRIBUTES_H_D7THWFVV
#define PLAYERATTRIBUTES_H_D7THWFVV

#include <list>
#include <string>

struct Player
{
	std::string name;
	double x;
	double y;
	double z;
}; /* -----  end of struct Player  ----- */

class PlayerAttributes
{
public:
	typedef std::list<Player> Players;

	PlayerAttributes(const std::string &sourceDirectory);
	Players::iterator begin();
	Players::iterator end();

private:
	Players m_players;
}; /* -----  end of class PlayerAttributes  ----- */

#endif /* end of include guard: PLAYERATTRIBUTES_H_D7THWFVV */

