/**
 * ﻿Copyright (C) 2013 - 2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.client.arcmap.zippedshpexporttool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.n52.wps.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.geoprocessing.tools.datamanagementtools.CopyFeatures;

public class ShapefileGenerator extends AGenerator{

	private static Logger LOGGER = LoggerFactory
			.getLogger(ShapefileGenerator.class);
	
	public void generateBase64EncodedString(String layerName, String resultFilename) throws Exception {

		outputDir = getOutputDir() + File.separator + "52n" + File.separator + "tmp";
		
		cleanAndRecreateDirectory(outputDir);			
		
		String fileName = outputDir + File.separator + layerName + ".shp";
		/*
		 * test whether a layer name was passed or a path
		 */
		File f = new File(layerName);
		
		/*
		 * layer name should return false here
		 */
		if(f.exists()){
			fileName = outputDir + File.separator + f.getName();
		}
		
		CopyFeatures copyFeatures = new CopyFeatures();
		copyFeatures.setInFeatures(layerName);
		
		copyFeatures.setOutFeatureClass(fileName);
		// Set the output Coordinate System environment
		try {
			gp.setAddOutputsToMap(false);
			gp.execute(copyFeatures, null);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		File shp = new File(fileName);
		
		File resultFile = new File(resultFilename);
		
		File parent = resultFile.getParentFile();
		
		if(!parent.exists()){
			parent.mkdirs();
		}
		
		// Zip the shapefile
		String path = shp.getAbsolutePath();
		String baseName = path.substring(0, path.length() - ".shp".length());
		File shx = new File(baseName + ".shx");
		File dbf = new File(baseName + ".dbf");
		File prj = new File(baseName + ".prj");
		try {
			File zipped = IOUtils.zip(shp, shx, dbf, prj);

			// Base64 encoding of the zipped file
			InputStream is = new FileInputStream(zipped);
			if (zipped.length() > Integer.MAX_VALUE) {
				is.close();
				throw new IOException("File is too large to process");
			}
			byte[] bytes = new byte[(int) zipped.length()];
			is.read(bytes);
			is.close();
			shx.delete();
			dbf.delete();
			prj.delete();
			shp.delete();
			
			String base64Zip = Base64.encodeBase64String(bytes);
			
			BufferedWriter bwr = new BufferedWriter(new FileWriter(resultFile));
			bwr.write(base64Zip);
			bwr.close();
                        zipped.delete();// TODO: might crash due to file permissions
			
		} catch (Exception e) {
			LOGGER.error("Exception while trying to create zipped shapefile: ", e);
			throw e;
		}	
	}

}
