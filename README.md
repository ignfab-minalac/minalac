# Minecraft® à la carte

[![GitHub Logo](/images/logo.png)](http://ign.fr/minecraft)

Generate sandbox games compatible maps with geo data from IGN (Institut national de l'information géographique et forestière).
[ign.fr/minecraft](http://ign.fr/minecraft)

## Project Support

Part of the developments concerning Minecraft® à la carte is funded and supported by the French Education Ministry, through the "Programme d'investissements d'avenir" (PIA).

[![Ministère de l'Éducation nationale](/images/men.jpg)](https://www.education.gouv.fr/)
[![Edutheque](/images/edutheque.png)](https://www.edutheque.fr/)
[![PIA](/images/investirlavenir.png)](https://www.gouvernement.fr/le-programme-d-investissements-d-avenir)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

A system with Ubuntu 16.04 or Debian 9 or equivalent

### Installing

Install the following packages :
```
apt-get install libgeos-dev libproj-dev libsqlite3-mod-spatialite xvfb software-properties-common
```

Add the following repository (or newer):
```
add apt-repository ppa:beineri/opt/qt-5.10.1-trusty
```

Install QT510 (or newer) :
```
apt-get install qt510-meta-full
```

If your version isn't QT510 (replace {VERSION}):
```
ln -s /opt/qt{VERSION} /opt/qt510
```
```
cp /opt/qt510/bin/qt{VERSION}-env.sh /opt/qt510/bin/qt510-env.sh
```

Do a 755 chmod on the following executables :
```
chmod 755 sources/minetest_mapper/minetest_mapper
chmod 755 sources/minetest_engine/mcconvert
chmod 755 sources/minecraft_mapper/minutor
chmod 755 sources/minecraft_mapper/launch_mapping.sh
```

### Engine compilation

Please refer to "documentation_minalac_public.pdf" (from page 3) for detailed pictures.
You'll also need to specify a Geoportail API key, with access to layers, in config.properties.

### Launching engine

Copy the entire "sources" directory into a new directory named "minalac_engine".
Create file "logging.properties.1" and populate it with :
```
handlers= java.util.logging.FileHandler
java.util.logging.ConsoleHandler.level = SEVERE
java.util.logging.FileHandler.level = INFO
java.util.logging.FileHandler.pattern = 1.xml
```

Create an empty directory with the name "1".

For the engine to run properly, you should have the following file tree :
```
./1/ (empty directory)
./minalac_engine/logging.properties.1
./minalac_engine/sources/*
./minalac_engine/sources/minecraft_mapper/*
./minalac_engine/sources/minetest_mapper/*
./minalac_engine/sources/minetest_engine/*
./minalac_engine/sources/resources/*
./minalac_engine/sources/target/*
./minalac_engine/sources/lib/*
./minalac_engine/sources/config.properties
./minalac_engine/sources/logging.properties
```

The following command launches the engine (from "minalac_engine" folder) :
```
java -jar -Xmx5g -Djava.util.logging.config.file=logging.properties.1 ./sources/target/minecraftmap-0.0.1-SNAPSHOT.jar {plainUnderground} {noBorder} {snow} "../1/minecraft_alac" "{mapName}" 6.33488 45.764334 ./sources/resources/ {format} {ratio} {altitudeRatio} {mapsize} {orientation} {themes} {snowMinHeight} {snowMaxHeight}
```
* Underground: replace by "--plainUnderground" for no underground (nothing otherwise)
* Noborder: replace by "--noborder" for no map border (nothing otherwise)
* Snow: replace by "--snow" for snow map (nothing otherwise)
* Mapname: replace by your map name (will be showed ingame)
* Format: map format, choose between:
    * minecraft
    * bedrock
    * edu
    * minetest
* Ratio: ratio x & y axis, from 1 to 2 with 0.1 step
* Altitude ratio: z axis exaggeration, 1 to 5, to be used in relief mode
* Map size: km size multiplied by 2 (put 5 for 2.5km)
* Orientation: Angle between -90 and 90 degrees
* Themes: Separated by commas
    * 1: altitude (mandatory)
    * 2: hydrography
    * 3: land use
    * 4: roads
    * 5: buildings
    * 6: building traces
    * 7: hypsometric layer (only in relief mode)
* Snow min height: Only if snow is activated. Minimum height of snow (between 0 and 5 meters).
* Snow max height: Only if snow is activated. Maximum height of snow (between 1 and 5 meters).

Beware : You need to be within the minimum and maximum values of each attribute, otherwise the generation won't start.

## License
This project is licensed under GPLv3.

## Contact and help
For any questions about this project, please write us at **minecraft[at]ign.fr**