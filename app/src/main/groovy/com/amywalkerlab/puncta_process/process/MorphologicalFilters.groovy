package com.amywalkerlab.puncta_process.process

import ij.IJ
import ij.ImagePlus
import ij.WindowManager

class MorphologicalFilters extends ProcessDirectory {
    String args

    MorphologicalFilters(String directoryRoot, String fluorescence, String args, String inputDirPrefix = "color_split", String suffix = ".tif") {
        super(directoryRoot, fluorescence, inputDirPrefix, "White_Top_Hat", suffix)
        this.args = args
    }
    

	@Override
	protected void processFile(File file) {
	    def String full_name = file.toString()
	    def String file_name = file.name
	    def ImagePlus imp = IJ.openImage(full_name)
		//imp.show()
		logger.log("processFile " + file.name)
	    IJ.run(imp, "Morphological Filters (3D)", args)
	    def wth_imp = getWhiteTopHatImage()
	    if (wth_imp != null) {
			IJ.saveAs(wth_imp, "Tiff", outputDir+"/WTH_" + file_name)
	    	wth_imp.close()
	    } else {
	    	logger.error("ERROR: MorphologicalFilters.processFile White Top Hat Image not found!")
	    }
		imp.close()
	}
	
	def extractOperationValue(String inputString) {
		// Use a regular expression to find the word inside the square brackets following "operation="
		def matcher = inputString =~ /operation=\[(.*?)\]/
		
		if (matcher) {
			return matcher[0][1]  // Return the first match group (the word inside the brackets)
		} else {
			return null  // Return null if no match is found
		}
	}

    // Iterate the active images and return the White Top Hat Image
	// Assume there is only one White Top Hat Image as we close them after processing
	private ImagePlus getWhiteTopHatImage() {
		def topHatImage = null
	    WindowManager.getIDList().each { id ->
	        def imp = WindowManager.getImage((int) id)
	    	def title = imp.getTitle()
			def title_prefix = extractOperationValue(this.args)
	    	if (title.contains(title_prefix)) {
	    		topHatImage = imp
	    		return false //Stop iteration
	    	}
	    }
		return topHatImage
	}
}
