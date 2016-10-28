# WPS Client fo ArcGIS [![OpenHUB](https://www.openhub.net/p/wps-arcmap-client/widgets/project_thin_badge.gif)](https://www.openhub.net/p/wps-arcmap-client)

# Description
This client wraps WPS processes as standard ArcGIS Geoprocessing tools. With the client it is possible to select one or more layers in ArcMap and use them as input for WPS processes. The result of the processes can be visualized in ArcMap again.

# Getting Started and configuration

* Get the latest Version here [WPS Client fo ArcGIS 1.1.0 installer](http://52north.org/downloads/send/128-extensibleclient/430-52n-extensible-wps-arcmap-client-1-1-0-setup)

# License

The WPS Client fo ArcGIS is published under The Apache Software License, Version 2.0. 

# User guide/tutorial

See here : [Geoprocessing Wiki](https://wiki.52north.org/Geoprocessing/ExtensibleClient)

# Demo

* [WPS Client for ArcMap overview](https://www.youtube.com/watch?v=y5VzPkrEoPw)
* [Add a WPS process to ArcMap](https://www.youtube.com/watch?v=k0UhD1vr-cg)
* [Interaction with the Model Builder ](https://www.youtube.com/watch?v=SkuLOJAav3k)

# Changelog

  * Changes since last release
    * New features
      * Add mechanism to update the status of a WPS4R process 
      * Add a self-cleaning file input stream implementation for Postgres database
      * Raise an exception if an annotated Algorithm has no @Execute annotation
      * Empty port and webapp path allowed for WPS URL
  
    * Changes
      * Use moving code packages version 1.1  
      * Removed outdated python module.
      * Switch to Rserve from maven central
      * GRASS backend works with current GRASS 7 release
  
    * Fixed issues
      * Issue #123: Admin console not working when using Tomcat 6
      * Issue #173: Databinding issue with WPS4R
      * Issue #222: Save configuration with active R backend results in duplicate algorithm entries

# References

* [AcUser Fall 2016 article](http://www.esri.com/esri-news/arcuser/fall-2016/sharing-geoprocessing-tools-via-the-web)

# Contact

Benjamin Pross

b.pross (at) 52north.org

# Credits

 * GSoC
 * GLUES
 * Ordnance Survey
