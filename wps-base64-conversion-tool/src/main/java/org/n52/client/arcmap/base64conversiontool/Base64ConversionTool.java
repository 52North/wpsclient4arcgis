/*
 * ﻿Copyright (C) 2013 - 2020 52°North Initiative for Geospatial Open Source
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
package org.n52.client.arcmap.base64conversiontool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.datasourcesfile.DEFile;
import com.esri.arcgis.datasourcesfile.DEFileType;
import com.esri.arcgis.datasourcesfile.DELayerType;
import com.esri.arcgis.datasourcesfile.DETextFileType;
import com.esri.arcgis.datasourcesfile.IGPLayer;
import com.esri.arcgis.geodatabase.DEFeatureClassType;
import com.esri.arcgis.geodatabase.DERasterBandType;
import com.esri.arcgis.geodatabase.DERasterDatasetType;
import com.esri.arcgis.geodatabase.IDataset;
import com.esri.arcgis.geodatabase.IGPMessages;
import com.esri.arcgis.geodatabase.IGPValue;
import com.esri.arcgis.geodatabase.esriGPMessageSeverity;
import com.esri.arcgis.geoprocessing.BaseGeoprocessingTool;
import com.esri.arcgis.geoprocessing.GPCompositeDataType;
import com.esri.arcgis.geoprocessing.GPDataFileType;
import com.esri.arcgis.geoprocessing.GPFeatureLayerType;
import com.esri.arcgis.geoprocessing.GPFeatureRecordSetLayerType;
import com.esri.arcgis.geoprocessing.GPLayerType;
import com.esri.arcgis.geoprocessing.GPParameter;
import com.esri.arcgis.geoprocessing.GPRasterDataLayerType;
import com.esri.arcgis.geoprocessing.GPRasterLayerType;
import com.esri.arcgis.geoprocessing.GPStringType;
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
 * This class represents a ArcGIS geoprocessing tool that en-/decodes files in
 * base64.
 *
 * @author Benjamin Pross
 *
 */
public class Base64ConversionTool extends BaseGeoprocessingTool {

    /**
     *
     */
    private static final long serialVersionUID = -8229199587241414416L;

    private static Logger LOGGER = LoggerFactory.getLogger(Base64ConversionTool.class);

    private final String inputID = "in_file";

    private final String outputID = "out_file";

    private final String randomFileString = "RANDOM_FILE";

    public static final String toolName = "Base64ConversionTool";

    public static final String displayName = "Base64 Conversion Tool";

    private String metadataFileName = toolName + ".xml";

    public Base64ConversionTool() {

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
        return (IName) new Base64ConversionFactory().getFunctionName(toolName);
    }

