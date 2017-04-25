package decoder;

import part3.part3;
import part4.part4;

	
	// Run this part with parameters
	// <input file> <width> <height> <fps>
	// oneperson_960_540.rgb 960 540 12
	

	public class decoder {
				
		public static String usage() {
			return "usage) myencoder.exe input_file n1 n2 gazeControl width height output_file\n"; 
		}
				
		public static void main(String[] args){
			// Parse command line arguments. 
			// Note: It currently requires 7 arguments instead of 4
			//       in order to support various images sizes and
			//       in order to an intermediate rgb file for debugging
			if (args.length != 6) {
				throw new RuntimeException(usage());
			}
			String fi = args[0];                // Input file
			int n1 = Integer.parseInt(args[1]); // step for foreground blocks
			int n2 = Integer.parseInt(args[2]); // step for background blocks
			int g = Integer.parseInt(args[3]);  // Gaze Control
			int w = Integer.parseInt(args[4]);  // Frame width
			int h = Integer.parseInt(args[5]);  // Frame size
			// (Optinoal) real time decoding mode
			int r = (args.length == 7) ? Integer.parseInt(args[6]) : 0; 
			part3 part3Obj = new part3(fi, n1, n2, g, w, h, r);
			part4 part4Obj = new part4(part3Obj);
			part4Obj.showMovie();
//			ren.showMovie(args);
		}

	}
