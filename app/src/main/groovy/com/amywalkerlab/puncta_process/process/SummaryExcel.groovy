package com.amywalkerlab.puncta_process.process

import ij.IJ
import ij.ImagePlus
import ij.WindowManager

import java.awt.Font
import java.awt.Color

import org.jfree.chart.ChartUtils 
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.axis.ValueAxis
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.renderer.category.CategoryItemRenderer
import org.jfree.chart.renderer.category.StatisticalBarRenderer
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset
import org.jfree.data.statistics.StatisticalCategoryDataset



import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.CellType
import java.util.concurrent.ConcurrentHashMap

import ij.gui.GenericDialog
import ij.IJ
import ij.ImagePlus
import ij.ImageStack
import ij.process.ColorProcessor
import ij.gui.GenericDialog
import ij.WindowManager
import java.awt.Window
import ij.Prefs
import ij.measure.ResultsTable
import ij.measure.Calibration

import io.github.dphiggs01.gldataframe.GLDataframe
import io.github.dphiggs01.gldataframe.GLDataframeException
import io.github.dphiggs01.gldataframe.utils.GLLogger

class SummaryExcel {
	String[] fluorescences = ["488", "561"]
	String directoryRoot
	String outputDir
    String outputExcel
    Workbook workbook = null
    GLLogger logger

	// Closure to add to GLDataframe
	def writeExcelSheet = { workbook, sheetName -> 
		def sheet = workbook.createSheet(sheetName)
		def mapExcelCellTypes = { schema ->
			def cellTypeMap = [
				(Integer.name): CellType.NUMERIC,
				(Double.name) : CellType.NUMERIC,
				(String.name) : CellType.STRING,
				(Boolean.name): CellType.BOOLEAN
			]
			schema.collect { schemaType ->
				cellTypeMap[schemaType.name]
			}
		}

		//List of closures that convert the input to the expected Excel data type
		def toCellValue = delegate.converters
		def cellTypes = mapExcelCellTypes(delegate.schema)
		
		if( colHeader ) {
			Row row = sheet.createRow(0)
			colHeader.eachWithIndex { cellData, cellIndex ->
				Cell cell = row.createCell(cellIndex)
				cell.cellType = CellType.STRING
				cell.setCellValue(cellData)
			}
		}

		delegate.data.eachWithIndex { rowData, rowIndex ->
			Row row = sheet.createRow( rowIndex+1 )
			rowData.eachWithIndex { cellData, cellIndex ->
				if (cellData != null) {
					Cell cell = row.createCell( cellIndex )
					cell.cellType = cellTypes[cellIndex]
					cell.setCellValue(toCellValue[cellIndex]( cellData ))
				}
			}
		}
	}

	SummaryExcel(directoryRoot){
		this.logger = GLLogger.getLogger("debug", directoryRoot) 
		this.directoryRoot = directoryRoot
		this.outputDir = directoryRoot + "/Excel_Summary"
		this.outputExcel = outputDir + "/SummaryExcel.xlsx"
		def mkdir = { dir_nm -> 
            def dir = new File(dir_nm) 
            dir.deleteDir()
            dir.mkdirs()
        }
        
        // Check if the input directory exists if it does, make the output directory if needed.
        def directoryExists = new File(directoryRoot).isDirectory()

		if (!directoryExists) {
    		IJ.error("Error", "Input Directory '$directoryRoot' does not exist.")
		} else {
			mkdir(outputDir)
			workbook = WorkbookFactory.create(true)
		}

		// Add the writeExcelSheet method to the GLDataframe
		// Since we want to keep GLDataframe lite weight we do not want all the Excel Code built in
		ExpandoMetaClass.enableGlobally()
		GLDataframe.metaClass.writeExcelSheet = this.writeExcelSheet

	}

