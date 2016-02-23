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
package org.n52.client.arcmap.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.WPSCapabilitiesType;

import org.n52.client.arcmap.WPSFunctionFactory;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.catalog.GxCatalog;
import com.esri.arcgis.catalog.GxToolbox;
import com.esri.arcgis.catalog.GxToolboxesFolder;
import com.esri.arcgis.catalog.IEnumGxObject;
import com.esri.arcgis.catalog.IGxObject;
import com.esri.arcgis.catalogUI.IGxApplication;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.geoprocessing.IEnumGPToolboxName;
import com.esri.arcgis.geoprocessing.IGPFunction;
import com.esri.arcgis.geoprocessing.IGPFunctionTool;
import com.esri.arcgis.geoprocessing.IGPFunctionToolProxy;
import com.esri.arcgis.geoprocessing.IGPTool;
import com.esri.arcgis.geoprocessing.IGPToolbox;
import com.esri.arcgis.geoprocessing.IGPToolboxName;
import com.esri.arcgis.geoprocessing.IToolboxWorkspace;
import com.esri.arcgis.geoprocessing.IToolboxWorkspaceProxy;
import com.esri.arcgis.geoprocessing.ToolboxWorkspaceFactory;
import com.esri.arcgis.geoprocessing.esriGPToolType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.version.VersionManager;

/**
 * <code>jDialog</code> form used for connecting to WPS servers and adding processes to ArcMap.
 *
 * @author Benjamin Pross
 *
 */
