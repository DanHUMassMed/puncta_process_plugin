package com.amywalkerlab.puncta_process.process

import ij.IJ
import ij.ImagePlus
import ij.WindowManager

class ZProject extends ProcessDirectory {
    String args

    ZProject(String directoryRoot, String fluorescence, String args, String inputDirPrefix = "crop_trim", String suffix = ".tif") {
        super(directoryRoot, fluorescence, inputDirPrefix, "max", suffix)
        this.args = args
    }
    

	@Override
	protected void processFile(File file) {
	    def String full_name = file.toString()
	    def String file_name = file.name
	    def ImagePlus imp = IJ.openImage(full_name)
		//imp.show()
	    IJ.run(imp, "Z Project...", args)
	    sleep(100)
	    def maxImp = WindowManager.getImage("MAX_" + file_name)
	    if (maxImp != null) {
			IJ.saveAs(maxImp, "Tiff", outputDir+"/Max_" + file_name)
	    	maxImp.close()
	    } else {
	    	logger.error("ERROR: ZProject.processFile MaxIntensity Image not found!")
	    }
		imp.close()
	}
}
