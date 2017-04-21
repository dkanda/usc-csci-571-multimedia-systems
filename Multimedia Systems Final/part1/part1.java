package part1;

import java.io.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;


class Macroblock {
	
	int motionVector = 0;
	int x_pos = 0;
	int y_pos = 0;
	boolean foreground = false;
	int [][] pixels = new int[16][16];
}

public class part1 {

	File input = null;
	int width = 0;
	int height = 0;
	ArrayList<BufferedImage> srcImages = null;

	
	public part1(File infile, int w, int h) {
		
		this.input = infile;
		this.width = w;
		this.height = h;
		
		srcImages = new ArrayList<BufferedImage>();
		
		// Read frames from the video file
		this.getFrames();
		
	}
		
		
	public ArrayList<Macroblock> getMacroblocks(BufferedImage frame) {
	// Determines motion vectors, and returns a full ArrayList of Macroblock objects
	// for the frame
		ArrayList<Macroblock> blocks = new ArrayList<Macroblock>();
		
		return blocks;
	}
		
	private void getFrames() {
				
		InputStream is = null;
		int srcWidth = this.width;
		int srcHeight = this.height;
		
		try {
			is = new FileInputStream(this.input);
		}
		catch (FileNotFoundException e) {
			System.out.println("Error opening input file.");
			e.printStackTrace();
		} 
		
		
		// Get the source images
		System.out.println("Gathering original video frames...");
		long frameLength = srcWidth * srcHeight * 3; // Three bytes per pixel
		long fileLength = this.input.length();
		byte[] bytes = new byte[(int)frameLength];

		int fileOffset = 0;
		while (fileOffset < fileLength) {
		
			int offset = 0;
			int numRead = 0;
			try {
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
			}
			catch (IOException e) {
				System.out.println("Error reading bytes from the file stream.");
				e.printStackTrace();
			}
			fileOffset += offset;
	
	
			BufferedImage img = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_RGB);
			int ind = 0;
			for(int y = 0; y < srcHeight; y++){
	
				for(int x = 0; x < srcWidth; x++){
	
					byte r = bytes[ind];
					byte g = bytes[ind+srcHeight*srcWidth];
					byte b = bytes[ind+srcHeight*srcWidth*2]; 
	
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
			
			// Clear the byte array
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = 0;
			}
			
			// Put this frame in the frames ArrayList
			this.srcImages.add(img);
		}
		
		
		//Source images created; close input stream
		try {
			is.close();
		}
		catch (IOException e) {
			System.out.println("Error trying to close input stream.");
			e.printStackTrace();
		}
	}
}
			

