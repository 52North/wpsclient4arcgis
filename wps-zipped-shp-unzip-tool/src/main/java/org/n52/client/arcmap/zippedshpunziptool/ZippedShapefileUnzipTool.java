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
package org.n52.client.arcmap.zippedshpunziptool;

import static org.apache.commons.io.IOUtils.copyLarge;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.datasourcesfile.DEFile;
import com.esri.arcgis.datasourcesfile.DEFileType;
import com.esri.arcgis.geodatabase.IGPMessages;
import com.esri.arcgis.geodatabase.IGPValue;
import com.esri.arcgis.geodatabase.esriGPMessageSeverity;
import com.esri.arcgis.geoprocessing.BaseGeoprocessingTool;
import com.esri.arcgis.geoprocessing.GPParameter;
import com.esri.arcgis.geoprocessing.GPString;
import com.esri.arcgis.geoprocessing.GPStringType;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.IGPEnvironmentManager;
import com.esri.arcgis.geoprocessing.IGPParameter;
import com.esri.arcgis.geoprocessing.esriGPParameterDirection;
import com.esri.arcgis.geoprocessing.esriGPParameterType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.Array;
import com.esri.arcgis.system.IArray;
import com.esri.arcgis.system.IName;
import com.esri.arcgis.system.ITrackCancel;

/**
 * This class represents a ArcGIS geoprocessing tool that unzips
 * shapefiles. The shapefiles can be encoded as base64.
 *
 * @author Benjamin Pross
 *
 */
public class ZippedShapefileUnzipTool extends BaseGeoprocessingTool {

    /**
     *
     */
    private static final long serialVersionUID = 2796504233317967922L;

    private static Logger LOGGER = LoggerFactory.getLogger(ZippedShapefileUnzipTool.class);

    private final String inputName = "in_zipped_shapefile";

    private final String encodingName = "in_encoding";

    private final String resultName = "out_shapefile";

    public static final String displayName = "Zipped Shapefile Unzip Tool";

    public static final String toolName = "ZippedShapefileUnzipTool";

    private String metadataFileName = toolName + ".xml";

    private GeoProcessor gp = null;

