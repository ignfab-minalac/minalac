/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ignfab.minetest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class BlockSender {
	public static byte[] compress(byte[] bytes) {
        Deflater deflater = new Deflater();
        
        deflater.setInput(bytes);
          
        deflater.finish();
          
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
          
        byte[] buffer = new byte[1024];

        while(!deflater.finished()) {
       	 int bytesCompressed = deflater.deflate(buffer);
       	 bos.write(buffer,0,bytesCompressed);
        }
          
        try {
       	 bos.close();
        } catch(IOException e) {
       	 System.out.println("Erreur lors de la fermeture du flux : " + e); // � logger
        }
        
        byte[] compressedArray = bos.toByteArray();
        
        return compressedArray;
	}
}
