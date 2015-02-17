nuxeo-renditions-cmis-sample
===========================

## About this module

This module is an addon for Nuxeo Platform and more specifically the Renditions.

## About Renditions

The current rendition system provides 3 models of renditions :

 - Live computed renditions : blob is generated on the fly
    - ex : PDF rendition
 - Rendition based on a stored Blob
    - ex : PictureView renditions are actually compulted by the PictureView system in an asynchronous manner
 - Rendition published as a dedicated document
    - ex : Publish a Note as a PDF Rendition

The first model of rendition seems to be the model used by a majority of people since they can simply use an Automation Chain to build their rendition and eventually access it via simple URL or CMIS API.

Sadly this first model does not provide any direct caching mechanism (although the underlying Conversions that can be used will).

To make this whole process easier (and then safer) we could provide inside the platform some infrastructure to :

 - start Rendition Computation in an async / Tx safe way
 - provide a caching mechanism

Some people seems to use the fact that Renditions are exposed via CMIS to expose custom processing they build inside Nuxeo using Automation :

 - Custom tree XML export
 - CSV rendition of a folder
 - ...

Provided we do the infrastructure work inside Nuxeo, we still need to define how we handle that inside CMIS.

As suggested in [CMIS-883](https://issues.apache.org/jira/browse/CMIS-883), what we could do is :

 - if rendition is not available in cache :
    - start the generation process
    - return a 0 bytes rendition
 - return the "cached rendition if available"

This module is a sandbox to test this infrastructure.