	public void processDirectory() {
		logger.trace("START SummaryExcel.processDirectory")
		if (workbook == null) {
			logger.error("ERROR: SummaryExcel.processDirectory The 'workbook' is null but is required for processing.")
			return
		}
		
		def objectCountDataStatsList =[]
		def GLDataframe wormBarChartStats = new GLDataframe([],['name', 'mean', 'stdDev', 'chart'])
		def GLDataframe wormBarChartStat = null
		
		// Create the 3D_OC_ sheets
		// Read all the csv files in /3D_OC_ directory and get the Object Count Column of each file
		// This will give you one row for each csv
		fluorescences.each { fluorescence ->
		    def fluorescenceName = '3D_OC_' + fluorescence
			def objectCountDir = directoryRoot + "/" + fluorescenceName
			def objectCountData = readDirectoryOfObjectCountCSVs(objectCountDir)
			
			if (!objectCountData.isEmpty()) {
				objectCountData = sortDataframeByCounts(objectCountData)
				objectCountData.writeCSV(outputDir+"/"+fluorescenceName+".csv")
				objectCountData.writeExcelSheet(workbook, fluorescenceName)

				GLDataframe objectCountDataStats = objectCountData.colStats()
				objectCountDataStats.writeCSV(outputDir+"/"+fluorescenceName+"Stats.csv")
				// Postpone writing the sheet data till later
				objectCountDataStatsList.add([objectCountDataStats, fluorescenceName+"Stats"])
				
				// def title = 'Mean 3d OC ' + fluorescence
				// wormBarChartStat = wormBarChart(objectCountDataStats, 'name', 'mean', 'Mean 3d OC', title, outputDir)
				// wormBarChartStats = wormBarChartStats.concat(wormBarChartStat)
				
				// title = 'Mean Counts ' + fluorescence
				// wormBarChartStat = wormBarChart(objectCountDataStats, 'name', 'numCount', 'Mean Counts', title, outputDir)
				// wormBarChartStats = wormBarChartStats.concat(wormBarChartStat)
			}
		}

		// Create Measure_ sheets
		// Read the /measure_results_ csv file and directly write it to a sheet
		fluorescences.each { fluorescence ->
			try {
				def measureFileName = directoryRoot + '/measure_' + fluorescence + '/measure_results_' + fluorescence + '.csv'
				def measureData = GLDataframe.readCSV(measureFileName)
				if (!measureData.isEmpty()) {
					measureData.writeExcelSheet(workbook, "Measure_"+fluorescence)
					def title = 'Mean Measure ' + fluorescence
					//wormBarChartStat = wormBarChart(measureData, 'Label', 'Mean', 'Mean Measure', title, outputDir)
					//wormBarChartStats = wormBarChartStats.concat(wormBarChartStat)
				}
			} catch (GLDataframeException e) {
				logger.error("ERROR: SummaryExcel.processDirectory ${e.getMessage()}")
			}
		}
		
		// Add the stats to the end sheets of the workbook
		objectCountDataStatsList.each { objectCountDataStats ->
			def statsData = objectCountDataStats[0]
			def fluorescenceName = objectCountDataStats[1]
			statsData.writeExcelSheet(workbook, fluorescenceName)
		}
		// wormBarChartStats = wormBarChartStats.sortBy(['chart','name'])
		// wormBarChartStats = wormBarChartStats.cols(['chart', 'name','mean', 'stdDev'])
		wormBarChartStats.writeExcelSheet(workbook, "Chart Stats")
		
		// Write the workbook to the output file
		try (FileOutputStream outputStream = new FileOutputStream(outputExcel)) {
    		workbook.write(outputStream)
		}
		logger.trace("FINISH SummaryExcel.processDirectory")

	}

    private sortDataframeByCounts(threeDOCData) {
		logger.trace("START SummaryExcel.sortDataframeByCounts")
        //def groupNamesMap  = ['EV':'EV','ev':'ev','fa':'fat-7','pc':'pcyt','sa':'sams','ar':'arf','ir':'ire','xb':'xbp']
        //def groupNamesKeys = groupNamesMap.keySet()

        def sortHeadersByCounts = { dataframe ->
            def GLDataframe countsDF = dataframe.colCounts()
            def sortedCountsDF   = countsDF.sortBy(['count','name'],false)
            def sortedCountsData = sortedCountsDF.data.transpose()
            def sortedHeaders    = sortedCountsData[0]
            def sortedDF         = dataframe.cols(sortedHeaders)
            return sortedDF
        }

        def GLDataframe resultsDF = new GLDataframe()
		// By pass the mess below not sure what it is used for
		def sortedDF = sortHeadersByCounts(threeDOCData)
        resultsDF = resultsDF.join(sortedDF)

        // groupNamesKeys.each { key ->
        //     // Given a key find all the related column names in the given dataframe
        //     def found = threeDOCData.colHeader.findAll { it.startsWith(key) }
        //     if (!found.isEmpty()) {
        //         // Create a new Dataframe with only those columns
        //         def foundDF = threeDOCData.cols(found)
        //         // Sort the dataframe by column count (how many item in each column)
        //         def sortedDF = sortHeadersByCounts(foundDF)
        //         // Append the sorted data to the results dataframe
        //         resultsDF = resultsDF.join(sortedDF)
        //     }
        // }

		logger.trace("FINISH SummaryExcel.sortDataframeByCounts")
        return resultsDF
    }
		
