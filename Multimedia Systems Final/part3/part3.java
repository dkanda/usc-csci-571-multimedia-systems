package part3;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class part3 {
	byte[] bytes;
	int stepFg, stepBg, gazeControl, width, height, offset, numFrames, frameSize;
	String fileNameInput;
	String fileNameOutput;
	FileInputStream fis;
	DataInputStream dis;
	OutputStream os;
	ByteBuffer byteBuffer;
	FloatBuffer floatBuffer;
	byte[] bytesBlock, bytesRaw, bytesQuant;
	float[] srcFloatsBlock, dstFloatsBlock;
	int frameOffset;
	Mat srcIDCTMat, dstIDCTMat;
	
	// Required to use OpenCV
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	public part3(String fi, int n1, int n2, int g, int w, int h, String fo) {
		stepFg = n1;
		stepBg = n2;
		gazeControl = g;
		width  = w;
		height = h;
		fileNameInput = fi;
		fileNameOutput = fo;
	
		File fileInput = new File(fileNameInput);
		File fileOutput = new File(fileNameOutput);
		try {
			fis = new FileInputStream(fileInput);
			dis = new DataInputStream(fis);
			os = new FileOutputStream(fileOutput);
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}
		
		// Number of bytes requried per frame in the destination file.
		int numBlocksCol = width/8  + ((0<width %8) ? 1 : 0);
		int numBlocksRow = height/8 + ((0<height%8) ? 1 : 0);
		int numBlocksPerFrame = numBlocksCol * numBlocksRow;
		int numBytesPerBlock = 4 + 4*64 * 3; // 4 (type) + 4 * 64 * 3 (RGB) = 772
		int numBytesPerFrame = numBlocksPerFrame * numBytesPerBlock;
		
		// Number of frames in the video
		if (0 != (fileInput.length() % numBytesPerFrame)) {
			throw new RuntimeException("Inavlid byte width, height and/or input data");
		}
		frameSize = width * height;
		numFrames = (int) (fileInput.length() / numBytesPerFrame);
		
		// IO buffer
		bytesBlock = new byte[64*4];
		byteBuffer = ByteBuffer.allocate(64*4);
		floatBuffer = byteBuffer.asFloatBuffer();
		
		// Float array used for 8x8 IDCT
		srcFloatsBlock = new float[64];
		dstFloatsBlock = new float[64];
		
		// Mat objects used for 8x8 IDCT
		srcIDCTMat = new Mat(8, 8, CvType.CV_32FC1);
		dstIDCTMat = new Mat(8, 8, CvType.CV_32FC1);
		
		// Internal state representing the offset to the current frame 		
		frameOffset = 0;
		
		// RGB buffer
		bytesRaw = new byte[3*width*height*numFrames];
		bytesQuant = new byte[3*width*height*numFrames];
	}
	
	public int readType() throws IOException {
		return Math.round(dis.readFloat());
	}
	
	/* TODO: May need readFrame() to speed it up */
	
	public void readBlock() throws IOException {
		dis.readFully(byteBuffer.array());
		floatBuffer.get(srcFloatsBlock);
		byteBuffer.clear();
		floatBuffer.clear();
	}
		
	public void decodeBlock(int row, int col, int colorOffset, int type) {
		// In order to support gaze control (for off-line decoding), 
		// generate separate rgb byte array for both raw and quantized output.
		// We probably need to implement frame by frame decoding to support real time
		// decoding for extra credit.
		
		// Offset to the beginning of current block
		int blockOffset = colorOffset + frameOffset;
		
		// Detect frame boundary
		int rowEnd = Math.min(row+8, height);
		int colEnd = Math.min(col+8, width );
		
		// Raw output
		// Inverse DCT transform
		srcIDCTMat.put(0, 0, srcFloatsBlock);
		Core.dct(srcIDCTMat, dstIDCTMat, Core.DCT_INVERSE);
		dstIDCTMat.get(0,0,dstFloatsBlock);
		// Populate RGB byte array
		for (int r=row; r<rowEnd; ++r) {
			for (int c=col; c<colEnd; ++c) {
				bytesRaw[r*width + c + blockOffset] = (byte) Math.max(0, dstFloatsBlock[(r-row)*8 + (c-col)]);
			}
		}
		
		// Quantized output
		int step = (0 == type) ? stepFg : stepBg;
		// Quantization
		for (int i=0; i<64; ++i) {
			int value = Math.round(srcFloatsBlock[i]);
			value /= step;
			value *= step;
			srcFloatsBlock[i] = value;
		}
		// Inverse DCT transform
		srcIDCTMat.put(0, 0, srcFloatsBlock);
		Core.dct(srcIDCTMat, dstIDCTMat, Core.DCT_INVERSE);
		dstIDCTMat.get(0,0,dstFloatsBlock);
		// Populate RGB byte array
		for (int r=row; r<rowEnd; ++r) {
			for (int c=col; c<colEnd; ++c) {
				float val = dstFloatsBlock[(r-row)*8 + (c-col)];
				val = (val < 0   ) ? 0    : 
					  (val > 0xff) ? 0xff : 
				       val;
				bytesQuant[r*width + c + blockOffset] = (byte) val;
			}
		}
	}
	
	public void decodeFrame(int frame) throws IOException {
		for (int r=0; r<height; r+=8) {
			for (int c=0; c<width; c+=8) {
				int type = readType();
				readBlock();
				decodeBlock(r, c, 0          , type); // R
				readBlock();
				decodeBlock(r, c, 1*frameSize, type); // G
				readBlock();
				decodeBlock(r, c, 2*frameSize, type); // B
			}
		}
	}
	
	public void writeRaw() throws IOException {
		os.write(bytesRaw);
	}
	
	public void writeQuant() throws IOException {
		os.write(bytesQuant);
	}
	
	// XXX for debugging only
	public void dumpFrame(int frame, int color, byte[] buffer) throws FileNotFoundException {
		PrintWriter wrtierFrame = new PrintWriter(String.format("%01d_%03d.txt", color, frame));
		for (int r=0; r<height; ++r) {
			for (int c=0; c<width; ++c) {
				wrtierFrame.print(String.format("%02X ", buffer[r*width + c + frame*frameSize*3 + color*frameSize]));				
			}
			wrtierFrame.println();
		}
		wrtierFrame.close();
	}

	// XXX for debugging only
	public void dumpFrameCoeff(int frame, int color, float[] buffer) throws FileNotFoundException {
		PrintWriter wrtierFrame = new PrintWriter(String.format("%01d_%03d.txt", color, frame));
		for (int r=0; r<height; ++r) {
			for (int c=0; c<width; ++c) {
				wrtierFrame.print(String.format("%.1f ", buffer[r*width + c + frame*frameSize*3 + color*frameSize]));				
			}
			wrtierFrame.println();
		}
		wrtierFrame.close();
	}
	
	public void decode() {
		try {
			for (int i=0; i<numFrames; ++i) {
				decodeFrame(i);
				frameOffset += frameSize*3;
			}
//			writeRaw();
			writeQuant();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String usage() {
		return "usage) myencoder.exe input_file n1 n2 gazeControl width height output_file\n"; 
	}
	
	//mydecoder.exe input_video.cmp n1 n2 gazeControl
	public static void main(String[] args) {
		// Parse command line arguments. 
		// Note: It currently requires 7 arguments instead of 4
		//       in order to support various images sizes and
		//       in order to an intermediate rgb file for debugging
		if (args.length != 7) {
			throw new RuntimeException(usage());
		}
		String fi = args[0];                // Input file
		int n1 = Integer.parseInt(args[1]); // step for foreground blocks
		int n2 = Integer.parseInt(args[2]); // step for background blocks
		int g = Integer.parseInt(args[3]);  // Gaze Control
		int w = Integer.parseInt(args[4]);  // Frame width
		int h = Integer.parseInt(args[5]);  // Frame size
		String fo = args[6];                // Output rgb file
		// Decode video
		part3 D = new part3(fi, n1, n2, g, w, h, fo);
		D.decode();
	}	
}

