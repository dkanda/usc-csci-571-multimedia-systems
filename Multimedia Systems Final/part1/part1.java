package part1;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.lang.Exception;


class Macroblock extends BufferedImage {
	
	static int BLK_SIZE = 16;
	
	int frameIndex = 0;
	int [] motionVector = new int[2];
	
	//x,y position in the containing BufferedImage of the first
	//macroblock pixel
	int x_anchor = 0; 
	int y_anchor = 0;
	
	boolean foreground = false;
	boolean foregroundChecked = false;
	
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
	
	public int [] getMotionVector() {
		return this.motionVector;
	}
	
	public int [] getxy() {
		int [] x_y = new int [] {this.x_anchor,this.y_anchor};
		return x_y;
	}
	
	public boolean isForeground() {
		return this.foreground;
	}
	
	public boolean isChecked() {
		return this.foregroundChecked;
	}
	
	public void setForeground(boolean fg) {
		this.foreground = fg;
	}
	
	public void setForegroundChecked(boolean checked) {
		this.foregroundChecked = checked;
	}
	
//	public void setMatch(Macroblock match) {
//		this.matchBlock = match;
//	}
	
	public void setMotionVector(int [] mv) {
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
		
	private int calcSAD(Macroblock block1, Macroblock block2) {
		
		int SAD = 0;
		
		for (int x = 0; x < Macroblock.BLK_SIZE; x++) {
			for (int y = 0; y > Macroblock.BLK_SIZE; y++) {
				SAD += (Math.abs(block1.getRGB(x, y) - block2.getRGB(x, y)));
			}
		}
		
		return SAD;
	}
	
	private void computeMVS(int refFrameIndex, int currentFrameIndex) {
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
			
			// At this point, the lowest SAD has been found
			int [] motionVector = new int [2];
			int [] currentXY = currentBlock.getxy();
			int [] refXY = matchBlock.getxy();
			
			motionVector[0] = currentXY[0] - refXY[0];
			motionVector[1] = currentXY[1] - refXY[1];
			
			currentBlock.setMotionVector(motionVector);
		}
	}
	
	private void decideForeground(ArrayList<Macroblock> blockList) throws Exception {
		int numColumns = this.width / Macroblock.BLK_SIZE;
		int numRows = this.height / Macroblock.BLK_SIZE;
		ArrayList<Macroblock> foregroundList = new ArrayList<Macroblock>();
		ArrayList<Macroblock> backgroundList = new ArrayList<Macroblock>();
		
		int mvErrorMargin = 2;
		
		for (int i = 0; i < blockList.size(); i++) {
			
			if (blockList.get(i).isChecked()) {
				continue;
			}
			
			// Get the current motion vector array for comparison
			int [] currentMV = blockList.get(i).getMotionVector();
			ArrayList<Macroblock> neighborBlocks = new ArrayList<Macroblock>();
			
			// Get surrounding blocks for comparison
			if ((i - 1) >= 0) {
				neighborBlocks.add(blockList.get(i-1));
			}
			
			if ((i + 1) < numColumns) {
				neighborBlocks.add(blockList.get(i+1));
			}
			
			if ((i - numColumns) >= 0) {
				neighborBlocks.add(blockList.get(i-numColumns));
			}
			
			if ((i + numColumns) < numRows) {
				neighborBlocks.add(blockList.get(i+numColumns));
			}
			
			//TEST: Just a check to make sure all blocks are accounted for
			if ((foregroundList.size() + backgroundList.size()) < blockList.size()) {
				throw new Exception("WARNING: Not all macroblocks were classified foreground/background.");
			}
			
			// Assumption 1: If there was no motion, set current block and matching
			// neighbors to background. Otherwise, set to foreground.
			if ((currentMV[0] <= mvErrorMargin) && (currentMV[1] <= mvErrorMargin)) {
				blockList.get(i).setForegroundChecked(true);
				backgroundList.add(blockList.get(i));
				
				//Check neighbors
				for (Macroblock neighbor : neighborBlocks) {
					
					if (neighbor.isChecked()) {
						continue;
					}
					
					if ((neighbor.getMotionVector()[0] - currentMV[0]) < mvErrorMargin
							&& (neighbor.getMotionVector()[1] - currentMV[1]) < mvErrorMargin) {
						neighbor.setForegroundChecked(true);
						backgroundList.add(neighbor);
					}
				}
			}
			else {
				blockList.get(i).setForegroundChecked(true);
				foregroundList.add(blockList.get(i));
				
				//Check neighbors
				for (Macroblock neighbor : neighborBlocks) {
					
					if (neighbor.isChecked()) {
						continue;
					}
					
					if ((neighbor.getMotionVector()[0] - currentMV[0]) < mvErrorMargin
							&& (neighbor.getMotionVector()[1] - currentMV[1]) < mvErrorMargin) {
						neighbor.setForegroundChecked(true);
						foregroundList.add(neighbor);
					}
				}
			}
			
			// Assumption 2: There will be more background macroblocks
			// than foreground macroblocks
			if (foregroundList.size() > backgroundList.size()) {
				// We need to switch the values
				for (Macroblock block : foregroundList) {
					block.setForeground(false);
				}
				
				for (Macroblock block : backgroundList) {
					block.setForeground(true);
				}
			}
		}
	}
	
