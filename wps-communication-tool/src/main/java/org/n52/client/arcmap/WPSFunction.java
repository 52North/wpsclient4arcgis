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

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.ows.x11.ValueType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.DataType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteDocument.Execute;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.OutputReferenceType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ResponseDocumentType;
import net.opengis.wps.x100.ResponseFormType;
import net.opengis.wps.x100.SupportedComplexDataInputType;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.client.arcmap.util.InputTypeEnum;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.esri.arcgis.datainterop.FMEDestDatasetType;
import com.esri.arcgis.datainterop.FMESourceDatasetType;
import com.esri.arcgis.datasourcesfile.DEFile;
import com.esri.arcgis.datasourcesfile.DEFileType;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.geodatabase.GPMessage;
import com.esri.arcgis.geodatabase.IGPCodedValueDomain;
import com.esri.arcgis.geodatabase.IGPDomain;
import com.esri.arcgis.geodatabase.IGPMessage;
import com.esri.arcgis.geodatabase.IGPMessages;
import com.esri.arcgis.geodatabase.IGPValue;
import com.esri.arcgis.geodatabase.esriGPMessageSeverity;
import com.esri.arcgis.geoprocessing.BaseGeoprocessingTool;
import com.esri.arcgis.geoprocessing.GPBoolean;
import com.esri.arcgis.geoprocessing.GPBooleanType;
import com.esri.arcgis.geoprocessing.GPCodedValueDomain;
import com.esri.arcgis.geoprocessing.GPCompositeDataType;
import com.esri.arcgis.geoprocessing.GPDate;
import com.esri.arcgis.geoprocessing.GPDateType;
import com.esri.arcgis.geoprocessing.GPDouble;
import com.esri.arcgis.geoprocessing.GPDoubleType;
import com.esri.arcgis.geoprocessing.GPLong;
import com.esri.arcgis.geoprocessing.GPLongType;
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
 * @author Benjamin Pross
 *
 */
public class WPSFunction extends BaseGeoprocessingTool {

    /**
     * 
     */
    private static final long serialVersionUID = 2980889352605483580L;

    private static Logger LOGGER = LoggerFactory.getLogger(WPSFunction.class);

    private String toolName = "";

    private String displayName = "";

    private String metadataFileName = toolName + ".xml";

    private Array parameters;

    public static IApplication app;

    private final String outputPrefix = "out_";

    private final String randomFileString = "RANDOM_FILE";

    public WPSFunction() {

    }

