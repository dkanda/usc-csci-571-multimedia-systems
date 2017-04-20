package part2;

import java.awt.GridBagConstraints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.opencv.core.Core;

public class part2Tester {
	
	static GridBagConstraints c = new GridBagConstraints();
	static JLabel lbIm1 = new JLabel();
	int fps, width, height, numberOfFrames;
	static int curFrame = 0;
	String fileNameInput;
	String fileNameOutput;
	byte[] bytes = {};
	int[] ints = {};
	JLabel lbText1;
	BufferedImage img;	
	JFrame frame = new JFrame();
	Timer frameTimer;
	long len;
	part2 encoder;
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	public static int[] convertToIntArray(byte[] input)	{
	    int[] ret = new int[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
	    }
	    return ret;
	}

	public void readMovie(String[] args) {
//		if( args.length == 5){	
			fileNameInput = args[0];
			width = Integer.parseInt(args[1]);
			height = Integer.parseInt(args[2]);
//			fps = Integer.parseInt(args[3]);
			fileNameOutput = args[3];
//		}
//		else{
//			//write error to console.
//		}
		
		try{
			File file = new File(fileNameInput);
			InputStream is = new FileInputStream(file);
			
			len = file.length();
			bytes  = new byte[(int)len];
			
			int offset = 0;
			int numRead = 0;
	
			while(offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length-offset))>=0){
				offset += numRead;
			}
			numberOfFrames = (int) (len) / (3 * width * height);
			//ints = convertToIntArray(bytes);
			
			is.close();
	
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void runPart2() {
		encoder = new part2(bytes, width, height, fileNameOutput);
		encoder.encode();
	}
	
	public static void main(String[] args) {
		if (args.length != 4)
			throw new RuntimeException("need 4 arguments");
		part2Tester T = new part2Tester();
		T.readMovie(args);
		T.runPart2();
	}

}