	private ArrayList<Macroblock> getMacroblocks(BufferedImage frame) {
	// Returns an ArrayList of Macroblock objects for the frame
		ArrayList<Macroblock> blocks = new ArrayList<Macroblock>();
		
		int blockSize = Macroblock.BLK_SIZE;
		int numBlocks = (this.width * this.height) / (blockSize * blockSize);
		int blockOffset = 0;
		int xOffset = 0;
		int yOffset = 0;
		
		while (blockOffset < numBlocks) {
			Macroblock block = new Macroblock(xOffset, yOffset);
			for (int x = xOffset; x < xOffset + blockSize; x++) {
				for (int y = yOffset; y < yOffset + blockSize; y++) {
					block.setRGB(x, y, frame.getRGB(x, y));
					yOffset++;
				}
				xOffset++;
			}
			blocks.add(block);
			blockOffset++;
		}		
		return blocks;
	}

//	private void fillMacroblockPixels(Macroblock block, BufferedImage img) {
//		
//		int blockSize = Macroblock.BLK_SIZE;
//		int [] startPos = block.getxy();
//		
//		for (int x = 0; x < blockSize; x++) {
//			for (int y = 0; y < blockSize; y++) {
//				block.setRGB(x, y, img.getRGB(startPos[0]+x,startPos[1]+y));
//			}
//		}
//	}
	
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
			
			this.resizeFrame(img);
			
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
	
	private void resizeFrame(BufferedImage img) {
		int new_width = this.width;
		int new_height = this.height;
		
		while (new_width % Macroblock.BLK_SIZE != 0) {
			new_width++;
		}
		
		while (new_height % Macroblock.BLK_SIZE != 0) {
			new_height++;
		}
		
		for (int x = this.width; x < new_width; x++) {
			for (int y = 0; y < this.height; y++) {
				img.setRGB(x, y, img.getRGB(x-1, y));
			}
		}
		
		for (int y = this.height; y < new_height; y++) {
			for (int x = 0; x < new_width; x++) {
				img.setRGB(x, y, img.getRGB(x, y-1));
			}
		}
		
		this.width = new_width;
		this.height = new_height;
	}
	
	public HashMap<Integer,ArrayList<Macroblock>> doPart1() throws Exception {
		
		int frameIndex = 0;
		
		this.getFrames();
		
		for (BufferedImage frame : this.srcImages) {
			this.resizeFrame(frame);
			this.blockMap.put(frameIndex, this.getMacroblocks(frame));
			
			if (frameIndex != 0) {
				this.computeMVS(frameIndex-1, frameIndex);
				
				try {
					this.decideForeground(this.blockMap.get(frameIndex));
				}
				catch (Exception ex) {
					throw ex;
				}
			}
		}
		
		return this.blockMap;
	}
}