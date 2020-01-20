/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft;

public class MinecraftGenerationException extends Exception {
	
	public enum Definition {
		MINECRAFTEXPORT_GLOBAL_INVALIDGENERATIONPATH(1,"specified generation path is invalid"),
		MINECRAFTEXPORT_GLOBAL_NOUNDERGOUNDDATA(2,"underground data could not be found"),
		SERVICEIMPORT_GLOBAL_ERROR(3,"couldn't import all layers"),
		MINECRAFTEXPORT_GLOBAL_ERROR(4,"couldn't transform the data into a minecraft map file"),
		SERVICEIMPORT_UNSUPPORTED_ZONE(5,"unsupported zone of generation"),
		SERVICEIMPORT_CONNECTION_ERROR(6,"couldn't connect to service"),
		SERVICEIMPORT_SHAPEFILE_ERROR(7,"shapefile missing"),
		SERVICEIMPORT_UNEXPECTEDSIZE_ERROR(8,"data received from a service has an unexpected size (might be an html error message)"),
		SERVICEIMPORT_RAWDATA_ERROR(9,"data received from a service could not be treated (unknown format)"),
		SERVICEIMPORT_MEMORY_ERROR(10,"memory IO error while receiving data from a service"),
		SERVICEIMPORT_WMSUNEXPECTEDSIZE_ERROR(11,"data received from a WMS service has a smaller size than expected"),
		SERVICEIMPORT_DEBUGIMAGEDUMP_ERROR(12,"IO error while dumping image from treated service data for debug purposes"),
		ROADIMPORT_TOOMANYROADS_ERROR(13,"The maximum number of possible roads has been reached"),
		MINECRAFTEXPORT_MAPITEM_MISSINGIMAGEFILE(14,"cannot find the image for a map item"),
		MINECRAFTEXPORT_MAPITEM_BADIMAGEFILE(15,"provided image for a map item has an unsupported format")
		;
		
		public final int id;
		public final String message;
		
		Definition(int id, String message) {
			this.id = id;
			this.message = message;
		}
	}

	private static final long serialVersionUID = -6416416855513086154L;
	
	private final int id;

	public MinecraftGenerationException(Definition definition) {
		this(definition, null);
	}

	public MinecraftGenerationException(Definition definition, Throwable cause) {
		super(definition.message, cause);
		this.id = definition.id;
	}
	
	public int getId() {
		return id;
	}
	
	public MinecraftGenerationException getSourceException() {
		if (MinecraftGenerationException.class.isAssignableFrom(getCause().getClass())) {
			return ((MinecraftGenerationException) getCause()).getSourceException();
		}
		return this;
	}
}
