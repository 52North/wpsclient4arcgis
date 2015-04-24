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
package org.n52.client;

import java.io.IOException;

import org.n52.client.arcmap.gui.dialog.ArcMapWPSClientDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.addins.desktop.Extension;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author Benjamin Pross
 *
 */
public class ShowDialogExtension extends Extension {
	
	private static Logger LOGGER = LoggerFactory.getLogger(ShowDialogExtension.class);

	private IApplication app;
	
	private static ShowDialogExtension instance;	
	
	public static ShowDialogExtension getInstance(){
		return instance;
	}	
	
	/**
	 * Initializes this application extension with the ArcMap application instance it is hosted in.
	 * 
	 * This method is automatically called by the host ArcMap application.
	 * It marks the start of the dockable window's lifecycle.
	 * Clients must not call this method.
	 * 
	 * @param app is a reference to ArcMap's IApplication interface
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	@Override
	public void init(IApplication arg0) throws IOException, AutomationException {
		
		this.app = arg0;
		instance = this;
		
		ArcMapWPSClientDialog dialog = null;
		
		try {			
			dialog = ArcMapWPSClientDialog.getInstance();			
		} catch (Exception e) {
			LOGGER.error("Error while getting dialog instance: ", e.getMessage());
		}
		
		if(dialog == null){
			
			try {				
				dialog = new ArcMapWPSClientDialog(arg0);
				dialog.setVisible(true);				
			} catch (Exception e) {			
				LOGGER.error("Error while creating new dialog instance: ",  e.getMessage());
			}
		}else{
			dialog.setVisible(true);
			LOGGER.debug("Show dialog.");
		}

	}
	
	public IApplication getApplication(){
		return app;
	}	
}
