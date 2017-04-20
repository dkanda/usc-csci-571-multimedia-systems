package part2;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class part2 {
	byte[] bytes;
	int[] types;
	int width, height, numFrames, frameSize;
	String fileName;
	OutputStream os;
	ByteBuffer byteBuffer;
	FloatBuffer floatBuffer;
	int frameOffset;
	float[] floatsBlock;
	Mat srcDCTMat, dstDCTMat;
	
	public part2(byte[] b, int w, int h, String f) {
		bytes = b;
		width  = w;
		height = h;
		fileName = f;
		
		File file = new File(fileName);
		try {
			os = new FileOutputStream(file);
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}

		// Number of frames in the video
		if (0 != bytes.length % (width*height*3)) {
			throw new RuntimeException("Inavlid byte width, height and/or input array");
		}
		frameSize = width * height; 
		numFrames = bytes.length/(frameSize*3);

		// Number of bytes requried per frame in the destination file.
		int numBlocksCol = width/8  + ((0<width %8) ? 1 : 0);
		int numBlocksRow = height/8 + ((0<height%8) ? 1 : 0);
		int numBlocksPerFrame = numBlocksCol * numBlocksRow;
		int numBytesPerBlock = 4 + 4*64 * 3; // 4 (type) + 4 * 64 * 3 (RGB) = 772
		int numBytesPerFrame = numBlocksPerFrame * numBytesPerBlock;
		
		// IO buffer
		byteBuffer = ByteBuffer.allocate(numBytesPerFrame);
		floatBuffer = byteBuffer.asFloatBuffer();

		// Float arary used for 8x8 DCT
		floatsBlock = new float[64];
		
		// Mat objects used for 8x8 DCT
		srcDCTMat = new Mat(8, 8, CvType.CV_32FC1);
		dstDCTMat = new Mat(8, 8, CvType.CV_32FC1);
		
		// Internal state representing the offset to the current frame 
		frameOffset = 0;
	}
	
	// Write data for a frame to output stream.
	public void writeFrame() throws IOException {
		os.write(byteBuffer.array());
		floatBuffer.clear();
		byteBuffer.clear();
	}
	
	public void encodeType(int type) {
		// Caution: implicit cast to float
		// Need to use Math.round() at the decoder.
		floatBuffer.put(type);
	}
	
	// Apply DCT transform on an 8x8 block
	public void encodeBlock(int row, int col, int colorOffset) {
		// If we are at a frame boundary, clear DCT src array
		// because we want the pixel values to be 0 for the
		// elements that are not being touched
		if (row+8 > height || col+8 > width) {
			for (int i=0; i<64; ++i) {
				floatsBlock[i] = 0;
			}
		}
		// Populate DCT src array with pixel values
		int row_end = Math.min(row+8, height);
		int col_end = Math.min(col+8, width );
		int blockOffset = colorOffset + frameOffset;
		for (int r=row; r<row_end; ++r) {
			for (int c=col; c<col_end; ++c) {
				floatsBlock[(r-row)*8 + (c-col)] = bytes[r*width + c + blockOffset] & 0xff;
			}
		}
		// Forward DCT transform
		srcDCTMat.put(0,0,floatsBlock); // Populate source Mat
		Core.dct(srcDCTMat, dstDCTMat); // Run DCT
		dstDCTMat.get(0,0,floatsBlock); // Retrieve data from destination Mat
		
		// Write result to the frame buffer
		floatBuffer.put(floatsBlock);
	}
	
	// Iterate through each 8x8 block in a frame, 
	// apply transform and write coefficients to file
	public void encodeFrame(int frame) throws IOException {
		for (int r=0; r<height; r+=8) {
			for (int c=0; c<width; c+=8) {
				//Assuming that types the motion compensation
				//based foreground/background separation gives
				//an int array indicating the type of the block
				//writeType(types[(r/16 * height/16 + c/16]);
				//TODO currently hardcoding type to 0 
				encodeType(0);
				encodeBlock(r, c, 0          ); // R
				encodeBlock(r, c, 1*frameSize); // G
				encodeBlock(r, c, 2*frameSize); // B
			}
		}
		frameOffset += frameSize*3;
	}
	
	public void encode() {
		try {
			for (int i=0; i<numFrames; ++i) {
				encodeFrame(i);
				writeFrame();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