    /**
     * Returns an array of paramInfo This is the location where the parameters
     * to the Function Tool are defined. This property returns an IArray of
     * parameter objects (IGPParameter). These objects define the
     * characteristics of the input and output parameters.
     */
    public IArray getParameterInfo() throws IOException, AutomationException {
        IArray parameters = new Array();

        GPParameter inputParameter = new GPParameter();

        GPCompositeDataType composite = new GPCompositeDataType();
        composite.addDataType(new DERasterBandType());
        composite.addDataType(new DERasterDatasetType());
        composite.addDataType(new GPRasterLayerType());
        composite.addDataType(new GPRasterDataLayerType());
        composite.addDataType(new GPStringType());
        composite.addDataType(new GPFeatureLayerType());
        composite.addDataType(new DEFeatureClassType());
        composite.addDataType(new GPLayerType());
        composite.addDataType(new DELayerType());
        composite.addDataType(new GPFeatureRecordSetLayerType());
        composite.addDataType(new DETextFileType());
        composite.addDataType(new GPDataFileType());
        composite.addDataType(new DEFileType());

        inputParameter.setName(inputID);
        inputParameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
        inputParameter.setDisplayName("Input file");
        inputParameter.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
        inputParameter.setDataTypeByRef(composite);
        // parameter4.setValueByRef(new GPString());
        parameters.add(inputParameter);

        GPParameter outputParameter = new GPParameter();
        outputParameter.setName(outputID);
        outputParameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
        outputParameter.setDisplayName("Output file");
        outputParameter.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
        outputParameter.setDataTypeByRef(new DEFileType());

        DEFile file = new DEFile();

        String tmpFilePath = System.getenv("TMP") + "base64conversion" + UUID.randomUUID().toString().substring(0, 5) + ".tmp";

        file.setAsText(tmpFilePath);

        outputParameter.setValueByRef(file);

        parameters.add(outputParameter);

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

                    String value = tmpParameterValue.getAsText();

                    if (tmpParameterValue instanceof IGPLayer) {
                        ILayer layer = gpUtilities.findMapLayer(value);
                        if (layer instanceof IDataset) {
                            value = ((IDataset) layer).getWorkspace().getPathName() + File.separator + value;
                        }
                    }
                    result.put(tmpParameter.getName(), value);
                    LOGGER.info("added " + tmpParameter.getName());
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

        if (parameterNameValueMap.get(inputID) != null) {
            inputPath = parameterNameValueMap.get(inputID);
        }

        LOGGER.debug("inputpath " + inputPath);

        messages.addMessage("inputpath " + inputPath);

        File inputFile = new File(inputPath);

        File outputFile = null;
        // /*
        // * there should be only one
        // */
        if (parameterNameValueMap.get(outputID) != null) {
            outputPath = parameterNameValueMap.get(outputID);

            messages.addMessage("Outputting to: " + outputPath);
            /*
             * if this is RANDOM_FILE generate a temp file
             */
            if (!outputPath.equals(randomFileString)) {
                outputFile = new File(outputPath);
            }
        }

        try {
            treatBase64(inputFile, outputFile, messages, paramvalues);
        } catch (Exception e) {
            LOGGER.error("Something went wrong while de-/encoding file in base64", e);
            messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Something went wrong while de-/encoding file in base64");
        }

        messages.addMessage("success");
    }

    /**
     * This method encodes/decodes files in/from base64 depending on the input
     * file.
     *
     * @param inputFile
     *            The input file decoded in base64 or not
     * @param outputFile
     *            The output file decoded in base64 or not depending on the
     *            input file
     * @throws Exception
     *             If something goes wrong while de-/encoding
     */
    public void treatBase64(File inputFile,
            File outputFile,
            IGPMessages messages,
            IArray paramvalues) throws Exception {

        InputStream in = new FileInputStream(inputFile);

        byte[] bytes = new byte[(int) inputFile.length()];

        in.read(bytes);

        in.close();

        /*
         * if input is not base64 assume that output should be
         */
        boolean outputShouldBeBase64 = !Base64.isBase64(bytes);

        if (outputFile == null) {
            /*
             * create temp file with same extension as input file
             */
            String extension = "dat";
            String inputFileName = inputFile.getName();

            if (inputFile.getName().lastIndexOf(".") != -1) {
                extension = inputFileName.substring(inputFileName.lastIndexOf("."));
            }
            if (outputShouldBeBase64) {
                extension = ".base64" + extension;
            }
            outputFile = File.createTempFile("base64Conversion", extension);
            messages.addMessage("Using random file for output: " + outputFile.getAbsolutePath());

            for (int i = 0; i < paramvalues.getCount(); i++) {
                IGPParameter tmpParameter = (IGPParameter) paramvalues.getElement(i);
                IGPValue tmpParameterValue = gpUtilities.unpackGPValue(tmpParameter);
                if (tmpParameter.getName().equals(outputID)) {
                    tmpParameterValue.setAsText(outputFile.getAbsolutePath());
                }

            }
        }

        if (outputShouldBeBase64) {
            writeBase64(bytes, outputFile);
        } else {
            write(bytes, outputFile);
        }
    }

    private void writeBase64(byte[] content,
            File outputFile) throws Exception {
        String stringToWrite = Base64.encodeBase64String(content);

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        writer.write(stringToWrite);

        writer.close();
    }

    private void write(byte[] content,
            File outputFile) throws Exception {

        byte[] bytes = Base64.decodeBase64(content);

        FileOutputStream fout = new FileOutputStream(outputFile);

        fout.write(bytes);

        fout.close();
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
