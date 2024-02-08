package com.amywalkerlab.puncta_process.process

import ij.IJ
import ij.ImagePlus
import ij.WindowManager
import java.awt.Window

class ThreeDObjectsCounter extends ProcessDirectory {
    String args
  
    ThreeDObjectsCounter(String directoryRoot, String fluorescence, String args, String inputDirPrefix = "White_Top_Hat", String suffix = ".tif") {
    	super(directoryRoot, fluorescence, inputDirPrefix, "3D_OC", suffix)
        this.args = args
    }

	@Override
	protected void processFile(File file) {
	    def String full_name = file.toString()
	    def String file_name = file.name
	    def ImagePlus imp = IJ.openImage(full_name)
		//imp.show()
	    IJ.run(imp, "WL 3D Objects Counter", args)
	    sleep(100)
	    IJ.selectWindow("Statistics for "+file_name)
    	IJ.saveAs("Results", outputDir + "/Statistics_" + file_name[0..-5] + ".csv")
    	
    	Window currentWindow = WindowManager.getWindow("Statistics_" + file_name[0..-5] + ".csv")
    	if (currentWindow != null) {
    		currentWindow.close()
    	}else{
    		currentWindow = WindowManager.getWindow("Statistics for "+file_name)
    		if (currentWindow != null) {
    			currentWindow.close()
    		}
    	}
		imp.close()
	}
}
