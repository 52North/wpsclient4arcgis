/*
 * ﻿Copyright (C) 2013 - 2016 52°North Initiative for Geospatial Open Source
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.n52.wps.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.geoprocessing.tools.datamanagementtools.CopyFeatures;

/**
 * Generator class that creates a base64 encoded String and saves it to a file.
 *
 * @author Benjamin Pross
 *
 */
public class ShapefileGenerator extends AGenerator {

    private static Logger LOGGER = LoggerFactory.getLogger(ShapefileGenerator.class);

    public void generateBase64EncodedString(String layerName,
            String resultFilename) throws Exception {

        String outputDir = getOutputDir() + UUID.randomUUID().toString().substring(0, 5);

        try {
            new File(outputDir).mkdir();
        } catch (Exception e) {
            LOGGER.error("Could not create temp dir for shapefile export.", e);
        }

        String fileName = outputDir + File.separator + layerName + ".shp";
        /*
         * test whether a layer name was passed or a path
         */
        File f = new File(layerName);

        /*
         * layer name should return false here
         */
        if (f.exists()) {
            fileName = outputDir + File.separator + f.getName();
        }

        CopyFeatures copyFeatures = new CopyFeatures();
        copyFeatures.setInFeatures(layerName);

        copyFeatures.setOutFeatureClass(fileName);
        try {
            gp.setAddOutputsToMap(false);
            gp.execute(copyFeatures, null);
        } catch (Exception e) {
            LOGGER.error("Could not execute copyFeatures tool.", e);
        }

        File shp = new File(fileName);

        File resultFile = new File(resultFilename);

        File parent = resultFile.getParentFile();

        if (!parent.exists()) {
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
