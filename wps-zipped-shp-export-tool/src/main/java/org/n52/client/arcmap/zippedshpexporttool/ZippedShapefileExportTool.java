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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.datasourcesfile.DEFileType;
import com.esri.arcgis.datasourcesfile.DELayerType;
import com.esri.arcgis.datasourcesfile.DETextFileType;
import com.esri.arcgis.geodatabase.DEFeatureClass;
import com.esri.arcgis.geodatabase.DEFeatureClassType;
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
import com.esri.arcgis.geoprocessing.GPString;
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
 * This class represents a ArcGIS geoprocessing tool that exports vector data
 * as shapefiles, zips the files and encodes the zip file in base64.
 * This can be used as inline complex data for WPS execute requests.
 *
 * @author Benjamin Pross
 *
 */
public class ZippedShapefileExportTool extends BaseGeoprocessingTool {

    /**
     *
     */
    private static final long serialVersionUID = -2667973686420068297L;

    private static Logger LOGGER = LoggerFactory.getLogger(ZippedShapefileExportTool.class);

    private final String randomFileString = "RANDOM_FILE";

    private final String inputName = "in_layer";

    private final String resultName = "out_zipped_shapefile";

    public static final String toolName = "ZippedShapefileExportTool";

    public static final String displayName = "Zipped Shapefile Export Tool";

    private String metadataFileName = toolName + ".xml";

    public ZippedShapefileExportTool() {

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
        return (IName) new ZippedShapefileExportFunctionFactory().getFunctionName(toolName);
    }

    /**
     * Returns an array of paramInfo This is the location where the parameters
     * to the Function Tool are defined. This property returns an IArray of
     * parameter objects (IGPParameter). These objects define the
     * characteristics of the input and output parameters.
     */
    public IArray getParameterInfo() throws IOException, AutomationException {
        IArray parameters = new Array();

        GPParameter parameter4 = new GPParameter();

        GPCompositeDataType compositeVector = new GPCompositeDataType();
        compositeVector.addDataType(new GPFeatureLayerType());
        compositeVector.addDataType(new DEFeatureClassType());
        compositeVector.addDataType(new GPLayerType());
        compositeVector.addDataType(new DELayerType());
        compositeVector.addDataType(new GPFeatureRecordSetLayerType());
        compositeVector.addDataType(new DETextFileType());
        compositeVector.addDataType(new GPDataFileType());
        compositeVector.addDataType(new DEFileType());

        parameter4.setName(inputName);
        parameter4.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
        parameter4.setDisplayName(inputName);
        parameter4.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
        parameter4.setDataTypeByRef(compositeVector);
        parameter4.setValueByRef(new DEFeatureClass());
        parameters.add(parameter4);

        GPCompositeDataType compositeVector1 = new GPCompositeDataType();
        compositeVector1.addDataType(new GPFeatureLayerType());
        compositeVector1.addDataType(new DEFeatureClassType());
        compositeVector1.addDataType(new GPLayerType());
        compositeVector1.addDataType(new DELayerType());
        compositeVector1.addDataType(new GPFeatureRecordSetLayerType());
        compositeVector1.addDataType(new DETextFileType());
        compositeVector1.addDataType(new GPDataFileType());
        compositeVector1.addDataType(new DEFileType());
        compositeVector1.addDataType(new GPStringType());

        GPParameter parameter41 = new GPParameter();
        parameter41.setName(resultName);
        parameter41.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
        parameter41.setDisplayName(resultName);
        parameter41.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
        parameter41.setDataTypeByRef(compositeVector1);

        GPString gpRandomFileString = new GPString();

        gpRandomFileString.setAsText(randomFileString);
        parameter41.setValueByRef(gpRandomFileString);

        parameters.add(parameter41);

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

        try {
            for (int i = 0; i < paramvalues.getCount(); i++) {
                IGPParameter tmpParameter = (IGPParameter) paramvalues.getElement(i);
                IGPValue tmpParameterValue = gpUtilities.unpackGPValue(tmpParameter);
                if (tmpParameter.getName().equals(resultName)) {
                    /*
                     * if this is RANDOM_FILE generate a temp file
                     */
                    if (tmpParameterValue.getAsText().equals(randomFileString)) {
                        String newOutputPath = File.createTempFile("zippedShp", "base64.zip").getAbsolutePath();
                        tmpParameterValue.setAsText(newOutputPath);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while updating parameters: ", e);
        }
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

        if (parameterNameValueMap.get("in_layer") != null) {
            inputPath = parameterNameValueMap.get("in_layer");
        }

        LOGGER.debug("inputpath " + inputPath);

        /*
         * there should be only one
         */
        if (parameterNameValueMap.get(resultName) != null) {
            outputPath = parameterNameValueMap.get(resultName);

        }

        messages.addMessage("Writing base64 encoded zipped shapefile to: " + outputPath);

        try {
            new ShapefileGenerator().generateBase64EncodedString(inputPath, outputPath);
        } catch (Exception e) {
            LOGGER.error("Something went wrong while executing the WPS process.", e);
            try {
                messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Sorry, something went wrong while executing the zipped shapefile exporter.");
            } catch (Exception e2) {
                /* ignore */
            }
        }

        messages.addMessage("success");
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
