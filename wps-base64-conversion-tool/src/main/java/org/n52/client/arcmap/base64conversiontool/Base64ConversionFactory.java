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
package org.n52.client.arcmap.base64conversiontool;

import java.io.IOException;
import java.util.UUID;

import com.esri.arcgis.geodatabase.IEnumGPName;
import com.esri.arcgis.geodatabase.IGPName;
import com.esri.arcgis.geoprocessing.EnumGPName;
import com.esri.arcgis.geoprocessing.GPFunctionName;
import com.esri.arcgis.geoprocessing.IEnumGPEnvironment;
import com.esri.arcgis.geoprocessing.IGPFunction;
import com.esri.arcgis.geoprocessing.IGPFunctionFactory;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.interop.extn.ArcGISCategories;
import com.esri.arcgis.interop.extn.ArcGISExtension;
import com.esri.arcgis.system.IUID;
import com.esri.arcgis.system.UID;
import com.esri.arcgis.system.esriProductCode;

/**
 * This class represents the function factory for the
 * Base64Conversion ArcGIS geoprocessing tool.
 *
 * @author Benjamin Pross
 *
 */
@ArcGISExtension(
        categories = { ArcGISCategories.GPFunctionFactories })
public class Base64ConversionFactory implements IGPFunctionFactory {

    /**
     *
     */
    private static final long serialVersionUID = -6282191862777911563L;

    private String functionFactoryAlias = "base64conversionfactory";

    private String factoryName = "Base64ConversionFunctionFactory";

    private String category = "Base64ConversionJavaToolset";

    private String base64ConversionToolName = Base64ConversionTool.toolName;

    private String base64ConversionToolDisplayName = Base64ConversionTool.displayName;

    /**
     * Returns the appropriate GPFunction object based on specified tool name
     */
    public IGPFunction getFunction(String name) throws IOException, AutomationException {
        if (name.equalsIgnoreCase(base64ConversionToolName)) {
            return new Base64ConversionTool();
        }

        return null;
    }

    /**
     * Returns a GPFunctionName objects based on specified tool name
     */
    public IGPName getFunctionName(String name) throws IOException, AutomationException {
        if (name.equalsIgnoreCase(base64ConversionToolName)) {
            GPFunctionName functionName = new GPFunctionName();
            functionName.setCategory(category);
            functionName.setDescription("This tool converts files to base64 encoded files and vice versa.");
            functionName.setDisplayName(base64ConversionToolDisplayName);
            functionName.setName(base64ConversionToolName);
            functionName.setMinimumProduct(esriProductCode.esriProductCodeAdvanced);
            functionName.setFactoryByRef(this);
            return functionName;
        }

        return null;
    }

    /**
     * Returns names of all gp tools created by this function factory
     */
    public IEnumGPName getFunctionNames() throws IOException, AutomationException {
        EnumGPName nameArray = new EnumGPName();
        nameArray.add(getFunctionName(base64ConversionToolName));
        return nameArray;
    }

    /**
     * Returns Alias of the function factory
     */
    public String getAlias() throws IOException, AutomationException {
        return functionFactoryAlias;
    }

    /**
     * Returns Class ID
     */
    public IUID getCLSID() throws IOException, AutomationException {
        UID uid = new UID();
        uid.setValue("{" + UUID.nameUUIDFromBytes(this.getClass().getName().getBytes()) + "}");

        return uid;
    }

    /**
     * Returns Function Environments
     */
    public IEnumGPEnvironment getFunctionEnvironments() throws IOException, AutomationException {
        return null;
    }

    /**
     * Returns name of the FunctionFactory
     */
    public String getName() throws IOException, AutomationException {
        return factoryName;
    }
}
