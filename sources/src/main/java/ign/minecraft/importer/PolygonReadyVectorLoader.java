/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.geotools.geometry.jts.Geometries;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import ign.minecraft.MinecraftGenerationException;

abstract class PolygonReadyVectorLoader extends ColorTreatmentVectorLoader {
    public static final Color COLOR_NULL = toColor(0x00000000);
    
	protected static BufferedImage polygonImage = null;
	protected static Graphics2D polygonGraphics = null;
	protected static void initPolygonBuffer(int size, ColorTreatmentVectorLoader importerInstance) {
		if (polygonImage == null) {
			//create graphic buffer used to draw polygons before they are applied to the main graphic buffer
	        polygonImage = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
	        polygonGraphics = polygonImage.createGraphics();
	        //set custom composite to polygon graphics
	        // while the normal graphic buffer has no custom composite
	        polygonGraphics.setComposite(importerInstance.new CustomBlendComposite(importerInstance));
	        // custom composite in polygon graphic buffer (defined in this class)
			//  ensures that holes in polygon are erasing the polygon's first drawn full shape
	        // and normal composite in classic graphic buffer ensures polygon buffer is copied considering transparency,
	        // so holes won't erase what was previously there
		}
	}
	
	public PolygonReadyVectorLoader(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath, false);
		
		initPolygonBuffer(valueMapSize, this);
	}

	@Override
	public int blendColors( int srcPixelValueARGB, int dstPixelValueARGB) {
		//replace dest by source without any blending
		return srcPixelValueARGB;
	}

	@Override
	protected void treatGeometry(Geometry geometry) throws InvalidGridGeometryException, TransformException {
		Geometries geomType = Geometries.get(geometry);
		if (geomType == Geometries.POLYGON) {
	    	assert Polygon.class.isAssignableFrom(geometry.getClass());
	        drawPolygon((Polygon)geometry);
		} else {
			super.treatGeometry(geometry);
		}
	}
	
	protected void drawPolygon(Polygon polygon) throws InvalidGridGeometryException, TransformException {
		if (polygon.getNumInteriorRing() > 0) {
			//there is at least one hole in the polygon
			
			//get current color
			Color drawingColor = graphics.getColor();
			//reset polygon graphics
			polygonGraphics.setColor(COLOR_NULL);
			polygonGraphics.fillRect(0, 0, valueMapSize, valueMapSize);

			//switch the graphics to polygon graphics (for drawGeometry methods)
			Graphics2D normalGraphics = graphics;
			graphics = polygonGraphics;
			//draw exterior geometry normally as a polygon with current color
			graphics.setColor(drawingColor);
			drawGeometry( Geometries.POLYGON, polygon.getExteriorRing(), false, true);
			//then draw interior geometry in null color
			graphics.setColor(COLOR_NULL);
			for (int i = 0; i < polygon.getNumInteriorRing(); i++ ) {
				drawGeometry( Geometries.POLYGON, polygon.getInteriorRingN(i), true, true);
			}
			
			//set back normal graphics
			graphics = normalGraphics;

			//if bbox is flat, no use in going further
			if (currentBBox[0] == currentBBox[1] || currentBBox[2] == currentBBox[3]) {
				return;
			}
			//copy polygon into normal graphics, but only the zone containing the current polygon
			assert currentBBox[0] < currentBBox[1];
			assert currentBBox[2] < currentBBox[3];
			graphics.drawImage(polygonImage.getSubimage(currentBBox[0], currentBBox[2],
										currentBBox[1] - currentBBox[0] + 1, currentBBox[3] - currentBBox[2] + 1),
					currentBBox[0], currentBBox[2], null);
		} else {
			drawGeometry( Geometries.POLYGON, polygon.getExteriorRing(), false, false);
		}
	}

}
