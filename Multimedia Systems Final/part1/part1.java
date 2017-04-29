package part1;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.image.BufferedImage;


class Macroblock extends BufferedImage {
	
	static int BLK_SIZE = 16;
	
	int frameIndex = 0;
	int motionVector = 0;
	
	//x,y position in the containing BufferedImage of the first
	//macroblock pixel
	int x_anchor = 0; 
	int y_anchor = 0;
	
	boolean foreground = false;
	
	Macroblock matchBlock = null; //best match from previous frame
	
	Macroblock(int x_pos, int y_pos) {
		super(Macroblock.BLK_SIZE, Macroblock.BLK_SIZE, BufferedImage.TYPE_INT_RGB);
		//this.frameIndex = frame;
		this.x_anchor = x_pos;
		this.y_anchor = y_pos;
	}
	
	Macroblock(int x_pos, int y_pos, boolean fg) {
		super(Macroblock.BLK_SIZE, Macroblock.BLK_SIZE, BufferedImage.TYPE_INT_RGB);
		//this.frameIndex = frame;
		this.x_anchor = x_pos;
		this.y_anchor = y_pos;
		this.foreground = fg;
	}
	
//	public int getFrame() {
//		return this.frameIndex;
//	}
	
//	public Macroblock getMatch() {
//		return this.matchBlock;
//	}
	
	public int getMotionVector() {
		return this.motionVector;
	}
	
	public int [] getxy() {
		int [] x_y = new int [] {this.x_anchor,this.y_anchor};
		return x_y;
	}
	
	public boolean isForeground() {
		return this.foreground;
	}
	
	public void setForeground(boolean fg) {
		this.foreground = fg;
	}
	
//	public void setMatch(Macroblock match) {
//		this.matchBlock = match;
//	}
	
	public void setMotionVector(int mv) {
		this.motionVector = mv;
	}
}

public class part1 {

	File input = null;
	int width = 0;
	int height = 0;
	ArrayList<BufferedImage> srcImages = null;  //list of frames
	HashMap<Integer,ArrayList<Macroblock>> blockMap = null; //maps frames to their Macroblock lists
	
	public part1(File infile, int w, int h) {
		
		this.input = infile;
		this.width = w;
		this.height = h;
		
		srcImages = new ArrayList<BufferedImage>();
		
		// Read frames from the video file
		this.getFrames();
		
		for (int i = 0; i < srcImages.size(); i++) {
			blockMap.put(i, this.getMacroblocks(srcImages.get(i)));
		}
	}
		
	public int calcSAD(Macroblock block1, Macroblock block2) {
		
		int SAD = 0;
		
		//TODO: get and compare pixels from the frame using index and offset
		
		return SAD;
	}
	
	public void computeMVS(int refFrameIndex, int currentFrameIndex) {
		for (Macroblock currentBlock : this.blockMap.get(currentFrameIndex)) {
			
			int lowestSAD = 0xFFFFFFFF;
			Macroblock matchBlock = null;
			for (Macroblock refBlock : this.blockMap.get(refFrameIndex)) {
				int currentSAD = this.calcSAD(currentBlock, refBlock);
				if (currentSAD < lowestSAD) {
					matchBlock = refBlock;
					lowestSAD = currentSAD;
				}
			}
		}
	}
	
	public ArrayList<Macroblock> getMacroblocks(BufferedImage frame) {
	// Returns an ArrayList of Macroblock objects for the frame
		ArrayList<Macroblock> blocks = new ArrayList<Macroblock>();
		
		//TODO: create macroblock objects from the frame
		
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
	
	private void fillMacroblockPixels(Macroblock block, BufferedImage img) {
		
		int blockSize = Macroblock.BLK_SIZE;
		int [] startPos = block.getxy();
		
		for (int x = 0; x < blockSize; x++) {
			for (int y = 0; y < blockSize; y++) {
				block.setRGB(x, y, img.getRGB(startPos[0]+x,startPos[1]+y));
			}
		}
	}
}