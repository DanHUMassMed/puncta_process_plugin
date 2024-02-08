package com.amywalkerlab.puncta_process.process

import java.awt.Font
import java.awt.Color

import org.jfree.chart.ChartUtils 
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.axis.ValueAxis
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset

import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.renderer.category.CategoryItemRenderer
import org.jfree.chart.renderer.category.StatisticalBarRenderer
import org.jfree.data.statistics.StatisticalCategoryDataset

public class PipelineCharts {
    
    public static categoryBarChart(chartData, yLabel, title, storeInDir) {

        StatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset()
        chartData.data.each { record ->
            def label =  record[0]
            def value =  record[1]==null ? 0: record[1].toDouble()
            def stdDev = record[2]==null ? 0: record[2].toDouble()
            dataset.add(value, stdDev, "Category", label);
        }

        final CategoryAxis xAxis = new CategoryAxis("RNAi");
        xAxis.setLowerMargin(0.05d); // percentage of space before first bar
        xAxis.setUpperMargin(0.05d); // percentage of space after last bar
        xAxis.setCategoryMargin(0.5d); // percentage of space between categories
        final ValueAxis yAxis = new NumberAxis(yLabel);

        // define the plot
        final CategoryItemRenderer renderer = new StatisticalBarRenderer();
        renderer.setSeriesPaint(0, new Color(21,96,130)) //DARK_BLUE
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        final JFreeChart chart = new JFreeChart(title,
                                          new Font("Helvetica", Font.BOLD, 14),
                                          plot,
                                          false);

        final ChartPanel chartPanel = new ChartPanel(chart);
        
        int width = 640;    /* Width of the image */
        int height = 480;   /* Height of the image */ 
        def outFileNm = title.replaceAll("['\", `@]", '_')
        File barChartFile = new File(storeInDir + "/" + outFileNm+".png" ); 
        ChartUtils.saveChartAsPNG( barChartFile , chart , width , height );
        chartData.writeCSV(storeInDir + "/Chart_" + outFileNm+".csv" )
    }
}