	// Given a directory of CSV files 
	// read Nb of obj. voxels to each file
	// sort the results by columnName before returning
	private readDirectoryOfObjectCountCSVs(String directoryName) {
		logger.trace("START SummaryExcel.readDirectoryOfObjectCountCSVs")
	    //def flouresence_type = directoryName[-3..-1]
	    //def right_trim = (flouresence_type=="488") ? -15 : -13 // flouresence_type=="561"

	    def left_trim = 15 //Remove "Statistics_WTH_" from the name
	    def right_trim = -5 //Remove ".csv" from name

	    def csvFiles = (new File(directoryName)).listFiles()        
	
	    if (!csvFiles) {
	        logger.warn("WARN: SummaryExcel.readDirectoryOfObjectCountCSVs No CSV files found in the directory: $directoryName")
	        return new GLDataframe()
	    }
	
		GLDataframe result_df = new GLDataframe()
	    csvFiles.each { csvFile ->
	    	if (csvFile.name.endsWith('.csv')) {
		        def columnName = csvFile.name[left_trim..right_trim]
				//logger.log("csvFile.name $csvFile.name columnName: $columnName  left_trim $left_trim right_trim $right_trim ")
				// Read the third column of the CSV file only usecols=[2] 

				def csvFileNm = csvFile.toString()
				def header=true
				def usecols=[2] //Nb of obj. voxels
				def temp_df = GLDataframe.readCSV(csvFileNm, header, usecols)
				temp_df.colHeader = [columnName]
				result_df = result_df.join(temp_df)
				Runtime runtime = Runtime.getRuntime();
				logger.trace("TRACE: SummaryExcel.readDirectoryOfObjectCountCSVs memory usage: " + (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " MB");
	    	}
	    }			
		
		// Sort the columns alphabetically by column name 
		def colHeaderSorted = result_df.colHeader.sort()
		result_df = result_df.cols(colHeaderSorted)
		logger.trace("FINISH SummaryExcel.readDirectoryOfObjectCountCSVs")
		return result_df
	}
	
	private wormBarChart(chartDataframe, groupOnCol, calcOnCol, yLabel, title, storeInDir) { 
		logger.trace("START SummaryExcel.wormBarChart")
		if(chartDataframe.isEmpty()) {
			logger.warn("WARN: SummaryExcel.wormBarChart The chartDataframe is empty but is expected to have data")
			return new GLDataframe()
		}
	    def groupNamesMap  = ['ev':'ev','fa':'fat-7','pc':'pcyt','sa':'sams','ar':'arf','ir':'ire','xb':'xbp']
	    def groupNamesKeys = groupNamesMap.keySet()
	    def statsColumns = ['name','mean','stdDev']
	
	    // Add the column grpNm to the summary stats dataframe
	    // grpNm is a substring of the  'name' column of the current dataframe e.g [ev,ev001,ev002,ev003,ev004]
	    // grpNm should have values ['ev','fa','pc','sa'] after substring-ing the name columns
	    def groupOnColIdx = chartDataframe.getHeaderIndex(groupOnCol)
	    if (groupOnColIdx == -1) {
	    	logger.error("ERROR: SummaryExcel.wormBarChart Group On Column Name [" + groupOnCol + "] not found in [" + chartDataframe.getColHeader() + "]")
	    	return new GLDataframe()
	    }
		// Add a column 'grpNm' the contents are the first two characters from ...
	    def chartDataframeWGrp = chartDataframe.addCol('grpNm',{row -> row[0].substring(groupOnColIdx, 2)})
	
	    def chartData = [] // Hold the rows of data for the Bar Chart
	
	    // Using grpNm we can slice the dataframe into sub-dataframes based on groups
	    def grpNmColIdx = chartDataframeWGrp.getHeaderIndex('grpNm')
	    groupNamesKeys.each { grpNm ->
	        // Slice the stats dataframe based on grpNm
	        def groupSlice = chartDataframeWGrp.slice({row -> row[grpNmColIdx] == grpNm })
	        if (!groupSlice.isEmpty()) {
	            // Of the returned Stats save only the 'count' values
	            groupSlice = groupSlice.cols([calcOnCol])
	            // rename the 'count' with the category name that will be used in the chart
	            def renameColsMap = [:] 
	            renameColsMap[calcOnCol] = groupNamesMap[grpNm]
	            groupSlice = groupSlice.renameCols(renameColsMap)
	            // Now we calculate the summary stats on the counts
	            // This gives us the Avgerage and Standard Deviation of the Averages  
	            // Plus some additional stats min, max, count, etc..
	            def groupSliceStats = groupSlice.colStats()
	            // Keep only the columns we are interested in ['name','mean','stdDev']
	            groupSliceStats = groupSliceStats.cols(statsColumns)
	            // For each group slice stats dataframe there is only one row
	            // This row is the stats on the Averages 'mean' column
	            def chartCategory = groupSliceStats.getRow(0)
	            // Save this data as it is the data we want to chart
	            chartData << chartCategory
	        }
	    }
	    // Create a new dataframe with output chart data
	    GLDataframe chartOutDataframe= new GLDataframe(chartData, statsColumns)
	    PipelineCharts.categoryBarChart(chartOutDataframe, yLabel, title, storeInDir)
	    chartOutDataframe = chartOutDataframe.addCol('chart', {title})
		logger.trace("FINISH SummaryExcel.wormBarChart")
	    return chartOutDataframe

	}
}