    public WPSFunction(String toolName) {
        try {
            String[] identifierAndURL = toolName.split("@");

            this.toolName = toolName;
            this.displayName = identifierAndURL[0];
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
        return (IName) new WPSFunctionFactory().getFunctionName(toolName);
    }

    /**
     * Returns an array of paramInfo This is the location where the parameters
     * to the Function Tool are defined. This property returns an IArray of
     * parameter objects (IGPParameter). These objects define the
     * characteristics of the input and output parameters.
     */
    public IArray getParameterInfo() throws IOException, AutomationException {

        LOGGER.debug("Creating parameter array for " + toolName + " a.k.a " + displayName);

        parameters = new Array();

        String[] identifierAndURL = toolName.split("@");

        String wpsURL = identifierAndURL[1];

        WPSClientSession session = WPSClientSession.getInstance();

        ProcessDescriptionType descriptionType = null;

        JFrame progressFrame = new JFrame();
        try {

            progressFrame.setTitle("Connecting to server...");

            progressFrame.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/image/52n48x48-transp.png")).getImage());

            progressFrame.setAlwaysOnTop(true);

            progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            progressFrame.setSize(400, 80);

            progressFrame.setLocationRelativeTo(progressFrame.getRootPane());

            GridLayout l = new GridLayout(2, 1);

            progressFrame.setLayout(l);

            JLabel label1 = new JLabel();

            label1.setText("Connecting to " + wpsURL);

            progressFrame.add(label1);

            JProgressBar jbar = new JProgressBar();

            jbar.setDoubleBuffered(true);

            jbar.setIndeterminate(true);

            progressFrame.add(jbar);

            if (!session.getLoggedServices().contains(wpsURL)) {
                progressFrame.setVisible(true);
            }

            /**
             * TODO: do that maybe beforehand, if we want the input and output
             * descriptions for the process already at the process selection
             * panel
             */
            descriptionType = session.getProcessDescription(wpsURL, displayName);

            progressFrame.setVisible(false);

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            JOptionPane.showMessageDialog(null, "Could not connect to: \n" + wpsURL + ".", "52 North WPS ArcMap Client", 1);
            progressFrame.setVisible(false);
            return null;
        }

        DataInputs dataInputs = descriptionType.getDataInputs();
        InputDescriptionType[] inputDescriptions = dataInputs.getInputArray();

        for (int i = 0; i < inputDescriptions.length; i++) {

            final InputDescriptionType currentDescriptionType = inputDescriptions[i];

            String labelText = currentDescriptionType.getIdentifier().getStringValue();

            InputTypeEnum type = checkType(currentDescriptionType);

            switch (type) {
            case Complex:

                GPCompositeDataType composite = new GPCompositeDataType();
                // composite.addDataType(new DERasterBandType());
                // composite.addDataType(new DERasterDatasetType());
                // composite.addDataType(new GPRasterLayerType());
                // composite.addDataType(new GPRasterDataLayerType());
                // composite.addDataType(new GPFeatureLayerType());
                // composite.addDataType(new DEFeatureClassType());
                // composite.addDataType(new GPLayerType());
                // composite.addDataType(new DELayerType());
                // composite.addDataType(new GPFeatureRecordSetLayerType());
                // composite.addDataType(new DETextFileType());
                composite.addDataType(new DEFileType());
                composite.addDataType(new GPStringType());
                composite.addDataType(new FMEDestDatasetType());
                // composite.addDataType(new DEDatasetType());
                GPParameter parameter4 = new GPParameter();

                parameter4.setName(labelText);
                parameter4.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
                parameter4.setDisplayName(labelText);
                if (currentDescriptionType.getMinOccurs().intValue() > 0) {
                    parameter4.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
                } else {
                    parameter4.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
                }

                parameter4.setDataTypeByRef(composite);
                parameter4.setValueByRef(new GPString());
                parameters.add(parameter4);

                addSchemaMimeTypeEncodingToParameters(parameters, currentDescriptionType);

                try {
                    addReferenceParameter(labelText, parameters, esriGPParameterDirection.esriGPParameterDirectionInput);
                } catch (Exception e) {
                    LOGGER.error("Could not add reference parameter", e);
                }

                break;

            case Literal:
                GPParameter parameter2 = new GPParameter();
                parameter2.setName(labelText);
                parameter2.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
                parameter2.setDisplayName(labelText);
                if (currentDescriptionType.getMinOccurs().intValue() > 0) {
                    parameter2.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
                } else {
                    parameter2.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
                }

                DomainMetadataType dataType = currentDescriptionType.getLiteralData().getDataType();

                boolean noDataTypeFound = false;

                if (dataType != null) {

                    String dataTypeString = currentDescriptionType.getLiteralData().getDataType().getReference();

                    if (dataTypeString == null) {
                        dataTypeString = currentDescriptionType.getLiteralData().getDataType().getStringValue();
                    }
                    if (dataTypeString != null) {

                        if (dataTypeString.contains("double")) {
                            // parameter2.setDataTypeByRef(new GPDoubleType());
                            // parameter2.setValueByRef(new GPDouble());
                            /*
                             * try this to avoid decimal point/comma issues
                             */
                            parameter2.setDataTypeByRef(new GPStringType());
                            parameter2.setValueByRef(new GPString());
                        } else if (dataTypeString.contains("string")) {
                            parameter2.setDataTypeByRef(new GPStringType());
                            parameter2.setValueByRef(new GPString());
                        } else if (dataTypeString.contains("integer") || dataTypeString.contains("int")) {
                            parameter2.setDataTypeByRef(new GPLongType());
                            parameter2.setValueByRef(new GPLong());
                        } else if (dataTypeString.contains("dateTime")) {
                            parameter2.setDataTypeByRef(new GPDateType());
                            parameter2.setValueByRef(new GPDate());
                        } else if (dataTypeString.contains("boolean") || dataTypeString.contains("bool")) {
                            parameter2.setDataTypeByRef(new GPBooleanType());
                            parameter2.setValueByRef(new GPBoolean());
                        } else if (dataTypeString.contains("float")) {
                            parameter2.setDataTypeByRef(new GPDoubleType());
                            parameter2.setValueByRef(new GPDouble());
                        } else {
                            noDataTypeFound = true;
                        }
                    } else {
                        noDataTypeFound = true;
                    }

                } else {
                    noDataTypeFound = true;
                }

                try {

                    if (currentDescriptionType.getLiteralData().getAllowedValues() != null) {

                        ValueType[] allowedValues = currentDescriptionType.getLiteralData().getAllowedValues().getValueArray();

                        IGPCodedValueDomain domain = new GPCodedValueDomain();

                        for (ValueType allowedValue : allowedValues) {

                            String string = allowedValue.getStringValue();

                            LOGGER.debug("Allowed value " + string);

                            domain.addStringCode(string, string);
                        }
                        // Assign the domain to the parameter.
                        parameter2.setDomainByRef((IGPDomain) domain);

                    }
                    if (!noDataTypeFound) {
                        parameters.add(parameter2);
                    } else {
                        parameter2.setDataTypeByRef(new GPStringType());

                        String defaultValue = currentDescriptionType.getLiteralData().getDefaultValue();

                        if (defaultValue != null && !defaultValue.equals("")) {

                            GPString gpString = new GPString();

                            gpString.setValue(defaultValue);

                            parameter2.setValueByRef(gpString);
                        } else {
                            parameter2.setValueByRef(new GPString());
                        }
                        parameters.add(parameter2);
                    }

                } catch (Exception e) {
                    LOGGER.error("Could not create allowed values for literaldata input", e);
                }

                break;
            default:
                break;
            }

        }

        for (OutputDescriptionType outDescType : descriptionType.getProcessOutputs().getOutputArray()) {
            // TODO add support for literal-/bboxdata
            if (outDescType.getComplexOutput() == null) {
                LOGGER.info("Skipping non-complex output {}", outDescType.getIdentifier().getStringValue());
                continue;
            }

            GPCompositeDataType composite = new GPCompositeDataType();
            composite.addDataType(new FMESourceDatasetType());
            composite.addDataType(new DEFileType());
            composite.addDataType(new GPStringType());
            GPParameter parameter4 = new GPParameter();
            parameter4.setName(outputPrefix + outDescType.getIdentifier().getStringValue());
            parameter4.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
            parameter4.setDisplayName(outDescType.getIdentifier().getStringValue());
            parameter4.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
            parameter4.setDataTypeByRef(new DEFileType());

            DEFile file = new DEFile();

            String tmpFilePath = System.getenv("TMP") + File.separator + "wpsOutput" + UUID.randomUUID().toString().substring(0, 5) + ".tmp";

            file.setAsText(tmpFilePath);

            GPString gpRandomFileString = new GPString();

            gpRandomFileString.setAsText(tmpFilePath);

            parameter4.setValueByRef(file);
            parameters.add(parameter4);

            addSchemaMimeTypeEncodingToParameters(parameters, outDescType);

            try {
                addReferenceParameter(outDescType.getIdentifier().getStringValue(), parameters, esriGPParameterDirection.esriGPParameterDirectionOutput);
            } catch (Exception e) {
                LOGGER.error("Could not add reference parameter", e);
            }

        }
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

        //TODO, we need to check if the chosen combination of mime type, schema, encoding is supported by the process
        
        try {
            for (int i = 0; i < paramvalues.getCount(); i++) {
                IGPParameter tmpParameter = (IGPParameter) paramvalues.getElement(i);
                IGPValue tmpParameterValue = gpUtilities.unpackGPValue(tmpParameter);

                if (tmpParameter.getName().startsWith("out") && tmpParameterValue instanceof DEFile && tmpParameterValue.getAsText().endsWith("tmp")) {
                    // assume tmp file name was not touched, so if the file
                    // exists, create a different tmp file
                    File tmpFile = new File(tmpParameterValue.getAsText());

                    if (tmpFile.exists()) {

                        String tmpFilePath = System.getenv("TMP") + File.separator + "wpsOutput" + UUID.randomUUID().toString().substring(0, 5) + ".tmp";

                        tmpParameterValue.setAsText(tmpFilePath);
                    }

                }

                LOGGER.info("check " + tmpParameter.getName());
                LOGGER.info("Value: " + tmpParameterValue.getAsText());
            }
        } catch (AutomationException e) {
            LOGGER.error("Error in updating parameters method", e);
        } catch (IOException e) {
            LOGGER.error("Error in updating parameters method", e);
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

    /**
     * Executes the tool
     */
    public void execute(IArray paramvalues,
            ITrackCancel trackcancel,
            IGPEnvironmentManager envMgr,
            IGPMessages messages) throws IOException, AutomationException {

        String[] identifierAndURL = toolName.split("@");

        String wpsURL = identifierAndURL[1];

        try {
            messages.addMessage("WPS URL: " + wpsURL);

            WPSClientSession.getInstance().connect(wpsURL);

            Map<String, String> parameterNameValueMap = getParameterNameValueMap(paramvalues);

            ProcessDescriptionType pDescType = WPSClientSession.getInstance().getProcessDescription(wpsURL, displayName);

            ExecuteDocument execDoc = createExecuteDocument(parameterNameValueMap, pDescType, messages);

            if (execDoc == null) {
                return;
            }

            LOGGER.debug(execDoc.toString());

            ExecuteResponseDocument responseDoc = (ExecuteResponseDocument) WPSClientSession.getInstance().execute(wpsURL, execDoc);

            handleExecuteResponse(responseDoc, paramvalues, parameterNameValueMap, messages);

        } catch (Exception e1) {
            LOGGER.error("Something went wrong while executing the WPS process.", e1);
            try {
                messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Something went wrong while executing the WPS process.");
            } catch (Exception e) {
            }
        }

    }

    private void handleExecuteResponse(ExecuteResponseDocument responseDoc,
            IArray paramvalues,
            Map<String, String> parameterNameValueMap,
            IGPMessages messages) throws IOException, TransformerFactoryConfigurationError, TransformerException {

        ExecuteResponse response = responseDoc.getExecuteResponse();

        LOGGER.debug(response.toString());

        DataType outputData = response.getProcessOutputs().getOutputArray(0).getData();

        OutputReferenceType outputReference = response.getProcessOutputs().getOutputArray(0).getReference();

        String statusLocation = response.getStatusLocation();

        boolean processFinished = response.getStatus() == null ? false : response.getStatus().isSetProcessSucceeded();

        boolean processFailed = response.getStatus() == null ? false : response.getStatus().isSetProcessFailed();

        if (statusLocation != null && !statusLocation.equals("") && !processFinished) {

            // sleep for five seconds
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                LOGGER.error("Something went wrong while pausing the thread.", e1);
                messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Something went wrong while pausing the thread.");
            }

            URL statusLocationURL = new URL(statusLocation);

            ExecuteResponseDocument executeDoc = ExecuteResponseDocument.Factory.newInstance();

            try {
                executeDoc = ExecuteResponseDocument.Factory.parse(statusLocationURL.openStream());
            } catch (XmlException e) {
                LOGGER.error("Could not fetch statuslocation URL: {}", statusLocation);
                LOGGER.error(e.getMessage());
            }
            handleExecuteResponse(executeDoc, paramvalues, parameterNameValueMap, messages);
            return;
        }

        if (processFailed) {

            LOGGER.error("Something went wrong while executing the WPS process.");
            messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Something went wrong while executing the WPS process.");

            ExceptionType[] exceptions = response.getStatus().getProcessFailed().getExceptionReport().getExceptionArray();

            for (ExceptionType exceptionType : exceptions) {

                String[] exceptionTexts = exceptionType.getExceptionTextArray();

                String completeExceptionText = "";

                for (String string : exceptionTexts) {
                    completeExceptionText = completeExceptionText.concat(string + "\n");
                }

                messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, completeExceptionText);
            }
            return;
        }

        String outputPath = "";

        File outputFile = null;

        for (String key : parameterNameValueMap.keySet()) {
            if (key.startsWith(outputPrefix)) {
                /*
                 * there should be only one
                 */
                if (parameterNameValueMap.get(key) != null) {
                    outputPath = parameterNameValueMap.get(key);
                    /*
                     * if this is RANDOM_FILE generate a temp file by leaving
                     * output file null
                     */
                    if (!outputPath.equals(randomFileString)) {
                        outputFile = new File(outputPath);
                    }
                }
                break;
            }
        }

        String identifier = response.getProcessOutputs().getOutputArray(0).getIdentifier().getStringValue();

        String encoding = parameterNameValueMap.get(identifier + "_encoding");

        String mimeType = parameterNameValueMap.get(identifier + "_mimetype");

        String extension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);

        if (extension == null || extension.equals("")) {
            extension = "dat";
        }

        /*
         * if input is not base64 assume that output should be
         */
        boolean outputShouldBeBase64 = false;

        if (encoding != null) {
            outputShouldBeBase64 = encoding.equals("base64");
        }

        if (outputFile == null) {

            if (outputShouldBeBase64) {
                extension = ".base64." + extension;
            }
            outputFile = File.createTempFile("wpsFunction", "." + extension);
        }

        for (int i = 0; i < paramvalues.getCount(); i++) {
            IGPParameter tmpParameter = (IGPParameter) paramvalues.getElement(i);
            IGPValue tmpParameterValue = gpUtilities.unpackGPValue(tmpParameter);
            if (tmpParameter.getName().equals(outputPrefix + identifier)) {
                tmpParameterValue.setAsText(outputFile.getAbsolutePath());
            }

        }

        String s = "";

        if (outputData != null) {

            ComplexDataType cData = outputData.getComplexData();

            s = nodeToString(cData.getDomNode().getFirstChild());

            if (!response.toString().contains("application/x-zipped-shp")) {

                if (s == null || s.trim().equals("") || s.trim().equals(" ")) {

                    try {
                        s = nodeToString(cData.getDomNode().getChildNodes().item(1));

                        LOGGER.debug("ComplexData content " + s);
                    } catch (Exception e) {
                        LOGGER.error("cData.getDomNode().getFirstChild().getChildNodes().item(1) leads to " + e);
                    }
                } else {
                    LOGGER.debug("ComplexData content " + s);
                }
            }

            LOGGER.debug("Writing " + identifier + " output to " + outputFile.getAbsolutePath());
            messages.addMessage("Writing " + identifier + " output to " + outputFile.getAbsolutePath());

            BufferedWriter bwr = new BufferedWriter(new FileWriter(outputFile));

            bwr.write(s);

            bwr.close();

        } else if (outputReference != null) {

            URL href = new URL(outputReference.getHref());

            FileUtils.copyURLToFile(href, outputFile);
        }

    }

