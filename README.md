# CLion-descamcheck
A CLion plugin that interfaces with descam_check.py to inspect SystemC-PPA files for compliance and display lint messages.  

Screenshots
===========

![Settings](/demo/Settings.PNG)


Features
========

 - descam_check.py is used to check your SystemC-PPA model implementation for compliance.
 - Suspicious code is highlighted and lint messages are displayed.
 - The plugin supports both Unix and Windows based systems.

 
Usage
=====

 - Download the descamcheck plugin from [plugin link](https://github.com/ludwig247/DeSCAM/tree/feature-logging/plugins/CLion)
 - Drag & drop the plugin onto CLion to install it.
 - Go to File -> Settings -> DeSCAM Check -> fill in the absolute paths to <b>*python*</b> and <b>*descam_check.py*</b>
 - To check a file, Go to Code -> Run Inspection by Name... (or CTRL + ALT + SHIFT + I) -> type descam and hit enter
 - Set the inpection scope to File and click OK.
 - If suspicious code is found, a new window will show up with lint messages. 
 - Click on a message to highlight suspicious code.