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

import java.awt.GridLayout;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.n52.client.arcmap.util.InputTypeEnum;
import org.n52.geoprocessing.wps.client.WPSClientSession;
import org.n52.geoprocessing.wps.client.model.AllowedValues;
import org.n52.geoprocessing.wps.client.model.ComplexInputDescription;
import org.n52.geoprocessing.wps.client.model.ExceptionReport;
import org.n52.geoprocessing.wps.client.model.Format;
import org.n52.geoprocessing.wps.client.model.InputDescription;
import org.n52.geoprocessing.wps.client.model.LiteralInputDescription;
import org.n52.geoprocessing.wps.client.model.OutputDescription;
import org.n52.geoprocessing.wps.client.model.Process;
import org.n52.geoprocessing.wps.client.model.ResponseMode;
import org.n52.geoprocessing.wps.client.model.Result;
import org.n52.geoprocessing.wps.client.model.StatusInfo;
import org.n52.geoprocessing.wps.client.model.TransmissionMode;
import org.n52.geoprocessing.wps.client.model.execution.BoundingBoxData;
import org.n52.geoprocessing.wps.client.model.execution.ComplexData;
import org.n52.geoprocessing.wps.client.model.execution.ComplexDataReference;
import org.n52.geoprocessing.wps.client.model.execution.Data;
import org.n52.geoprocessing.wps.client.model.execution.Execute;
import org.n52.geoprocessing.wps.client.model.execution.ExecuteOutput;
import org.n52.geoprocessing.wps.client.model.execution.ExecutionMode;
import org.n52.geoprocessing.wps.client.model.execution.LiteralData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.datainterop.FMEDestDatasetType;
import com.esri.arcgis.datasourcesfile.DEFile;
import com.esri.arcgis.datasourcesfile.DEFileType;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.geodatabase.GPMessage;
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

    private String version = "1.0.0";

    public WPSFunction() {

    }

    public WPSFunction(String toolName) {
        try {
            String[] identifierAndURL = toolName.split("@");

            this.toolName = toolName;
            this.displayName = identifierAndURL[0];
            try {
                this.version = identifierAndURL[2];
            } catch (Exception e) {
                LOGGER.error("Could not extract version from tool name: " + identifierAndURL + ". Possibly an old process was started with a new version of the client. Falling back to version 1.0.0.");
            }
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

        LOGGER.trace("Creating parameter array for " + toolName + " a.k.a " + displayName);

        parameters = new Array();

        String[] identifierAndURL = toolName.split("@");

        String wpsURL = identifierAndURL[1];

        WPSClientSession session = WPSClientSession.getInstance();

        Process descriptionType = null;

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
                session.connect(wpsURL, version);
            }

            /**
             * TODO: do that maybe beforehand, if we want the input and output
             * descriptions for the process already at the process selection
             * panel
             */
            descriptionType = session.getProcessDescription(wpsURL, displayName, version);

            progressFrame.setVisible(false);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            JOptionPane.showMessageDialog(progressFrame, "Could not connect to: \n" + wpsURL + ".", "52 North WPS ArcMap Client", 1);
            progressFrame.setVisible(false);
            return null;
        }

        List<InputDescription> dataInputs = descriptionType.getInputs();

        Iterator<InputDescription> dataInputsIterator = dataInputs.iterator();

        while(dataInputsIterator.hasNext()) {

            final InputDescription currentDescription = dataInputsIterator.next();

            String labelText = currentDescription.getId();

            InputTypeEnum type = checkType(currentDescription);

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
                if (currentDescription.getMinOccurs() > 0) {
                    complexParameter.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
                } else {
                    complexParameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
                }

                complexParameter.setDataTypeByRef(composite);
                complexParameter.setValueByRef(new GPString());
                parameters.add(complexParameter);

                addSchemaMimeTypeEncodingToParameters(parameters, currentDescription);

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
                if (currentDescription.getMinOccurs() > 0) {
                    literalParameter.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
                } else {
                    literalParameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
                }

                LiteralInputDescription literalInputDescription = (LiteralInputDescription)currentDescription;

                String dataTypeString = literalInputDescription.getDataType();

                boolean noDataTypeFound = false;

                if(dataTypeString != null) {
                    noDataTypeFound = true;
                }

//                if (dataType != null) {
//
//                    String dataTypeString = currentDescription.getLiteralData().getDataType().getReference();
//
//                    if (dataTypeString == null) {
//                        dataTypeString = currentDescription.getLiteralData().getDataType().getStringValue();
//                    }
//                    if (dataTypeString != null) {
//
//                        if (dataTypeString.contains("double")) {
//                            // parameter2.setDataTypeByRef(new GPDoubleType());
//                            // parameter2.setValueByRef(new GPDouble());
//                            /*
//                             * try this to avoid decimal point/comma issues
//                             */
//                            literalParameter.setDataTypeByRef(new GPStringType());
//                            literalParameter.setValueByRef(new GPString());
//                        } else if (dataTypeString.contains("string")) {
//                            literalParameter.setDataTypeByRef(new GPStringType());
//                            literalParameter.setValueByRef(new GPString());
//                        } else if (dataTypeString.contains("integer") || dataTypeString.contains("int")) {
//                            literalParameter.setDataTypeByRef(new GPLongType());
//                            literalParameter.setValueByRef(new GPLong());
//                        } else if (dataTypeString.contains("dateTime")) {
//                            //TODO: use String here as format of dateTime is not clear
//                            literalParameter.setDataTypeByRef(new GPStringType());
//                            literalParameter.setValueByRef(new GPString());
//                        } else if (dataTypeString.contains("boolean") || dataTypeString.contains("bool")) {
//                            literalParameter.setDataTypeByRef(new GPBooleanType());
//                            literalParameter.setValueByRef(new GPBoolean());
//                        } else if (dataTypeString.contains("float")) {
//                            literalParameter.setDataTypeByRef(new GPDoubleType());
//                            literalParameter.setValueByRef(new GPDouble());
//                        } else {
//                            noDataTypeFound = true;
//                        }
//                    } else {
//                        noDataTypeFound = true;
//                    }
//
//                } else {
//                    noDataTypeFound = true;
//                }

                try {

                    if (literalInputDescription.getAllowedValues() != null) {

                        AllowedValues allowedValuesSuperType = literalInputDescription.getAllowedValues();

                        List<String> allowedValues = allowedValuesSuperType.getAllowedValues();

                        IGPCodedValueDomain domain = new GPCodedValueDomain();

                        for (String allowedValue : allowedValues) {

                            LOGGER.trace("Allowed value " + allowedValue);

                            domain.addStringCode(allowedValue, allowedValue);
                        }
                        // Assign the domain to the parameter.
                        literalParameter.setDomainByRef((IGPDomain) domain);

                    }
                    if (!noDataTypeFound) {
                        parameters.add(literalParameter);
                    } else {
                        literalParameter.setDataTypeByRef(new GPStringType());

                        Object defaultValue = literalInputDescription.getDefaultValue();

                        if (defaultValue != null && !defaultValue.equals("")) {

                            GPString gpString = new GPString();

                            gpString.setValue(defaultValue.toString());

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

        for (OutputDescription outDescType : descriptionType.getOutputs()) {
            // TODO add support for literal-/bboxdata
//            if (outDescType.getComplexOutput() == null) {
//                LOGGER.info("Skipping non-complex output {}", outDescType.getId());
//                continue;
//            }

            String identifier = outDescType.getId();

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

                LOGGER.trace("check " + tmpParameter.getName());
                LOGGER.trace("Value: " + tmpParameterValue.getAsText());
            }
        } catch (AutomationException e) {
            LOGGER.error("Error in updating parameters method", e);
        } catch (IOException e) {
            LOGGER.error("Error in updating parameters method", e);
        } catch (NullPointerException e) {
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

            WPSClientSession.getInstance().connect(wpsURL, version);

            Map<String, String> parameterNameValueMap = getParameterNameValueMap(paramvalues);

            Process process = WPSClientSession.getInstance().getProcessDescription(wpsURL, displayName, version);

            Execute execute = createExecuteDocument(parameterNameValueMap, process, messages);

            if (execute == null) {
                return;
            }

            //only debug execute document if smaller than 5 MB
            if(execute.toString().length() < 5242880){
                LOGGER.debug(execute.toString());
            }

            try {
                messages.addMessage("Sending POST request to WPS.");
            } catch (Exception e1) {
                /*
                 * ignore
                 */
            }

            Object response = WPSClientSession.getInstance().execute(wpsURL, execute, version);

            try {
                messages.addMessage("Got response.");
            } catch (Exception e1) {
                /*
                 * ignore
                 */
            }

            if (response instanceof Result) {

                handleExecuteResponse((Result) response, paramvalues, parameterNameValueMap, messages, trackcancel);

            } else if (response instanceof StatusInfo) {

                handleExecuteResponse(((StatusInfo) response).getResult(), paramvalues, parameterNameValueMap, messages, trackcancel);

            } else if (response instanceof ExceptionReport) {
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

    private void handleExecuteResponse(Result responseDoc,
            IArray paramvalues,
            Map<String, String> parameterNameValueMap,
            IGPMessages messages,
            ITrackCancel trackcancel) throws IOException, TransformerFactoryConfigurationError, TransformerException {

        for (Data outputDataType : responseDoc.getOutputs()) {

            String identifier = outputDataType.getId();

            String outputPath = parameterNameValueMap.get(outputPrefix + identifier);

            File outputFile = new File(outputPath);

//            String mimeType = parameterNameValueMap.get(identifier + "_mimetype");
//
//            String extension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
//
//            if (extension == null || extension.equals("")) {
//                extension = "dat";
//            }

            LOGGER.trace("Writing " + identifier + " output to " + outputFile.getAbsolutePath());
            messages.addMessage("Writing " + identifier + " output to " + outputFile.getAbsolutePath());

            if(outputDataType instanceof ComplexData) {

                ComplexData complexData = (ComplexData)outputDataType;

                String s = "";

                if (complexData.isReference()) {

                    URL href = complexData.getReference().getHref();

                    messages.addMessage("Start result download from " + href);

                    download(href, outputFile, messages);

                } else {

                    Object cData = complexData.getValue();

                    s = cData.toString();

                    BufferedWriter bwr = new BufferedWriter(new FileWriter(outputFile));

                    bwr.write(s);

                    bwr.close();
                }

            } else if(outputDataType instanceof LiteralData) {
              //TODO
            } else if(outputDataType instanceof BoundingBoxData) {
                //TODO

            }
        }
    }

    private void download(URL url,
            File resultFile,
            IGPMessages messages) throws IOException {

        java.io.BufferedInputStream in = new java.io.BufferedInputStream(url.openStream());
        java.io.FileOutputStream fos = new java.io.FileOutputStream(resultFile);
        java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);

        int bufferSize = 1024;
        byte[] data = new byte[bufferSize];
        int x = 0;
        int count = 1;
        while ((x = in.read(data, 0, bufferSize)) >= 0) {
            bout.write(data, 0, x);
            if(count == 1) {
                messages.addMessage("Downloaded " + bufferSize*count + " bytes.");
            }else {
                updateDownloadMessage(bufferSize*count, messages);
            }
            count++;
        }
        bout.close();
        in.close();
    }

    private void updateDownloadMessage(int i,
            IGPMessages messages) throws AutomationException, IOException {
        int messagePosition = messages.getCount() - 1;
        GPMessage message = new GPMessage();
        message.setDescription("Downloaded " + i + " bytes.");
        messages.replace(messagePosition, message);
    }

    private Execute createExecuteDocument(Map<String, String> parameterNameValueMap,
            Process process,
            IGPMessages messages) {

        Execute ex = new Execute();

        ex.setId(process.getId());

        ex.setResponseMode(ResponseMode.DOCUMENT);

        ex.setExecutionMode(ExecutionMode.ASYNC);

        List<InputDescription> inputDescTypes = process.getInputs();

        Iterator<InputDescription> dataInputsIterator = inputDescTypes.iterator();

        while(dataInputsIterator.hasNext()) {

            InputDescription inputDescriptionType = dataInputsIterator.next();

            String identifier = inputDescriptionType.getId();

            InputTypeEnum type = checkType(inputDescriptionType);

            String value = parameterNameValueMap.get(identifier);

            if (value == null) {
                continue;
            }

            LOGGER.trace("Value " + value);

            switch (type) {
            case Complex:
                /*
                 * TODO: add strategy for empty schema/mimetype/encoding
                 */
                String schema = parameterNameValueMap.get(identifier + "_schema");
                LOGGER.trace("Schema = " + schema);

                String mimeType = parameterNameValueMap.get(identifier + "_mimetype");
                LOGGER.trace("Mime Type = " + mimeType);

                String encoding = parameterNameValueMap.get(identifier + "_encoding");
                LOGGER.trace("Encoding = " + encoding);

                String isReference = parameterNameValueMap.get(identifier + "_reference");
                LOGGER.trace("IsReference = " + isReference);

                ComplexData executeInput = new ComplexData();

                executeInput.setId(identifier);

                if (isReference != null && Boolean.parseBoolean(isReference)) {

                    ComplexDataReference inHref = new ComplexDataReference();

                    URL referenceURL;
                    try {
                        referenceURL = new URL(value);
                    } catch (MalformedURLException e) {
                        LOGGER.error("Could not form URL from value: " + value, e);
                        break;
                    }

                    inHref.setHref(referenceURL);

                    executeInput.setReference(inHref);

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


                    BufferedReader bufferedReader;
                    try {
                        bufferedReader = new BufferedReader(new FileReader(inputFile));

                    String content = "";

                    String line = "";

                    while((line = bufferedReader.readLine()) != null) {
                        content = content.concat(line);
                    }

                    executeInput.setValue(content);

                    } catch (FileNotFoundException e) {
                        LOGGER.error("File not found:" + value, e);
                        break;
                    } catch (IOException e) {
                        LOGGER.error("Could not read file: " + value, e);
                    }
                }

                Format format = new Format();

                if (mimeType != null && !mimeType.equals("")) {

                    format.setMimeType(mimeType);
                }

                if (schema != null && !schema.equals("")) {

                    format.setSchema(schema);
                }

                if (encoding != null && !encoding.equals("")) {

                    format.setEncoding(encoding);
                }
                executeInput.setFormat(format);

                ex.addInput(executeInput);

                try {
                    messages.addMessage("Done.");
                } catch (Exception e1) {
                    /*
                     * ignore
                     */
                }

                break;

            case Literal:

                LiteralData literalInput = new LiteralData();

                literalInput.setId(identifier);

                String literalDatatype = ((LiteralInputDescription)inputDescriptionType).getDataType();

                literalInput.setDataType(literalDatatype);

                literalInput.setValue(value);

                ex.addInput(literalInput);

                break;
            default:
                break;
            }
        }
        try {
            addResponseForm(process, ex, parameterNameValueMap, messages);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return ex;
    }

    private void addResponseForm(Process pDescType,
            Execute ex,
            Map<String, String> parameterNameValueMap,
            IGPMessages messages) throws IllegalArgumentException {

        ArrayList<OutputDescription> outputDescTypes = new ArrayList<OutputDescription>(pDescType.getOutputs().size());

        List<OutputDescription> outputDescriptions = pDescType.getOutputs();

        for (OutputDescription outDescType : outputDescriptions) {
            if (parameterNameValueMap.get(outputPrefix + outDescType.getId()) != null) {
                outputDescTypes.add(outDescType);
            }
        }

        for (OutputDescription outputDescriptionType : outputDescTypes) {

            String identifier = outputDescriptionType.getId();

            ExecuteOutput output = new ExecuteOutput();

            output.setId(identifier);
            /*
             * TODO: add strategy for empty schema/mimetype/encoding
             */
            String schema = parameterNameValueMap.get(identifier + "_schema");
            LOGGER.trace("Schema = " + schema);

            String mimeType = parameterNameValueMap.get(identifier + "_mimetype");
            LOGGER.trace("Mime Type = " + mimeType);

            String encoding = parameterNameValueMap.get(identifier + "_encoding");
            LOGGER.trace("Mime Type = " + encoding);

            String isReference = parameterNameValueMap.get(identifier + "_reference");
            LOGGER.trace("IsReference = " + isReference);

            if (isReference != null && Boolean.parseBoolean(isReference)) {
                output.setTransmissionMode(TransmissionMode.REFERENCE);
                ex.setResponseMode(ResponseMode.DOCUMENT);
            }

            Format outputFormat = new Format();

            if (mimeType != null && !mimeType.equals("")) {
                outputFormat.setMimeType(mimeType);
            }
            if (encoding != null && !encoding.equals("")) {
                outputFormat.setEncoding(encoding);
            }
            if (schema != null && !schema.equals("")) {
                outputFormat.setSchema(schema);
            }

            output.setFormat(outputFormat);

            ex.addOutput(output);
        }

    }

    private Map<String, String> getParameterNameValueMap(IArray paramvalues) {
        try {
            Map<String, String> result = new HashMap<String, String>(paramvalues.getCount());

            for (int i = 0; i < paramvalues.getCount(); i++) {
                IGPParameter tmpParameter = (IGPParameter) paramvalues.getElement(i);
                IGPValue tmpParameterValue = gpUtilities.unpackGPValue(tmpParameter);
                if (!tmpParameterValue.getAsText().equals("")) {
                    LOGGER.trace("added " + tmpParameter.getName());
                    result.put(tmpParameter.getName(), tmpParameterValue.getAsText());
                } else {
                    LOGGER.trace("Omitted " + tmpParameter.getName());
                    LOGGER.trace("Value: " + tmpParameterValue);
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

    private InputTypeEnum checkType(InputDescription inputDescription) {

        if (inputDescription instanceof ComplexInputDescription) {
            return InputTypeEnum.Complex;
        } else if (inputDescription instanceof LiteralInputDescription) {
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

    //TODO use supertype and merge with method below
    private void addSchemaMimeTypeEncodingToParameters(IArray params,
            InputDescription inputDescription) {

        String labelText = inputDescription.getId();

        Set<String> schemas = new HashSet<String>();
        Set<String> mimeTypes = new HashSet<String>();
        Set<String> encodings = new HashSet<String>();

        Format defaultFormat = inputDescription.getDefaultFormat();

        String defaultSchema = defaultFormat.getSchema();
        String defaultEncoding = defaultFormat.getEncoding();

        if (defaultSchema != null && !defaultSchema.equals("")) {
            schemas.add(defaultSchema);
        }
        if (defaultEncoding != null && !defaultEncoding.equals("")) {
            encodings.add(defaultEncoding);
        }
        mimeTypes.add(defaultFormat.getMimeType());

        List<Format> supportedFormats = inputDescription.getFormats();

        for (Format format : supportedFormats) {

            //TODO skip default format?

            String supportedSchema = format.getSchema();
            String supportedEncoding = format.getEncoding();

            if (supportedSchema != null && !supportedSchema.equals("")) {
                schemas.add(supportedSchema);
            }

            if (supportedEncoding != null && !supportedEncoding.equals("")) {
                encodings.add(supportedEncoding);
            }

            mimeTypes.add(format.getMimeType());
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

    //TODO use supertype and merge with method above
    private void addSchemaMimeTypeEncodingToParameters(Array parameters2,
            OutputDescription outputDescription) {
        String labelText = outputDescription.getId();

        Set<String> schemas = new HashSet<String>();
        Set<String> mimeTypes = new HashSet<String>();
        Set<String> encodings = new HashSet<String>();

        Format defaultFormat = outputDescription.getDefaultFormat();

        String defaultSchema = defaultFormat.getSchema();
        String defaultEncoding = defaultFormat.getEncoding();

        if (defaultSchema != null && !defaultSchema.equals("")) {
            schemas.add(defaultSchema);
        }
        if (defaultEncoding != null && !defaultEncoding.equals("")) {
            encodings.add(defaultEncoding);
        }
        mimeTypes.add(defaultFormat.getMimeType());

        List<Format> supportedFormats = outputDescription.getFormats();

        for (Format format : supportedFormats) {

            //TODO skip default format?

            String supportedSchema = format.getSchema();
            String supportedEncoding = format.getEncoding();

            if (supportedSchema != null && !supportedSchema.equals("")) {
                schemas.add(supportedSchema);
            }

            if (supportedEncoding != null && !supportedEncoding.equals("")) {
                encodings.add(supportedEncoding);
            }

            mimeTypes.add(format.getMimeType());
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

}