    private ExecuteDocument createExecuteDocument(Map<String, String> parameterNameValueMap,
            ProcessDescriptionType pDescType,
            IGPMessages messages) {

        ExecuteDocument result = ExecuteDocument.Factory.newInstance();

        Execute ex = result.addNewExecute();

        ex.setVersion("1.0.0");

        ex.setService("WPS");

        ex.addNewIdentifier().setStringValue(pDescType.getIdentifier().getStringValue());

        DataInputsType dataInputs = ex.addNewDataInputs();

        InputDescriptionType[] inputDescTypes = pDescType.getDataInputs().getInputArray();

        for (InputDescriptionType inputDescriptionType : inputDescTypes) {

            String identifier = inputDescriptionType.getIdentifier().getStringValue();

            InputTypeEnum type = checkType(inputDescriptionType);

            String value = parameterNameValueMap.get(identifier);

            if (value == null) {
                continue;
            }

            LOGGER.debug("Value " + value);

            switch (type) {
            case Complex:
                /*
                 * TODO: add strategy for empty schema/mimetype/encoding
                 */
                String schema = parameterNameValueMap.get(identifier + "_schema");
                LOGGER.debug("Schema = " + schema);

                String mimeType = parameterNameValueMap.get(identifier + "_mimetype");
                LOGGER.debug("Mime Type = " + mimeType);

                String encoding = parameterNameValueMap.get(identifier + "_encoding");
                LOGGER.debug("Encoding = " + encoding);

                String isReference = parameterNameValueMap.get(identifier + "_reference");
                LOGGER.debug("IsReference = " + isReference);

                InputType inTypeVector = dataInputs.addNewInput();

                inTypeVector.addNewIdentifier().setStringValue(identifier);

                if (isReference != null && Boolean.parseBoolean(isReference)) {

                    InputReferenceType inHref = inTypeVector.addNewReference();

                    inHref.setHref(value);

                    if (mimeType != null && !mimeType.equals("")) {

                        inHref.setMimeType(mimeType);
                    }

                    if (schema != null && !schema.equals("")) {

                        inHref.setSchema(schema);
                    }

                } else {
                    try {
                        messages.addMessage("Reading file for complex input: " + value);
                    } catch (Exception e1) {
                        /*
                         * ignore
                         */
                    }

                    DocumentBuilderFactory factoryVector = DocumentBuilderFactory.newInstance();

                    if (mimeType != null && mimeType.contains("xml")) {
                        try {

                            DataType dataTypeVector = inTypeVector.addNewData();

                            ComplexDataType cDataTypeVector = dataTypeVector.addNewComplexData();

                            factoryVector.setNamespaceAware(true);

                            // TODO: just parse text input??? For
                            // vector/raster/whatever??
                            DocumentBuilder builder = factoryVector.newDocumentBuilder();

                            Document d = builder.parse(new File(value));

                            cDataTypeVector.set(XmlObject.Factory.parse(d));

                            if (mimeType != null && !mimeType.equals("")) {

                                cDataTypeVector.setMimeType(mimeType);
                            }

                            if (schema != null && !schema.equals("")) {

                                cDataTypeVector.setSchema(schema);
                            }
                            
                            if (encoding != null && !encoding.equals("")) {
                                
                                cDataTypeVector.setEncoding(encoding);
                            }

                        } catch (Exception e) {
                            LOGGER.error("Can not parse input XML", e);
                        }
                    } else {

                        try {

                            BufferedReader bread = new BufferedReader(new FileReader(new File(value)));

                            String line = "";

                            String content = "";

                            while ((line = bread.readLine()) != null) {
                                content = content.concat(line);
                            }

                            bread.close();

                            DataType dataTypeVector = inTypeVector.addNewData();

                            ComplexDataType cDataTypeVector = dataTypeVector.addNewComplexData();

                            factoryVector.setNamespaceAware(true);

                            DocumentBuilder builder = factoryVector.newDocumentBuilder();

                            Document d = builder.newDocument();

                            Node cdata = d.createCDATASection(content);

                            cDataTypeVector.set(XmlObject.Factory.parse(cdata));

                            if (mimeType != null && !mimeType.equals("")) {

                                cDataTypeVector.setMimeType(mimeType);
                            }

                            if (schema != null && !schema.equals("")) {

                                cDataTypeVector.setSchema(schema);
                            }
                            
                            if (encoding != null && !encoding.equals("")) {
                                
                                cDataTypeVector.setEncoding(encoding);
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                }

                break;

            case Literal:

                InputType inType1 = dataInputs.addNewInput();

                inType1.addNewIdentifier().setStringValue(identifier);

                DataType dataType1 = inType1.addNewData();

                LiteralDataType lit = dataType1.addNewLiteralData();

                DomainMetadataType literalDatatype = inputDescriptionType.getLiteralData().getDataType();

                String datatypeRef = null;

                if (literalDatatype != null) {
                    datatypeRef = literalDatatype.getReference();
                    if (datatypeRef == null) {
                        datatypeRef = literalDatatype.getStringValue();
                    }
                }

                if (datatypeRef == null) {
                    datatypeRef = "xs:string";
                }

                lit.setDataType(datatypeRef);

                lit.setStringValue(value);

                break;
            default:
                break;
            }
        }
        try {
            addResponseForm(pDescType, ex, parameterNameValueMap, messages);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return result;
    }

    private void addResponseForm(ProcessDescriptionType pDescType,
            Execute ex,
            Map<String, String> parameterNameValueMap,
            IGPMessages messages) throws IllegalArgumentException {

        ArrayList<OutputDescriptionType> outputDescTypes = new ArrayList<OutputDescriptionType>(pDescType.getProcessOutputs().getOutputArray().length);

        for (OutputDescriptionType outDescType : pDescType.getProcessOutputs().getOutputArray()) {
            if (parameterNameValueMap.get(outputPrefix + outDescType.getIdentifier().getStringValue()) != null) {
                outputDescTypes.add(outDescType);
            }
        }

        if (outputDescTypes.size() > 1) {
            LOGGER.error("More than one output currently not supported.");
            try {
                IGPMessage errorMessage = new GPMessage();
                errorMessage.setErrorCode(esriGPMessageSeverity.esriGPMessageSeverityError);
                errorMessage.setDescription("Currently only one output is allowed. Found " + outputDescTypes.size() + ".");
                messages.add(errorMessage);
            } catch (Exception e) {
                /*
                 * ignore
                 */
            }
            throw new IllegalArgumentException();
        }

        ResponseFormType responseForm = ex.addNewResponseForm();

        ResponseDocumentType responseDocument = responseForm.addNewResponseDocument();

        DocumentOutputDefinitionType output = responseDocument.addNewOutput();

        String identifier = outputDescTypes.get(0).getIdentifier().getStringValue();

        output.addNewIdentifier().setStringValue(identifier);
        /*
         * TODO: add strategy for empty schema/mimetype/encoding
         */
        String schema = parameterNameValueMap.get(identifier + "_schema");
        LOGGER.debug("Schema = " + schema);

        String mimeType1 = parameterNameValueMap.get(identifier + "_mimetype");
        LOGGER.debug("Mime Type = " + mimeType1);

        String encoding = parameterNameValueMap.get(identifier + "_encoding");
        LOGGER.debug("Mime Type = " + encoding);

        String isReference = parameterNameValueMap.get(identifier + "_reference");
        LOGGER.debug("IsReference = " + isReference);

        if (isReference != null && Boolean.parseBoolean(isReference)) {
            output.setAsReference(true);
        }

        if (mimeType1 != null && !mimeType1.equals("")) {
            output.setMimeType(mimeType1);
        }
        if (encoding != null && !encoding.equals("")) {
            output.setEncoding(encoding);
        }
        if (schema != null && !schema.equals("")) {
            output.setSchema(schema);
        }

    }

    private Map<String, String> getParameterNameValueMap(IArray paramvalues) {
        try {
            Map<String, String> result = new HashMap<String, String>(paramvalues.getCount());

            for (int i = 0; i < paramvalues.getCount(); i++) {
                IGPParameter tmpParameter = (IGPParameter) paramvalues.getElement(i);
                IGPValue tmpParameterValue = gpUtilities.unpackGPValue(tmpParameter);
                if (!tmpParameterValue.getAsText().equals("")) {
                    LOGGER.info("added " + tmpParameter.getName());
                    result.put(tmpParameter.getName(), tmpParameterValue.getAsText());
                } else {
                    LOGGER.info("Omitted " + tmpParameter.getName());
                    LOGGER.info("Value: " + tmpParameterValue);
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
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
        // no license checking is being done here.
        return true;
    }

    public static void setApp(IApplication app) {
        WPSFunction.app = app;
    }

    private InputTypeEnum checkType(InputDescriptionType inputDescType) {

        SupportedComplexDataInputType cDataType = inputDescType.getComplexData();
        LiteralInputType lDataType = inputDescType.getLiteralData();

        if (cDataType != null) {
            return InputTypeEnum.Complex;
        } else if (lDataType != null) {
            return InputTypeEnum.Literal;
        }
        return InputTypeEnum.Unknown;
    }

    public String getMetadataFileName() {
        try {
            return this.getDisplayName() + ".xml";
        } catch (AutomationException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    private void addSchemaMimeTypeEncodingToParameters(IArray params,
            InputDescriptionType currentDescriptionType) {

        String labelText = currentDescriptionType.getIdentifier().getStringValue();

        Set<String> schemas = new HashSet<String>();
        Set<String> mimeTypes = new HashSet<String>();
        Set<String> encodings = new HashSet<String>();

        ComplexDataDescriptionType defaultFormat = currentDescriptionType.getComplexData().getDefault().getFormat();

        String defaultSchema = defaultFormat.getSchema();
        String defaultEncoding = defaultFormat.getEncoding();

        if (defaultSchema != null && !defaultSchema.equals("")) {
            schemas.add(defaultSchema);
        }
        if (defaultEncoding != null && !defaultEncoding.equals("")) {
            encodings.add(defaultEncoding);
        }
        mimeTypes.add(defaultFormat.getMimeType());

        ComplexDataDescriptionType[] supportedFormats = currentDescriptionType.getComplexData().getSupported().getFormatArray();

        for (ComplexDataDescriptionType complexDataDescriptionType : supportedFormats) {

            String supportedSchema = complexDataDescriptionType.getSchema();
            String supportedEncoding = complexDataDescriptionType.getEncoding();

            if (supportedSchema != null && !supportedSchema.equals("")) {
                schemas.add(supportedSchema);
            }
            
            if (supportedEncoding != null && !supportedEncoding.equals("")) {
                encodings.add(supportedEncoding);
            }

            mimeTypes.add(complexDataDescriptionType.getMimeType());
        }

        if (!schemas.isEmpty()) {

            try {
                parameters.add(createListBoxStringParameter(labelText + "_schema", labelText + " schema", schemas, false));
            } catch (AutomationException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }

        if (!encodings.isEmpty()) {

            try {
                parameters.add(createListBoxStringParameter(labelText + "_encoding", labelText + " encoding", encodings, false));
            } catch (AutomationException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }

        try {
            parameters.add(createListBoxStringParameter(labelText + "_mimetype", labelText + " mime type", mimeTypes, false));
        } catch (AutomationException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void addSchemaMimeTypeEncodingToParameters(Array parameters2,
            OutputDescriptionType outDescType) {
        String labelText = outDescType.getIdentifier().getStringValue();

        Set<String> schemas = new HashSet<String>();
        Set<String> mimeTypes = new HashSet<String>();
        Set<String> encodings = new HashSet<String>();

        ComplexDataDescriptionType defaultFormat = outDescType.getComplexOutput().getDefault().getFormat();

        String defaultSchema = defaultFormat.getSchema();

        if (defaultSchema != null && !defaultSchema.equals("")) {
            schemas.add(defaultSchema);
        }
        mimeTypes.add(defaultFormat.getMimeType());
        encodings.add(defaultFormat.getEncoding());

        ComplexDataDescriptionType[] supportedFormats = outDescType.getComplexOutput().getSupported().getFormatArray();

        for (ComplexDataDescriptionType complexDataDescriptionType : supportedFormats) {
            String supportedSchema = complexDataDescriptionType.getSchema();

            if (supportedSchema != null && !supportedSchema.equals("")) {
                schemas.add(supportedSchema);
            }

            mimeTypes.add(complexDataDescriptionType.getMimeType());
            encodings.add(complexDataDescriptionType.getEncoding());
        }

        if (!schemas.isEmpty()) {

            try {
                parameters.add(createListBoxStringParameter(labelText + "_schema", labelText + " schema", schemas, true));
            } catch (AutomationException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        try {
            parameters.add(createListBoxStringParameter(labelText + "_mimetype", labelText + " mime type", mimeTypes, true));
        } catch (AutomationException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        try {
            parameters.add(createListBoxStringParameter(labelText + "_encoding", labelText + " encoding", encodings, true));
        } catch (AutomationException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void addReferenceParameter(String parameterName,
            Array parameters,
            int direction) throws Exception {
        GPParameter parameter2 = new GPParameter();
        parameter2.setName(parameterName + "_reference");
        parameter2.setDirection(direction);
        parameter2.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
        parameter2.setDisplayName(parameterName + " as reference");
        parameter2.setDataTypeByRef(new GPBooleanType());
        parameter2.setValueByRef(new GPBoolean());
        parameters.add(parameter2);
    }

    private GPParameter createListBoxStringParameter(String name,
            String displayName,
            Set<String> listBoxElements,
            boolean output) throws Exception {
        GPParameter parameter = new GPParameter();
        parameter.setName(name);
        if (output) {
            parameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
        } else {
            parameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
        }
        parameter.setDisplayName(name);
        parameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
        parameter.setDataTypeByRef(new GPStringType());
        parameter.setValueByRef(new GPString());
        IGPCodedValueDomain domain = new GPCodedValueDomain();

        for (String string : listBoxElements) {
            domain.addStringCode(string, string);
        }
        // Assign the domain to the parameter.
        parameter.setDomainByRef((IGPDomain) domain);

        return parameter;
    }

    private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
        StringWriter stringWriter = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));

        return stringWriter.toString();
    }

}
