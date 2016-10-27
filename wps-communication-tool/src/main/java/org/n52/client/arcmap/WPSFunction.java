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
import java.util.Observable;
import java.util.Observer;
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
import net.opengis.ows.x11.ExceptionReportDocument;
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
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.OutputReferenceType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ResponseDocumentType;
import net.opengis.wps.x100.ResponseFormType;
import net.opengis.wps.x100.SupportedComplexDataInputType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.client.arcmap.util.Download;
import org.n52.client.arcmap.util.InputTypeEnum;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.esri.arcgis.datainterop.FMEDestDatasetType;
import com.esri.arcgis.datasourcesfile.DEFile;
import com.esri.arcgis.datasourcesfile.DEFileType;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.geodatabase.IGPCodedValueDomain;
import com.esri.arcgis.geodatabase.IGPDomain;
import com.esri.arcgis.geodatabase.IGPMessages;
import com.esri.arcgis.geodatabase.IGPValue;
import com.esri.arcgis.geodatabase.esriGPMessageSeverity;
import com.esri.arcgis.geoprocessing.BaseGeoprocessingTool;
import com.esri.arcgis.geoprocessing.GPBoolean;
import com.esri.arcgis.geoprocessing.GPBooleanType;
import com.esri.arcgis.geoprocessing.GPCodedValueDomain;
import com.esri.arcgis.geoprocessing.GPCompositeDataType;
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
 * This class represents a ArcGIS geoprocessing tool that communicates with
 * Web Processing Services (WPS). It reads in files for complex data inputs and
 * produces files again for complex output data of the WPS.
 *
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
                composite.addDataType(new DEFileType());
                composite.addDataType(new GPStringType());
                composite.addDataType(new FMEDestDatasetType());
                GPParameter complexParameter = new GPParameter();

                complexParameter.setName(labelText);
                complexParameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
                complexParameter.setDisplayName(labelText);
                if (currentDescriptionType.getMinOccurs().intValue() > 0) {
                    complexParameter.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
                } else {
                    complexParameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
                }

                complexParameter.setDataTypeByRef(composite);
                complexParameter.setValueByRef(new GPString());
                parameters.add(complexParameter);

                addSchemaMimeTypeEncodingToParameters(parameters, currentDescriptionType);

                try {
                    addReferenceParameter(labelText, parameters, esriGPParameterDirection.esriGPParameterDirectionInput);
                } catch (Exception e) {
                    LOGGER.error("Could not add reference parameter", e);
                }

                break;

            case Literal:
                GPParameter literalParameter = new GPParameter();
                literalParameter.setName(labelText);
                literalParameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
                literalParameter.setDisplayName(labelText);
                if (currentDescriptionType.getMinOccurs().intValue() > 0) {
                    literalParameter.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
                } else {
                    literalParameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
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
                            literalParameter.setDataTypeByRef(new GPStringType());
                            literalParameter.setValueByRef(new GPString());
                        } else if (dataTypeString.contains("string")) {
                            literalParameter.setDataTypeByRef(new GPStringType());
                            literalParameter.setValueByRef(new GPString());
                        } else if (dataTypeString.contains("integer") || dataTypeString.contains("int")) {
                            literalParameter.setDataTypeByRef(new GPLongType());
                            literalParameter.setValueByRef(new GPLong());
                        } else if (dataTypeString.contains("dateTime")) {
                            //TODO: use String here as format of dateTime is not clear
                            literalParameter.setDataTypeByRef(new GPStringType());
                            literalParameter.setValueByRef(new GPString());
                        } else if (dataTypeString.contains("boolean") || dataTypeString.contains("bool")) {
                            literalParameter.setDataTypeByRef(new GPBooleanType());
                            literalParameter.setValueByRef(new GPBoolean());
                        } else if (dataTypeString.contains("float")) {
                            literalParameter.setDataTypeByRef(new GPDoubleType());
                            literalParameter.setValueByRef(new GPDouble());
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
                        literalParameter.setDomainByRef((IGPDomain) domain);

                    }
                    if (!noDataTypeFound) {
                        parameters.add(literalParameter);
                    } else {
                        literalParameter.setDataTypeByRef(new GPStringType());

                        String defaultValue = currentDescriptionType.getLiteralData().getDefaultValue();

                        if (defaultValue != null && !defaultValue.equals("")) {

                            GPString gpString = new GPString();

                            gpString.setValue(defaultValue);

                            literalParameter.setValueByRef(gpString);
                        } else {
                            literalParameter.setValueByRef(new GPString());
                        }
                        parameters.add(literalParameter);
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

            String identifier = outDescType.getIdentifier().getStringValue();

            GPParameter outputParameter = new GPParameter();
            outputParameter.setName(outputPrefix + identifier);
            outputParameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
            outputParameter.setDisplayName(identifier);
            outputParameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
            outputParameter.setDataTypeByRef(new DEFileType());

            DEFile file = new DEFile();

            String tmpFilePath = System.getenv("TMP") + identifier + UUID.randomUUID().toString().substring(0, 5) + ".tmp";

            file.setAsText(tmpFilePath);

            outputParameter.setValueByRef(file);
            parameters.add(outputParameter);

            addSchemaMimeTypeEncodingToParameters(parameters, outDescType);

            try {
                addReferenceParameter(identifier, parameters, esriGPParameterDirection.esriGPParameterDirectionOutput);
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

        // TODO, we need to check if the chosen combination of mime type,
        // schema, encoding is supported by the process

        try {
            for (int i = 0; i < paramvalues.getCount(); i++) {
                IGPParameter tmpParameter = (IGPParameter) paramvalues.getElement(i);
                IGPValue tmpParameterValue = gpUtilities.unpackGPValue(tmpParameter);

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

            //only debug execute document if smaller than 5 MB
            if(execDoc.toString().length() < 5242880){
                LOGGER.debug(execDoc.toString());
            }

            try {
                messages.addMessage("Sending POST request to WPS.");
            } catch (Exception e1) {
                /*
                 * ignore
                 */
            }

            Object response = WPSClientSession.getInstance().execute(wpsURL, execDoc);

            try {
                messages.addMessage("Got response.");
            } catch (Exception e1) {
                /*
                 * ignore
                 */
            }

            if (response instanceof ExecuteResponseDocument) {

                handleExecuteResponse((ExecuteResponseDocument) response, paramvalues, parameterNameValueMap, messages, trackcancel);

            } else if (response instanceof ExceptionReportDocument) {
                LOGGER.error("Something went wrong while executing the WPS process. Exceptionreport: {}", response.toString());
                try {
                    messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Something went wrong while executing the WPS process.");
                    messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, response.toString());
                } catch (Exception e) {
                }
            }

        } catch (Exception e1) {
            LOGGER.error("Something went wrong while executing the WPS process.", e1);
            try {
                messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Something went wrong while executing the WPS process.");
                messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, e1.getMessage());
            } catch (Exception e) {
            }
        }

    }

    private void handleExecuteResponse(ExecuteResponseDocument responseDoc,
            IArray paramvalues,
            Map<String, String> parameterNameValueMap,
            final IGPMessages messages, ITrackCancel trackcancel) throws IOException, TransformerFactoryConfigurationError, TransformerException {

        ExecuteResponse response = responseDoc.getExecuteResponse();

        //only debug response if smaller than 5 MB
        if(response.toString().length() < 5242880){
            LOGGER.debug(response.toString());
        }

        String statusLocation = response.getStatusLocation();

        boolean processFinished = response.getStatus() == null ? false : response.getStatus().isSetProcessSucceeded();

        boolean processFailed = response.getStatus() == null ? false : response.getStatus().isSetProcessFailed();

        OutputDataType[] outputs = response.getProcessOutputs().getOutputArray();

        if (statusLocation != null && !statusLocation.equals("") && !processFinished  && !processFailed) {

            boolean processAccepted = false;
            boolean processStarted = false;

            String status = "";
            int percentage = 0;

            if(response.getStatus().isSetProcessAccepted()){
                processAccepted = true;
            }

            if(response.getStatus().isSetProcessStarted()){
                processStarted = true;
                status = response.getStatus().getProcessStarted().getStringValue();
                percentage = response.getStatus().getProcessStarted().getPercentCompleted();
            }

            try {

                if (processAccepted) {
                    messages.addMessage("Process accepted.");
                }
                if (processStarted) {
                    if (status != null && !status.isEmpty()) {
                        messages.addMessage("Process status: " + status);
                    } else if (percentage != 0) {
                        messages.addMessage("Process completed: " + status + " %");
                    }
                }
            } catch (Exception e) {
                /* ignore */
            }

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
            handleExecuteResponse(executeDoc, paramvalues, parameterNameValueMap, messages, trackcancel);
            return;
        } else if (processFinished) {

            for (OutputDataType outputDataType : outputs) {

                DataType outputData = outputDataType.getData();

                OutputReferenceType outputReference = outputDataType.getReference();

                String identifier = outputDataType.getIdentifier().getStringValue();

                String outputPath = parameterNameValueMap.get(outputPrefix + identifier);

                File outputFile = new File(outputPath);

                String mimeType = parameterNameValueMap.get(identifier + "_mimetype");

                String extension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);

                if (extension == null || extension.equals("")) {
                    extension = "dat";
                }

                LOGGER.debug("Writing " + identifier + " output to " + outputFile.getAbsolutePath());
                messages.addMessage("Writing " + identifier + " output to " + outputFile.getAbsolutePath());

                String s = "";

                if (outputData != null) {

                    ComplexDataType cData = outputData.getComplexData();

                    s = nodeToString(cData.getDomNode().getFirstChild());

                    //TODO check if still necessary
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

                    BufferedWriter bwr = new BufferedWriter(new FileWriter(outputFile));

                    bwr.write(s);

                    bwr.close();

                } else if (outputReference != null) {

                    URL href = new URL(outputReference.getHref());
                    try {
                        messages.addMessage("Start result download from " + outputReference.getHref());
                    } catch (Exception e) {
                        /* ignore */
                    }

                    final Download download = new Download(href, outputFile);

                    download.addObserver(new Observer() {

                        @Override
                        public void update(Observable o,
                                Object arg) {
                            try {
                                messages.addMessage("Downloaded " + download.getDownloaded() + " / " + download.getSize() + " bytes.");
                            } catch (Exception e) {
                                /* ignore */
                            }
                        }
                    });

                    download.startDownload();
                }

            }
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

                    if (encoding != null && !encoding.equals("")) {

                        inHref.setEncoding(encoding);
                    }

                } else {
                    try {
                        messages.addMessage("Reading file for complex input: " + value);
                    } catch (Exception e1) {
                        /*
                         * ignore
                         */
                    }

                    File inputFile = null;

                    boolean isQuickExport = false;

                    // split string coming from quick export
                    if (value.contains(",")) {

                        isQuickExport = true;

                        LOGGER.info("Input value for input {} seems to be generated by the quick export tool.", identifier);
                        LOGGER.info("Trying to split parameter coming from quick export tool.");

                        String[] parameters = value.split(",");

                        for (String parameter : parameters) {
                            //remove possible quotation marks
                            parameter = parameter.replace("\"", "");
                            try {
                                inputFile = new File(parameter);

                                if (inputFile.exists()) {
                                    value = parameter;
                                    break;
                                }
                            } catch (Exception e) {
                                /*
                                 * ignore
                                 */
                            }
                        }
                    }

                    //if input file does not come from quick export, use value directly and check, if file exists
                    if(!isQuickExport){
                        inputFile = new File(value);
                        if(!inputFile.exists()){
                            LOGGER.error("Value \"{}\" doesn't point to an existing file.", value);
                            throw new IllegalArgumentException(String.format("Value \"{}\" doesn't point to an existing file.", value));
                        }
                    }else{
                        //check if value contained a filepath
                        if(inputFile == null){
                            LOGGER.error("Value \"{}\" did not contain a valid file path.", value);
                            throw new IllegalArgumentException(String.format("Value \"{}\" did not contain a valid file path.", value));
                        }
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

                            String content = "";

                            char[] chars = new char[8192];

                            while (bread.read(chars) != -1) {
                                content = content.concat(String.valueOf(chars));
                            }

                            content = content.trim();

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

                try {
                    messages.addMessage("Done.");
                } catch (Exception e1) {
                    /*
                     * ignore
                     */
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

        ResponseFormType responseForm = ex.addNewResponseForm();

        ResponseDocumentType responseDocument = responseForm.addNewResponseDocument();

        for (OutputDescriptionType outputDescriptionType : outputDescTypes) {

            String identifier = outputDescriptionType.getIdentifier().getStringValue();

            DocumentOutputDefinitionType output = responseDocument.addNewOutput();

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

        try {
            parameters.add(createListBoxStringParameter(labelText + "_mimetype", labelText + " mime type", mimeTypes, false));
        } catch (AutomationException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
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
