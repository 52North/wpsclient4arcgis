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
package org.n52.client.arcmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.client.WPSClientSession;
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

@ArcGISExtension(categories = { ArcGISCategories.GPFunctionFactories })
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
	public IGPFunction getFunction(String name) throws IOException,
			AutomationException {
		return new WPSFunction(name);
	}

	/**
	 * Returns a GPFunctionName objects based on specified tool name
	 */
	public IGPName getFunctionName(String name) throws IOException, AutomationException {
		
		String[] identifierAndURL = name.split("@");
		
		LOGGER.debug("Category (WPS-URL) " + identifierAndURL[1]);
		LOGGER.debug("Displayname (Proces-ID) " + identifierAndURL[0]);
		
		GPFunctionName functionName = new GPFunctionName();
		functionName.setCategory(identifierAndURL[1]);
		try {
			ProcessDescriptionType pDesc = WPSClientSession.getInstance().getProcessDescription(identifierAndURL[1], identifierAndURL[0]);
			String abstractString = "-";
			if(pDesc.getAbstract() != null){
				abstractString = pDesc.getAbstract().getStringValue();
			}
			functionName.setDescription(abstractString);			
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			functionName.setDescription("-");
		}
		functionName.setDisplayName(identifierAndURL[0]);
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
