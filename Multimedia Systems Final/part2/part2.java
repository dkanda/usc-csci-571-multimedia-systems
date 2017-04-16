package part2;

import java.io.*;

import kanzi.SliceIntArray;
import kanzi.transform.DCT8;

public class part2 {
	byte[] bytes;
	int[] types;
	int width, height, numFrames;
	String fileName;
	FileOutputStream fos;
	DataOutputStream dos;
	int[] blockDCT8srcArray,blockDCT8dstArray;
	SliceIntArray blockDCT8src, blockDCT8dst;
	DCT8 dct8;
// For debugging
//	int[] blockIDCT8dstArray;
//	SliceIntArray blockIDCT8dst;
	
	public part2(byte[] b, int w, int h, String f) {
		bytes = b;
		width  = w;
		height = h;
		fileName = f;
		dct8 = new DCT8();
		blockDCT8srcArray = new int[64];
		blockDCT8dstArray = new int[64];
		if (0 != bytes.length % (width*height*3)) {
			throw new RuntimeException("Inavlid byte width, height and/or input array");
		}
		numFrames = bytes.length/(width*height*3);
		blockDCT8src = new SliceIntArray(blockDCT8srcArray, 0);
		blockDCT8dst = new SliceIntArray(blockDCT8dstArray, 0);
		// For debugging
		// blockIDCT8dst = new SliceIntArray(blockDCT8dstArray, 0);
		File file = new File(fileName);
		try {
			fos = new FileOutputStream(file);
			dos = new DataOutputStream(fos);
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}
	
	// Write block type to the stream
	public void writeType(int type) throws IOException {
		dos.writeInt(type);
	}
	
	// Write coefficients for an 8x8 block to the stream
	public void writeBlock() throws IOException {
		for (int i=0; i<64; ++i) {
			dos.writeInt(blockDCT8dst.array[i]);
		}
	}
	
	// Apply DCT transform on an 8x8 block
	public void encodeBlock(int row, int col) {
		// Need to reset index because this method is repeatedly
		// using the same pair of SliceIntArray objects
		blockDCT8src.index = 0;
		blockDCT8dst.index = 0;
		int blockIntArrayIndex = 0;
		// Populate DCT src array
		for (int r=row; r<row+8; ++r) {
			for (int c=col; c<col+8; ++c) {
				blockDCT8src.array[blockIntArrayIndex] = bytes[r*width + c] & 0xff;
				blockIntArrayIndex++;
			}
		}
		// Forward DCT transform
		if (false == dct8.forward(blockDCT8src, blockDCT8dst)) {
			throw new RuntimeException("forward failed");
		}
//		FIXME following is only for debugging
//		// Inverse DCT transform
//		blockDCT8dst.index = 0;
//		blockIDCT8dst.index = 0;
//		if (false == dct8.inverse(blockDCT8dst, blockIDCT8dst)) {
//			throw new RuntimeException("inverse failed");
//		}
//		for (int i=0;i<64; ++i) {
//			if (blockDCT8src.array[i] != blockIDCT8dst.array[i]) {
//				int l = blockDCT8src.array[i];
//				int r = blockIDCT8dst.array[i];
////				throw new RuntimeException("differ " + Integer.toString(l) + " " + Integer.toString(r));
////				System.out.println("differ " + Integer.toString(l) + " " + Integer.toString(r));
//					
//			}
//		}
	}
	
	// Iterate through each 8x8 block in a frame, 
	// apply transform and write coefficients to file
	public void encodeFrame(int frame) throws IOException {
		int row = frame * height * 3;
		for (int r=row; r<row+height; r+=8) {
			for (int c=0; c<width; c+=8) {
				//Assuming that types the motion compensation
				//based foreground/background separation gives
				//an int array indicating the type of the block
				//writeType(types[(r/3)/16 * height/16 + c/16]);
				//TODO currently hardcoding type to 0 
				writeType(0);
				encodeBlock(r           , c); // R
				writeBlock();
				encodeBlock(r + 1*height, c); // G
				writeBlock();
				encodeBlock(r + 2*height, c); // B
				writeBlock();
			}
		}
	}
	
	public void encode() {
		try {
			for (int i=0; i<numFrames; ++i) {
				encodeFrame(i);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
