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

@ArcGISExtension(
        categories = { ArcGISCategories.GPFunctionFactories })
public class ZippedShapefileExportFunctionFactory implements IGPFunctionFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 575332665165567771L;

    private String functionFactoryAlias = "zippedshpfileexportfactory";

    private String factoryName = "ZippedShapefileExportFunctionFactory";

    private String category = "ZippedShapefileExportJavaToolset";

    private String toolName = "ZippedShapefileExport";

    private String toolDisplayName = "Java Zipped Shapefile Export Tool";

    /**
     * Returns the appropriate GPFunction object based on specified tool name
     */
    public IGPFunction getFunction(String name) throws IOException, AutomationException {
        if (name.equalsIgnoreCase(toolName)) {
            return new ZippedShapefileExportTool();
        }

        return null;
    }

    /**
     * Returns a GPFunctionName objects based on specified tool name
     */
    public IGPName getFunctionName(String name) throws IOException, AutomationException {
        if (name.equalsIgnoreCase(toolName)) {
            GPFunctionName functionName = new GPFunctionName();
            functionName.setCategory(category);
            functionName.setDescription("Tool for exporting layers as shapefiles and zipping them.");
            functionName.setDisplayName(toolDisplayName);
            functionName.setName(toolName);
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
        nameArray.add(getFunctionName(toolName));
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
