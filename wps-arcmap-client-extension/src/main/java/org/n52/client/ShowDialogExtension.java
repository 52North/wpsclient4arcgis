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
package org.n52.client;

import java.io.IOException;

import org.n52.client.arcmap.gui.dialog.ArcMapWPSClientDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.addins.desktop.Extension;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.interop.AutomationException;

/**
 * ArcMap extension used to open the <code>ArcMapWPSClientDialog</code>.
 *
 * @author Benjamin Pross
 *
 */
public class ShowDialogExtension extends Extension {

    private static Logger LOGGER = LoggerFactory.getLogger(ShowDialogExtension.class);
    /**
     * Initializes this application extension with the ArcMap application
     * instance it is hosted in.
     *
     * This method is automatically called by the host ArcMap application. It
     * marks the start of the dockable window's lifecycle. Clients must not call
     * this method.
     *
     * @param app
     *            is a reference to ArcMap's IApplication interface
     * @exception java.io.IOException
     *                if there are interop problems.
     * @exception com.esri.arcgis.interop.AutomationException
     *                if the component throws an ArcObjects exception.
     */
    @Override
    public void init(IApplication app) throws IOException, AutomationException {

        ArcMapWPSClientDialog dialog = null;

        try {
            dialog = ArcMapWPSClientDialog.getInstance(app);
        } catch (Exception e) {
            LOGGER.error("Error while getting dialog instance: ", e.getMessage());
        }

        if (dialog == null) {

            try {
                dialog = new ArcMapWPSClientDialog(app);
                dialog.setVisible(true);
            } catch (Exception e) {
                LOGGER.error("Error while creating new dialog instance: ", e.getMessage());
            }
        } else {
            dialog.setVisible(true);
            LOGGER.trace("Show dialog.");
        }

    }
}
