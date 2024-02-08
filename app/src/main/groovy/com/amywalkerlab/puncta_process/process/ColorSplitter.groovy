package com.amywalkerlab.puncta_process.process

import ij.IJ
import ij.ImagePlus
import ij.ImageStack
import ij.measure.Calibration
import ij.process.ColorProcessor

class ColorSplitter  extends ProcessDirectory {
 	String[] colors = ["red", "green", "blue"]
 	String[] fluorescences = ["561", "488"]
    String holdDir
    
    ColorSplitter(String directoryRoot, String fluorescence, String inputDirPrefix = "crop_trim", String suffix = ".tif") {
    	super(directoryRoot, fluorescence, inputDirPrefix, "color_split", suffix)
        this.holdDir = directoryRoot + "/color_hold"
        def mkdir = { dir_nm -> (new File(dir_nm)).mkdirs() }
        mkdir(holdDir)    
    }
    
	// Handle each individual file
	@Override
	protected void processFile(File file) {
	    def String full_name = file.toString()
	    def ImagePlus imp = IJ.openImage(full_name)
		//imp.show()
		splitRGB(imp)
		imp.close()
	}


	// The images that we are given are RGB, and we will split them into individual channels
	private void splitRGB(ImagePlus imp) {
		String title = imp.getTitle();
		Calibration cal = imp.getCalibration();
		ImageStack[] channels = splitRGB(imp.getStack(), false);
		channels.eachWithIndex { channel, index ->
		    def String file_nm = title[0..-5]+"_"+colors[index]
			def ImagePlus channel_imp = new ImagePlus(file_nm, channel);
			channel_imp.setCalibration(cal)
			
			def String saveToDir = holdDir
			def index_fluorescence = fluorescences.findIndexOf { it == fluorescence }
            // If the color index is equal to the fluorescences save the image to the outputDir
            // If the color is red save and fluorescences is 561 then we want to save this to outputDir for further processing
            // If the color is green and the fluorescences is 488 then we want to save this to outputDir for further processing
            // All other images just go to the hold directory
			if(index == index_fluorescence) {
		    	saveToDir = outputDir			
			}
			
	    	IJ.saveAs(channel_imp, "Tiff", saveToDir+"/"+file_nm)
		}
	}


	private ImageStack[] splitRGB(ImageStack rgb, boolean keepSource) {
		 int w = rgb.getWidth();
		 int h = rgb.getHeight();
		 ImageStack[] channels = new ImageStack[3];
		 for (int i=0; i<3; i++)
		 	channels[i] = new ImageStack(w,h);
		 byte[] r,g,b;
		 ColorProcessor cp;
		 int slice = 1;
		 int inc = keepSource?1:0;
		 int n = rgb.getSize();
		 for (int i=1; i<=n; i++) {
			 r = new byte[w*h];
			 g = new byte[w*h];
			 b = new byte[w*h];
			 cp = (ColorProcessor)rgb.getProcessor(slice);
			 slice += inc;
			 cp.getRGB(r,g,b);
			 if (!keepSource)
				rgb.deleteSlice(1);
			 channels[0].addSlice(null,r);
			 channels[1].addSlice(null,g);
			 channels[2].addSlice(null,b);
			
		}
		return channels;
	}
}