    public ZippedShapefileUnzipTool() {
        try {
            /*
             * Initialize Geoprocessor. The Geoprocessor object represents the
             * Geoprocessing Framework and will be used to execute the system GP
             * tool.
             */
            gp = new GeoProcessor();
            gp.setOverwriteOutput(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns name of the tool This name appears when executing the tool at the
     * command line or in scripting. This name should be unique to each toolbox
     * and must not contain spaces.
     */
    public String getName() throws IOException, AutomationException {
        return toolName;
    }

    /**
     * Returns Display Name of the tool, as seen in ArcToolbox.
     */
    public String getDisplayName() throws IOException, AutomationException {
        return displayName;
    }

    /**
     * Returns the full name of the tool
     */
    public IName getFullName() throws IOException, AutomationException {
        return (IName) new ZippedShapefileUnzipFunctionFactory().getFunctionName(toolName);
    }

    /**
     * Returns an array of paramInfo This is the location where the parameters
     * to the Function Tool are defined. This property returns an IArray of
     * parameter objects (IGPParameter). These objects define the
     * characteristics of the input and output parameters.
     */
    public IArray getParameterInfo() throws IOException, AutomationException {
        IArray parameters = new Array();

        GPParameter fileNameParameter = new GPParameter();
        fileNameParameter.setName(inputName);
        fileNameParameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
        fileNameParameter.setDisplayName(inputName);
        fileNameParameter.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
        fileNameParameter.setDataTypeByRef(new DEFileType());
        fileNameParameter.setValueByRef(new GPString());
        parameters.add(fileNameParameter);

        GPParameter encodingParameter = new GPParameter();
        encodingParameter.setName(encodingName);
        encodingParameter.setDisplayName(encodingName);
        encodingParameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
        encodingParameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
        encodingParameter.setDataTypeByRef(new GPStringType());
        encodingParameter.setValueByRef(new GPString());
        parameters.add(encodingParameter);

        GPParameter resultParameter = new GPParameter();
        resultParameter.setName(resultName);
        resultParameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
        resultParameter.setDisplayName(resultName);
        resultParameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
        resultParameter.setDataTypeByRef(new DEFileType());

        DEFile file = new DEFile();

        String proposedFilePath = System.getenv("TMP") + resultName + UUID.randomUUID().toString().substring(0, 5) + ".shp";

        file.setAsText(proposedFilePath);

        resultParameter.setValueByRef(file);

        parameters.add(resultParameter);

        return parameters;
    }

    /**
     * Called each time the user changes a parameter in the tool dialog or
     * Command Line. This updates the output data of the tool, which extremely
     * useful for building models. After returning from UpdateParameters(), the
     * GP framework calls its internal validation routine to check that a given
     * set of parameter values are of the appropriate number, DataType, and
     * value.
     */
    public void updateParameters(IArray paramvalues,
            IGPEnvironmentManager envMgr) {

    }

    /**
     * Called after returning from the internal validation routine. You can
     * examine the messages created from internal validation and change them if
     * desired.
     */
    public void updateMessages(IArray paramvalues,
            IGPEnvironmentManager envMgr,
            IGPMessages gpMessages) {

    }

    private Map<String, String> getParameterNameValueMap(IArray paramvalues) {
        try {
            Map<String, String> result = new HashMap<String, String>(paramvalues.getCount());

            for (int i = 0; i < paramvalues.getCount(); i++) {
                IGPParameter tmpParameter = (IGPParameter) paramvalues.getElement(i);

                IGPValue tmpParameterValue = null;

                try {
                    tmpParameterValue = gpUtilities.unpackGPValue(tmpParameter);
                } catch (Exception e) {
                    LOGGER.error("Error unpacking value " + tmpParameter, e);
                }

                if (tmpParameterValue != null && !tmpParameterValue.getAsText().equals("")) {
                    LOGGER.info("added " + tmpParameter.getName());
                    result.put(tmpParameter.getName(), tmpParameterValue.getAsText());
                } else {
                    LOGGER.info("Omitted " + tmpParameter.getName());
                    LOGGER.info("Value: " + tmpParameterValue);
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("Error unpacking value", e);
        }
        return null;
    }

    /**
     * Executes the tool
     */
    public void execute(IArray paramvalues,
            ITrackCancel trackcancel,
            IGPEnvironmentManager envMgr,
            IGPMessages messages) throws IOException, AutomationException {

        Map<String, String> parameterNameValueMap = getParameterNameValueMap(paramvalues);

        String outputPath = "";
        String inputPath = "";

        String encoding = "";

        if (parameterNameValueMap.get(inputName) != null) {
            inputPath = parameterNameValueMap.get(inputName);
        }

        LOGGER.debug("inputpath " + inputPath);
        messages.addMessage("Reading from file: " + inputPath);

        if (parameterNameValueMap.get(encodingName) != null) {
            encoding = parameterNameValueMap.get(encodingName);
        }

        /*
         * there should be only one
         */
        if (parameterNameValueMap.get(resultName) != null) {
            outputPath = parameterNameValueMap.get(resultName);
        }

        messages.addMessage(outputPath);

        File outputFile = new File(outputPath);

        File inputFile = new File(inputPath);

        InputStream in = new FileInputStream(inputFile);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        if (encoding.equalsIgnoreCase("base64")) {
            copyLarge(new Base64InputStream(in), byteOut);
        } else {
            copyLarge(in, byteOut);
        }

        if (outputFile.getName().endsWith(".shp")) {

            String tmpDir = System.getProperty("java.io.tmpdir");

            LOGGER.debug("Tmp dir " + tmpDir);

            File tmpZipFile = new File(tmpDir + File.separator + "out" + File.separator + "output.zip");
            LOGGER.debug("Output ParentFile " + tmpZipFile.getParent());
            try {
                tmpZipFile.getParentFile().mkdirs();
            } catch (Exception e) {
                messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Something went wrong while trying to create temporary zip file:" + tmpZipFile.getAbsolutePath());
                LOGGER.debug("Exception " + e);
            }
            byteOut.writeTo(new FileOutputStream(tmpZipFile));

            byteOut.flush();

            byteOut.close();
            /*
             * path we got from the textfield we need to extract the zipped
             * files with the name of the output file
             */
            String shpFileName = outputFile.getName();

            String entryName = shpFileName.substring(0, shpFileName.indexOf("."));

            final int BUFFER = 2048;

            String path = outputFile.getParent();

            try {
                BufferedOutputStream dest = null;
                FileInputStream fis = new FileInputStream(tmpZipFile);
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {

                    String entryFileEnding = entry.getName();

                    entryFileEnding = entryFileEnding.substring(entryFileEnding.indexOf("."), entryFileEnding.length());

                    String entryOutputName = entryName + entryFileEnding;

                    int count;
                    byte data[] = new byte[BUFFER];
                    // write the files to the disk
                    FileOutputStream fos = new FileOutputStream(path + File.separator + entryOutputName);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
                zis.close();
            } catch (Exception e) {
                LOGGER.error("Could not unzip file.", e);
                messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Could not unzip file.");
            }
        }
    }

    /**
     * Returns metadata file
     */
    public String getMetadataFile() throws IOException, AutomationException {
        return metadataFileName;
    }

    /**
     * Returns status of license
     */
    public boolean isLicensed() throws IOException, AutomationException {
        return true;
    }
}
