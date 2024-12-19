package com.amywalkerlab.puncta_process.process

import ij.IJ
import io.github.dphiggs01.gldataframe.GLDataframe
import io.github.dphiggs01.gldataframe.GLDataframeException
import io.github.dphiggs01.gldataframe.utils.GLLogger

abstract class ProcessDirectory {
    String directoryRoot
    String fluorescence
    String inputDir
    String outputDir
    String suffix
    GLLogger logger

    ProcessDirectory(String directoryRoot, String fluorescence, String inputDirPrefix, String outputDirPrefix, String suffix = ".tif") {
        this.logger = GLLogger.getLogger("debug", directoryRoot) 
        this.directoryRoot = directoryRoot
        this.fluorescence = fluorescence
        this.inputDir = directoryRoot + "/" + inputDirPrefix + "_" + fluorescence
        this.outputDir = directoryRoot + "/" + outputDirPrefix+ "_" + fluorescence
        this.suffix = suffix
        def mkdir = { dir_nm -> 
            def dir = new File(dir_nm) 
            dir.deleteDir()
            dir.mkdirs()
        }
        
        // Check if the input directory exists if it does, make the output directory if needed.
        def directoryExists = new File(inputDir).isDirectory()
    
		if (!directoryExists) {
    		IJ.error("Error", "Input Directory '$inputDir' does not exist.")
		} else {
			mkdir(outputDir)
		}
    }

    // Process all the files in the provided directory
    public void processDirectory() {
        logger.log("processDirectory $inputDir")
        def list = (new File(inputDir)).listFiles()
        
        list.each { file ->        
            if (file.isDirectory()) {
                processDirectory(file)
            }
            
            logger.log("file.name " + file.name)
            if (file.name.endsWith(suffix)) {
                processFile(file)
            }
        }
    }

    // Abstract method to be implemented by subclasses
    protected abstract void processFile(File file)
}
