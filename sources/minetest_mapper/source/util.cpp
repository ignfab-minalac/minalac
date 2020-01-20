#include "util.h"

inline std::string trim(const std::string &s)
{
  size_t front = 0;
  while(s[front] == ' '    ||
        s[front] == '\t'   ||
        s[front] == '\r'   ||
        s[front] == '\n'
       )
    ++front;

  size_t back = s.size();
  while(back > front &&
        (s[back-1] == ' '  ||
         s[back-1] == '\t' ||
         s[back-1] == '\r' ||
         s[back-1] == '\n'
        )
       )
    --back;

  return s.substr(front, back - front);
}

#define EOFCHECK() \
   if(is.eof()) \
    throw std::runtime_error(((std::string) "Setting '") + name + "' not found");

std::string get_setting(std::string name, std::istream &is)
{
  char c;
  char s[256];
  std::string nm, value;

  next:
  while((c = is.get()) == ' ' || c == '\t' || c == '\r' || c == '\n')
    ;
  EOFCHECK();
  if(c == '#') // Ignore comments
    is.ignore(0xffff, '\n');
  EOFCHECK();
  s[0] = c; // The current char belongs to the name too
  is.get(&s[1], 255, '=');
  is.ignore(1); // Jump over the =
  EOFCHECK();
  nm = trim(std::string(s));
  is.get(s, 256, '\n');
  value = trim(std::string(s));
  if(name == nm)
    return value;
  else
    goto next;
}

#undef EOFCHECK
