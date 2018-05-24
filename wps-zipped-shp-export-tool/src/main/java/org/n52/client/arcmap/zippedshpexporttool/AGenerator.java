/*
 * ﻿Copyright (C) 2013 - 2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.client.arcmap.zippedshpexporttool;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.geoprocessing.GeoProcessor;

/**
 * Abstract generator class that offers some convenience methods.
 *
 * @author Benjamin Pross
 *
 */
public abstract class AGenerator {

    protected GeoProcessor gp;

    public AGenerator() {
        try {
            gp = new GeoProcessor();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns output directory
     *
     * @return
     */
    public String getOutputDir() {
        return System.getProperty("java.io.tmpdir");

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
