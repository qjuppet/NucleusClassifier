package ch.epfl.single_cell_classifier.plugin;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.io.IOService;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;

@Plugin(type = Command.class, label = "Rename", menu = {
		@Menu(label = MenuConstants.PLUGINS_LABEL, weight = MenuConstants.PLUGINS_WEIGHT, mnemonic = MenuConstants.PLUGINS_MNEMONIC),
		@Menu(label = "Single Cell Classifier"),
		@Menu(label = "Utilities"),
		@Menu(label = "Rename", weight = 5)
}) 
public class Renamer implements Command {

	@Parameter
	protected OpService opService;

	@Parameter
	protected DatasetService datasetService;

	@Parameter
	protected CommandService command;

	@Parameter
	protected IOService ioService;

	@Parameter(label="Source Directory (*)", style = "directory")
	private File sourceDirectory;

	@Parameter(label="Prefix (*)")
	private String prefix;

	@Parameter(label="Output File (.csv) (*)", style = "save")
	private File outputFile;

	@Parameter(label="Verbose")
	private boolean verbose = true;

	@Override
	public void run() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

			int i = 1;
			for(File sourceFile : sourceDirectory.listFiles()){
				if(verbose)
					IJ.log("Processing " + sourceFile.getName());

				String previousName = sourceFile.getName();				
				String newName = prefix + String.format("%02d", i) + ".tif";
				String newPath = sourceDirectory.getAbsolutePath() + "\\" + newName;
				
				ImagePlus imp = new Opener().openImage(sourceFile.getAbsolutePath());
				new ImageConverter(imp).convertToRGBStack();
				
				ImagePlus copy = getCopy(imp, newName);
				new ImageConverter(copy).convertToRGB();
				
				writer.write(newName + " : " + previousName);
				writer.newLine();
				
				IJ.saveAsTiff(copy, newPath);
				
				++i;
			}
			writer.close();
			if(verbose)
				IJ.log("Renaming completed");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ImagePlus getCopy(ImagePlus imp, String title) {
		ImagePlus out = IJ.createHyperStack(title, imp.getWidth(), imp.getHeight(), imp.getNChannels(), 1, 1, 8);
		for(int c = 0; c < imp.getNChannels(); ++c) {
			imp.setC(c + 1);
			out.setC(c + 1);
			ImageProcessor ip = imp.getProcessor();
			ImageProcessor outP = out.getProcessor();

			for(int x = 0; x < imp.getWidth(); ++x) {
				for(int y = 0; y < imp.getHeight(); ++y) {
					int value = (int) ip.getPixelValue(x, y);
					outP.putPixelValue(x, y, value);
				}
			}
		}
		
		out.setCalibration(imp.getCalibration());
		
		return out;
	}
}
