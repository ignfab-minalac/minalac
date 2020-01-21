Minetest Mapper C++
===================

.. image:: https://travis-ci.org/minetest/minetestmapper.svg?branch=master
    :target: https://travis-ci.org/minetest/minetestmapper

A port of minetestmapper.py to C++ from https://github.com/minetest/minetest/tree/master/util.
This version is both faster and provides more features than the now deprecated Python script.

Requirements
------------

* libgd
* sqlite3
* leveldb (optional, set ENABLE_LEVELDB=1 in CMake to enable)
* hiredis (optional, set ENABLE_REDIS=1 in CMake to enable)
* Postgres libraries (optional, set ENABLE_POSTGRES=1 in CMake to enable)

e.g. on Debian:
^^^^^^^^^^^^^^^

	sudo apt-get install libgd-dev libsqlite3-dev libleveldb-dev libhiredis-dev libpq-dev

Windows
^^^^^^^
Minetestmapper for Windows can be downloaded here: https://github.com/minetest/minetestmapper/releases

After extracting the archive somewhere minetestmapper will be available from cmd.exe:
::

	cd C:\Users\yourname\Desktop\example\path
	minetestmapper.exe --help

Compilation
-----------

::

    cmake . -DENABLE_LEVELDB=1
    make -j2

Usage
-----

`minetestmapper` has two mandatory paremeters, `-i` (input world path)
and `-o` (output image path).

::

    ./minetestmapper -i ~/.minetest/worlds/my_world/ -o map.png


Parameters
^^^^^^^^^^

bgcolor:
    Background color of image, e.g. ``--bgcolor #ffffff``

scalecolor:
    Color of scale, e.g. ``--scalecolor #000000``

playercolor:
    Color of player indicators, e.g. ``--playercolor #ff0000``

origincolor:
    Color of origin indicator, e.g. ``--origincolor #ff0000``

drawscale:
    Draw tick marks, ``--drawscale``

drawplayers:
    Draw player indicators, ``--drawplayers``

draworigin:
    Draw origin indicator, ``--draworigin``

drawalpha:
    Allow nodes to be drawn with transparency, ``--drawalpha``

noshading:
    Don't draw shading on nodes, ``--noshading``

min-y:
    Don't draw nodes below this y value, e.g. ``--min-y -25``

max-y:
    Don't draw nodes above this y value, e.g. ``--max-y 75``

backend:
    Use specific map backend; supported: *sqlite3*, *leveldb*, *redis*, *postgresql*, e.g. ``--backend leveldb``

geometry:
    Limit area to specific geometry (*x:y+w+h* where x and y specify the lower left corner), e.g. ``--geometry -800:-800+1600+1600``

zoom:
    "Zoom" the image by using more than one pixel per node, e.g. ``--zoom 4``

colors:
    Forcefully set path to colors.txt file (it's autodetected otherwise), e.g. ``--colors ../minetest/mycolors.txt``

scales:
    Draw scales on specified image edges (letters *t b l r* meaning top, bottom, left and right), e.g. ``--scales tbr``
