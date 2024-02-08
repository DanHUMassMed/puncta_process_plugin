package com.amywalkerlab.puncta_process.process

import ij.IJ
import ij.ImagePlus
import ij.WindowManager
import java.awt.Window

import ij.measure.ResultsTable
import ij.measure.Calibration


class BatchMeasure extends ProcessDirectory {

    BatchMeasure(String directoryRoot, String fluorescence, String inputDirPrefix = "crop_trim", String suffix = ".tif") {
        super(directoryRoot, fluorescence, inputDirPrefix, "measure", suffix)
    }
    
    
    public void processDirectory() {
    	def args="choose='" + inputDir +"'"
    	IJ.run("Measure...", args);
    	IJ.selectWindow("Results")
    	//IJ.saveAs("Results", outputDir + "/measure_results_" + fluorescence + ".csv")
    	
    	def resultsListOfLists = []
    	def resultsTable = ResultsTable.getResultsTable()
		// Check if there are any results
		if (resultsTable.getCounter() > 0) {
		    // Collect the results List
		    for (int row = 0; row < resultsTable.size(); row++) {
		        def rowValue = resultsTable.getRowAsString(row)
		        def parsedRow = rowValue.split('\t')
		        parsedRow = parsedRow[1..-1] //Remove the index
				resultsListOfLists << parsedRow
		 		//IJ.log("Row $row: $parsedRow")
		    }
		}
		writeCSV(resultsListOfLists)
		
    	Window currentWindow = WindowManager.getWindow("Results")
    	if (currentWindow != null) {
    		currentWindow.close(false)
    	}   
	}
    

	private writeCSV(resultsListOfLists){
		// Existing master list of lists with header
		def header = ["Label", "Area", "Mean", "Min", "Max"]
		
		def sortedListOfLists = resultsListOfLists.sort { a, b -> a[0] <=> b[0] }
		sortedListOfLists.add(0, header)
		
		def csvContent = sortedListOfLists.collect { it.join(',') }.join('\n')
		
		new File(outputDir + "/measure_results_" + fluorescence + ".csv").text = csvContent		
	}
	
	@Override
	protected void processFile(File file) {
		 throw new RuntimeException("ProcessFile() is not a valid method for BatchMeasure.")
	    
	}
}
