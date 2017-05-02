package part1;

import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;
import part1.Macroblock;

public class part1Tester {
	
	
	//Arg1: file name
	//Arg2: width
	//Arg3: height
	public static void main(String [] args) {
		
		String fileName = args[0];
		File inFile = new File(fileName);
		int width = Integer.parseInt(args[1]);
		int height = Integer.parseInt(args[2]);
		
		part1 part1Test = new part1(inFile,width,height);
		HashMap<Integer,ArrayList<Macroblock>> resultMap = null;
		
		//Do the test
		resultMap = part1Test.doPart1();
		
		System.out.println("Size of Frame 1: " + resultMap.get(1).size());
	}
	
	
	
}