public class ArcMapWPSClientDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 4074655130230106574L;

    private static Logger LOGGER = LoggerFactory.getLogger(ArcMapWPSClientDialog.class);

    private IApplication app;

    private static ArcMapWPSClientDialog instance;

    private ProcessBriefType[] processes;

    private ArrayList<String> selectedProcesses;

    private HashMap<DefaultMutableTreeNode, Object> treeWPSCapsMap;

    private javax.swing.JButton getProcessesButton;

    private javax.swing.JButton removeButton;

    private javax.swing.JButton reconnectButton;

    private javax.swing.JButton cancelButton;

    private javax.swing.JButton okButton;

    private javax.swing.JComboBox wpsUrlsComboBox;

    private javax.swing.JComboBox versionComboBox;

    private javax.swing.JEditorPane processDetailsEditorPane;

    private javax.swing.JLabel urlLabel;

    private javax.swing.JLabel exampelLabel;

    private javax.swing.JLabel versionLabel;

    private javax.swing.JLabel exampleWPSLabel;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel serverProcessesPanel;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JSplitPane jSplitPane1;

    private javax.swing.JTree processTree;

    private File arcmapWPSClientAppData;

    public static ArcMapWPSClientDialog getInstance(IApplication app) {

        if (instance == null) {
            instance = new ArcMapWPSClientDialog(app);
        }
        return instance;
    }

    private void initComponents() {

        urlLabel = new javax.swing.JLabel();
        wpsUrlsComboBox = new javax.swing.JComboBox();
        exampelLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        versionComboBox = new javax.swing.JComboBox();
        exampleWPSLabel = new javax.swing.JLabel();
        serverProcessesPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        processDetailsEditorPane = new javax.swing.JEditorPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        processTree = new javax.swing.JTree();
        processTree.setVisible(false);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        processTree.setCellRenderer(renderer);
        getProcessesButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        reconnectButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/image/52n48x48-transp.png")).getImage());
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        urlLabel.setText("URL:");
        urlLabel.setName("urlLabel");

        wpsUrlsComboBox.setEditable(true);
        wpsUrlsComboBox.setName("wpsUrlsCombobox");

        try {
            fillWPSURLComboBox();
        } catch (IOException e3) {
            LOGGER.error(e3.getMessage());
            JOptionPane.showMessageDialog(null, "Could not recieve stored WPS URLs.", "52°North WPS ArcMap Client", 1);
        }

        exampelLabel.setText("Example:");
        exampelLabel.setName("exampelLabel");

        versionLabel.setText("Version:");
        versionLabel.setName("versionLabel");

        versionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default Version", "1.0.0" }));
        versionComboBox.setName("jComboBox2");

        exampleWPSLabel.setText("http://geoprocessing.demo.52north.org:8080/wps/WebProcessingService");
        exampleWPSLabel.setName("exampleWPSLabel");

        serverProcessesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Server Processes"));
        serverProcessesPanel.setName("serverProcessesPanel");

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setEnabled(false);
        jSplitPane1.setName("jSplitPane1");

        jPanel1.setName("jPanel1");

        jScrollPane2.setName("jScrollPane2");

        processDetailsEditorPane.setEditable(false);

        processDetailsEditorPane.setName("jEditorPane1");
        jScrollPane2.setViewportView(processDetailsEditorPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 308,
                Short.MAX_VALUE));

        jSplitPane1.setRightComponent(jPanel1);

        jPanel2.setName("jPanel2");

        jScrollPane1.setName("jScrollPane1");

        processTree.setName("jTree1");
        jScrollPane1.setViewportView(processTree);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 198,
                Short.MAX_VALUE));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 308,
                Short.MAX_VALUE));

        jSplitPane1.setLeftComponent(jPanel2);

        getProcessesButton.setText("Get Processes");
        getProcessesButton.setName("getProcessesButton");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(serverProcessesPanel);
        serverProcessesPanel.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel3Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                                        .addComponent(getProcessesButton)).addContainerGap()));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel3Layout.createSequentialGroup().addComponent(getProcessesButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(53, Short.MAX_VALUE)));

        try {
            removeButton.setIcon(new javax.swing.ImageIcon(this.getClass().getResource("/image/33.png")));
            removeButton.setToolTipText("Remove WPS from list.");
        } catch (Exception e2) {
            LOGGER.error(e2.getMessage());
        }
        removeButton.setName("removeButton");

        try {
            reconnectButton.setIcon(new javax.swing.ImageIcon(this.getClass().getResource("/image/42.png")));
            reconnectButton.setToolTipText("Reconnect.");
        } catch (Exception e2) {
            LOGGER.error(e2.getMessage());
        }
        reconnectButton.setName("reconnectButton");

        cancelButton.setText("Cancel");
        cancelButton.setName("cancelButton");

        okButton.setText("OK");
        okButton.setMaximumSize(new java.awt.Dimension(67, 23));
        okButton.setMinimumSize(new java.awt.Dimension(67, 23));
        okButton.setName("okButton");
        okButton.setPreferredSize(new java.awt.Dimension(67, 23));

        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                WPSClientSession.getInstance().disconnect(getWPSUrl());
                processTree.setVisible(false);
                /*
                 * remove url from combobox
                 */
                removeWPSURL(getWPSUrl());
            }
        });

        reconnectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                WPSClientSession.getInstance().disconnect(getWPSUrl());
                try {
                    WPSClientSession.getInstance().connect(getWPSUrl());
                    processTree.setVisible(false);
                    connectToWPS();
                } catch (WPSClientException e1) {
                    LOGGER.error(e1.getMessage());
                }

            }
        });

        getProcessesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                connectToWPS();
            }
        });

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    finish(getSelectedProcesses());
                } catch (Exception e1) {
                    LOGGER.error(e1.getMessage());
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dispose();
                } catch (Exception e1) {
                    LOGGER.error(e1.getMessage());
                }
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(serverProcessesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(
                                                layout.createSequentialGroup()
                                                        .addGroup(
                                                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(exampelLabel).addComponent(urlLabel)
                                                                        .addComponent(versionLabel))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(
                                                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(layout.createSequentialGroup().addGap(1, 1, 1).addComponent(exampleWPSLabel))
                                                                        .addComponent(versionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        // .addComponent(jComboBox1,
                                                                        // 0,
                                                                        // 357,
                                                                        // Short.MAX_VALUE))
                                                                        .addComponent(wpsUrlsComboBox, 0, 357, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(reconnectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(10, 10, 10))
                                        .addGroup(
                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                layout.createSequentialGroup()
                                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18).addComponent(cancelButton))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                layout.createSequentialGroup().addGap(19, 19, 19).addComponent(urlLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(exampelLabel))
                                        .addGroup(
                                                layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addGroup(
                                                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                        .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(reconnectButton))
                                                                        .addGroup(
                                                                                layout.createSequentialGroup()
                                                                                        .addComponent(wpsUrlsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        // .addComponent(jComboBox1,
                                                                                        // javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        // javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        // javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                        .addComponent(exampleWPSLabel)
                                                                                        .addGap(14, 14, 14)
                                                                                        .addGroup(
                                                                                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                        .addComponent(versionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addComponent(versionLabel))))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(serverProcessesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                        .addGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(cancelButton)
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap()));

        pack();
    }

    private void connectToWPS() {

        try {

            String url = wpsUrlsComboBox.getSelectedItem().toString().trim();

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }

            boolean wpsURLAlreadyInComboboxItems = false;

            for (int i = 0; i < wpsUrlsComboBox.getItemCount(); i++) {
                if (wpsUrlsComboBox.getItemAt(i).equals(url)) {
                    wpsURLAlreadyInComboboxItems = true;
                    break;
                }
            }
            if (!wpsURLAlreadyInComboboxItems) {
                wpsUrlsComboBox.addItem(url);
            }

            WPSClientSession.getInstance().connect(url);

            CapabilitiesDocument capsDoc = WPSClientSession.getInstance().getWPSCaps(url);

            processes = capsDoc.getCapabilities().getProcessOfferings().getProcessArray();

            treeWPSCapsMap = new HashMap<DefaultMutableTreeNode, Object>(processes.length + 1);

            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("WPS");

            treeWPSCapsMap.put(rootNode, capsDoc.getCapabilities());

            /*
             * sort the processes
             */
            Map<String, ProcessBriefType> sortMap = new HashMap<String, ProcessBriefType>(processes.length);

            for (ProcessBriefType processBriefType : processes) {
                sortMap.put(processBriefType.getIdentifier().getStringValue(), processBriefType);
            }
            String[] sortedIDs = new String[processes.length];
            sortMap.keySet().toArray(sortedIDs);

            Arrays.sort(sortedIDs);

            for (String id : sortedIDs) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(id);
                treeWPSCapsMap.put(node, sortMap.get(id));
                rootNode.add(node);
            }

            DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

            processTree.setModel(treeModel);
            processTree.setVisible(true);
            processTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            processTree.addTreeSelectionListener(new TreeSelectionListener() {

                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    Object o = e.getNewLeadSelectionPath().getLastPathComponent();

                    Object o1 = treeWPSCapsMap.get(o);
                    if (o1 != null) {
                        if (o1 instanceof ProcessBriefType) {
                            ProcessBriefType pBrief = (ProcessBriefType) o1;

                            String id = pBrief.getIdentifier().getStringValue();
                            String title = "";
                            if (pBrief.getTitle() != null) {
                                title = pBrief.getTitle().getStringValue();
                            }

                            String abstractS = "";
                            if (pBrief.getAbstract() != null) {
                                abstractS = pBrief.getAbstract().getStringValue();
                            }

                            processDetailsEditorPane.setText("Identifier:" + "\n" + id + "\n\n" + "Title:" + "\n" + title + "\n\n" + "Abstract:" + "\n" + abstractS);
                        } else if (o1 instanceof WPSCapabilitiesType) {
                            WPSCapabilitiesType wpsCaps = (WPSCapabilitiesType) o1;

                            processDetailsEditorPane.setText("Title:" + "\n" + wpsCaps.getServiceIdentification().getTitleArray(0).getStringValue() + "\n\n" + "Abstract:\n"
                                    + wpsCaps.getServiceIdentification().getAbstractArray(0).getStringValue());

                        }
                    }

                }
            });

        } catch (Exception e1) {
            JOptionPane.showMessageDialog(null, "" + e1);
            LOGGER.error(e1.getMessage());
        }

    }

    /**
     * Returns the processes selected in the <code>processTree</code>.
     *
     * @return <code>ArrayList<String></code> containing the selected processes
     */
    public ArrayList<String> getSelectedProcesses() {

        TreePath[] paths = processTree.getSelectionPaths();

        selectedProcesses = new ArrayList<String>(paths.length);

        for (TreePath treePath : paths) {
            selectedProcesses.add((String) ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject());
        }

        return selectedProcesses;
    }

    /**
     * Returns the URL of the selected WPS.
     *
     * @return <code>String</code> containing the URL of the selected WPS
     */
    public String getWPSUrl() {
        return wpsUrlsComboBox.getSelectedItem().toString().trim();
    }

    public ArcMapWPSClientDialog(IApplication appl) {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        instance = this;

        this.app = appl;
        setTitle("52 North ArcMap WPS Client");
        setSize(508, 605);
        setResizable(false);
        initComponents();
        setAlwaysOnTop(true);
    }

    /**
     * Empties specified directory of all files, deletes and re-creates it
     *
     * @param dirName
     *            String
     */
    public void cleanAndRecreateDirectory(String dirName) {
        cleanAndDeleteDirectory(dirName);
        File dir = new File(dirName);
        dir.mkdir();
    }

    /**
     * Deletes all files in specified directory and then deletes the directory
     * as well
     *
     * @param Path
     *            String
     */
    public void cleanAndDeleteDirectory(String Path) {
        File src = new File(Path);
        if (src.isDirectory() && src.exists()) {
            File list[] = src.listFiles();
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    cleanAndDeleteDirectory(list[i].getPath());
                    list[i].delete();
                } else {
                    list[i].delete();
                }
            }
            src.delete();
        } else {
            src.delete();
        }
    }

    /**
     * Returns the current <code>IApplication</code>.
     *
     * @return the current <code>IApplication</code>
     */
    public IApplication getApplication() {
        return app;
    }



    /**
     * Called when the dialog is closed.
     */
    public void dispose() {
        super.dispose();
        try {
            saveWPSURLs();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        this.setVisible(false);
        ArcMapWPSClientDialog.instance = null;
    }

    @SuppressWarnings("deprecation")
    private void finish(ArrayList<String> selectedProcesses) throws AutomationException, IOException {

        IGxApplication application = (IGxApplication) app;

        // did not find another way to do this...
        GxCatalog gxCatalog = new GxCatalog(application.getCatalog());

        IEnumGxObject catchildren = gxCatalog.getChildren();

        IGxObject child1 = catchildren.next();

        GxToolboxesFolder conti = null;

        while (child1 != null) {

            if (child1.getFullName().equals("Toolboxes")) {
                conti = new GxToolboxesFolder(child1);
                break;
            }

            child1 = catchildren.next();
        }

        IEnumGxObject children = conti.getChildren();

        IGxObject child = children.next();

        GxToolbox toolboxObject = null;

        while (child != null) {

            if (child.getName().equals("My Toolboxes")) {

                int[] pCode = new int[1];
                String[] pVer = new String[1];
                String[] pPath = new String[1];

                new VersionManager().getActiveVersion(pCode, pVer, pPath);

                // did not find another way to do this...
                toolboxObject = new GxToolbox(child);

                ToolboxWorkspaceFactory fac = new ToolboxWorkspaceFactory();

                File toolboxFolder = new File(System.getenv("APPDATA") + "\\ESRI\\Desktop" + pVer[0] + "\\ArcToolbox\\My Toolboxes");

                IToolboxWorkspace worp = new IToolboxWorkspaceProxy(fac.openFromFile(toolboxFolder.getAbsolutePath(), 0));

                String wpsToolBoxName = "WPS";

                IEnumGPToolboxName tbNames = worp.getToolboxNames();

                IGPToolboxName tbName = tbNames.next();

                boolean wpsToolboxExists = false;

                while (tbName != null) {
                    if (tbName.getPathName().endsWith(wpsToolBoxName)) {
                        wpsToolboxExists = true;
                        break;
                    }
                    tbName = tbNames.next();
                }

                IGPToolbox tbox = null;

                if (wpsToolboxExists) {
                    tbox = worp.openToolbox(wpsToolBoxName);
                } else {
                    tbox = worp.createToolbox(wpsToolBoxName, "WPS Toolbox");
                }

                String url = getWPSUrl();

                WPSFunctionFactory wpsfac = new WPSFunctionFactory();

                for (String identifier : selectedProcesses) {

                    wpsfac.functionNames.add(identifier + "@" + url);

                    IGPFunction function = wpsfac.getFunction(identifier + "@" + url);

                    IGPTool tool = tbox.createTool(esriGPToolType.esriGPFunctionTool, identifier, identifier, identifier, url, null);

                    IGPFunctionTool functiontool = new IGPFunctionToolProxy(tool);

                    functiontool.setFunctionByRef(function);

                    tool.store();
                }
                toolboxObject.refresh();
                break;
            }
            child = children.next();
        }
        dispose();
    }

    private void fillWPSURLComboBox() throws IOException {
        arcmapWPSClientAppData = new File(System.getenv("appdata") + File.separator + "52North" + File.separator + "WPS ArcMap Client" + File.separator + "wpsurls.txt");

        if (arcmapWPSClientAppData.exists()) {

            wpsUrlsComboBox.addItem("");

            BufferedReader bread = new BufferedReader(new FileReader(arcmapWPSClientAppData));

            String line = "";

            while ((line = bread.readLine()) != null) {
                if (!line.isEmpty() && !line.equals(" ") && !line.equals("")) {
                    wpsUrlsComboBox.addItem(line);
                }
            }

            bread.close();

        } else {
            LOGGER.info("File not found: {}", arcmapWPSClientAppData.getAbsolutePath());
        }
    }

    private void removeWPSURL(String wpsURL) {
        wpsUrlsComboBox.removeItem(wpsURL);
    }

    private void saveWPSURLs() throws IOException {
        BufferedWriter bwrite = new BufferedWriter(new FileWriter(arcmapWPSClientAppData));

        for (int i = 0; i < wpsUrlsComboBox.getItemCount(); i++) {
            bwrite.write(wpsUrlsComboBox.getItemAt(i) + "\n");
        }

        bwrite.close();
    }
}
