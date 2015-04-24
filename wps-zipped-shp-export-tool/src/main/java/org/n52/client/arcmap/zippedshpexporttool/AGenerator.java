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

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.geoprocessing.GeoProcessor;

public abstract class AGenerator {

    protected String outputDir;

    protected GeoProcessor gp;

    public AGenerator() {
        try {
            gp = new GeoProcessor();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputDir = getOutputDir() + File.separator + "tmp";
        new File(outputDir).mkdirs();

    }

    /**
     * Returns output directory
     * 
     * @return
     */
    public String getOutputDir() {
        String outputDir = System.getenv("AppData") + File.separator + "52North" + File.separator + "WPS ArcMap Client";
        System.out.println("Creating output directory - " + outputDir);
        new File(outputDir).mkdirs();
        return outputDir;

    }

    /**
     * Empties specified directory of all files, deletes and re-creates it
     * 
     * @param dirName
     *            String
     */
    public void cleanAndRecreateDirectory(String dirName) {
        cleanAndDeleteDirectory(dirName);
        File dir = new File(dirName);
        dir.mkdirs();
    }

    /**
     * Deletes all files in specified directory and then deletes the directory
     * as well
     * 
     * @param Path
     *            String
     */
    public void cleanAndDeleteDirectory(String Path) {

        File src = new File(Path);
        if (src.isDirectory() && src.exists()) {
            File list[] = src.listFiles();
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    cleanAndDeleteDirectory(list[i].getPath());
                    list[i].delete();
                } else {
                    list[i].delete();
                }
            }
            src.delete();
        } else {
            src.delete();
        }
    }

}
