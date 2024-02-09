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

		// The threshold can not exceed the max stats value for the image
		def argsForThisRun = args
        def thresholdValue = getThresholdValue(argsForThisRun)
		if (thresholdValue != null) {
			def maxValue = getMaxValue(imp)
			if(thresholdValue > maxValue){
				def maxStr = maxValue.toString()
				argsForThisRun = argsForThisRun.replaceFirst(/threshold=\d+/, "threshold=$maxStr")
				logger.log("INFO: Updating the threshold for this image to $maxStr")
        	} 
		} 

	    IJ.run(imp, "3D Objects Counter", argsForThisRun)
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

	// Does the args string contain the 'threshold=' parameter
	// and if so what is the value.
	private getThresholdValue(String args) {
		def thresholdValue = null
		def matcher = (args =~ /threshold=(\d+)/)
		if (matcher.find()) {
    		thresholdValue = matcher.group(1).toInteger()
		}
		return thresholdValue
	}

	// Get Max stats value for image
	private getMaxValue(ImagePlus imp) {
		//def min=Math.pow(2, imp.getBitDepth());
		def Integer max=0
		def nbSlices=imp.getStackSize()
		for (int i=1; i<=nbSlices; i++) {
			imp.setSlice(i)
			//min=Math.min(min, imp.getStatistics().min)
			max=Math.max(max, imp.getStatistics().max)
		}
		return max     
	}
}
