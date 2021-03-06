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
package org.n52.client.arcmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.n52.geoprocessing.wps.client.WPSClientSession;
import org.n52.geoprocessing.wps.client.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.framework.IApplication;
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
 * ZippedShapefileExport ArcGIS geoprocessing tool.
 *
 * @author Benjamin Pross
 *
 */
@ArcGISExtension(
        categories = { ArcGISCategories.GPFunctionFactories })
public class WPSFunctionFactory implements IGPFunctionFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(WPSFunctionFactory.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String functionFactoryAlias = "wpsfunctionfactory";

    private String factoryName = "wpsfunctionfactory";

    public static IApplication app;

    public ArrayList<String> functionNames = new ArrayList<String>();

    /**
     * Returns the appropriate GPFunction object based on specified tool name
     */
    public IGPFunction getFunction(String name) throws IOException, AutomationException {
        return new WPSFunction(name);
    }

    /**
     * Returns a GPFunctionName objects based on specified tool name
     */
    public IGPName getFunctionName(String name) throws IOException, AutomationException {

        String[] identifierAndURL = name.split("@");

        String wpsURL = identifierAndURL[1];
        String processID = identifierAndURL[0];
        String version = "1.0.0";

        try {
            version = identifierAndURL[2];
        } catch (Exception e) {
            LOGGER.error("Could not extract version from tool name: " + identifierAndURL + ". Possibly an old process was started with a new version of the client. Falling back to verssion 1.0.0.");
        }

        LOGGER.trace("Category (WPS-URL) " + wpsURL);
        LOGGER.trace("Displayname (Process-ID) " + processID);
        LOGGER.trace("Version " + version);

        GPFunctionName functionName = new GPFunctionName();
        functionName.setCategory(identifierAndURL[1]);
        try {
            Process pDesc = WPSClientSession.getInstance().getProcessDescription(wpsURL, processID, version);
            String abstractString = "-";
            if (pDesc.getAbstract() != null) {
                abstractString = pDesc.getAbstract();
            }
            functionName.setDescription(abstractString);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            functionName.setDescription("-");
        }
        functionName.setDisplayName(processID);
        functionName.setName(name);
        functionName.setMinimumProduct(esriProductCode.esriProductCodeAdvanced);
        functionName.setFactoryByRef(this);
        return functionName;

    }

    /**
     * Returns names of all gp tools created by this function factory
     */
    public IEnumGPName getFunctionNames() throws IOException, AutomationException {
        EnumGPName nameArray = new EnumGPName();
        for (String functionName : functionNames) {
            nameArray.add(getFunctionName(functionName));
        }

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
