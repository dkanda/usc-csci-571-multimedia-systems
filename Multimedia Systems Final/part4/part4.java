package part4;

	import java.awt.*;
	import java.awt.image.*;
	import java.awt.event.*;
	import java.io.*;
	import javax.swing.*;

import part3.part3;

	
	// Run this part with parameters
	// <input file> <width> <height> <fps>
	// oneperson_960_540.rgb 960 540 12
	
	// This is code is a base from HW1 part 2 where we played a movie.
	// TODO: Add a mouse listener that can read from the rgb file.
	public class part4 {

		static GridBagConstraints c = new GridBagConstraints();
		static JLabel lbIm1 = new JLabel();
		int fps, width, height, numberOfFrames;
		static int curFrame = 0;
		String fileName;
		byte[] bytes = {};
		JLabel lbText1;
		BufferedImage img;	
		JFrame frame = new JFrame();
		Timer frameTimer;
		long len;	
		PointerInfo a;
		Point b;
		int heightTopOffset = 20;
		part3 part3Obj;
		boolean gazeControl;
		
		public part4(part3 obj) {
			part3Obj = obj;
			width = part3Obj.getWidth();
			height = part3Obj.getHeight();
			numberOfFrames = part3Obj.getNumFrames();
			gazeControl = part3Obj.getGazeControl();
			fps = 10;
		}
				
		public void showMovie() {
			// Display Image
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);				
			// Instantiate frame sync class to use with timer.
			DispFrameOnInt frameSync = new DispFrameOnInt();
			// Calculate the time interval for each frame to be displayed.
			//  as 1 / fps. 
			frameTimer = new Timer((int) (1000.00 / fps), frameSync);
			frameTimer.start();
		}
		
		private part4() {
			
		}
		
		public void showMovie(String[] args) {			
			if( args.length == 4){	
				fileName = args[0];
				width = Integer.parseInt(args[1]);
				height = Integer.parseInt(args[2]);
				fps = Integer.parseInt(args[3]);
			}
			else{
				//write error to console.
			}
			
			// Display Image
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);		
			try{
				File file = new File(fileName);
				InputStream is = new FileInputStream(file);
				
				len = file.length();
				bytes  = new byte[(int)len];
				
				int offset = 0;
				int numRead = 0;

				while(offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length-offset))>=0){
					offset += numRead;
				}
				numberOfFrames = (int) (len) / (3 * width * height);

			} catch(FileNotFoundException e){
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			}
			
			// Instantiate frame sync class to use with timer.
			DispFrameOnInt frameSync = new DispFrameOnInt();
			
			// Calculate the time interval for each frame to be displayed.
			//  as 1 / fps. 
			frameTimer = new Timer((int) (1000.00 / fps), frameSync);
			frameTimer.start();
		}

		 // Gets the next frame and writes it to the image buffer.
		 private void getNextFrame(int blockX, int blockY){		
			int offset = curFrame*width*height;
			// Set quantized pixel values for the entire frame
			img.setRGB(0, 0, width, height, part3Obj.intsQuant, offset, width);
			if (gazeControl) {
				// Set raw pixel values for the gaze window
				int y_b = blockY*64;
				int y_e = Math.min(height, y_b+64);
				int x_b = blockX*64;
				int x_e = Math.min(width, x_b+64);
				for (int y=y_b; y<y_e; ++y) {
					for (int x=x_b; x<x_e; ++x) {
						img.setRGB(x,y,part3Obj.intsRaw[y*width + x + offset]);
					}
				}			
			}
		}

		class DispFrameOnInt implements ActionListener{
			// Draw the gui and the first frame.
			public DispFrameOnInt(){
				// Use labels to display the images
				GridBagLayout gLayout = new GridBagLayout();
				frame.getContentPane().setLayout(gLayout);
				
				lbIm1.setIcon(new ImageIcon(img));

				c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.anchor = GridBagConstraints.CENTER;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 0;
				
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 1;
				frame.getContentPane().add(lbIm1,c);

				frame.pack();
				frame.setVisible(true);
			}
			
			// This function loops through at a set fps which is passed into the program.
			// Check if the cursor is on the screen to determine the block beneath it to 
			// pull from the rgb file.
			public void actionPerformed(ActionEvent e){
				Dimension panelSize = frame.getSize();
				a = MouseInfo.getPointerInfo();
				b = a.getLocation();
				int x = (int) b.getX();
				int y = (int) b.getY();
				Point panelLocation = frame.getLocationOnScreen();
				
				// Determine if cursor is in range.
				if( x - panelLocation.x > 0 
				   && (panelLocation.x + panelSize.width) - x > 0
				   && y - heightTopOffset - panelLocation.y > 0 
				   && (panelLocation.y + panelSize.height) - y > 0){
					int blockX = (x - panelLocation.x) / 64; 
					int blockY = (y - panelLocation.y) / 64;
					getNextFrame(blockX, blockY);
					
				}

				// Set the image as an icon 
				lbIm1.setIcon(new ImageIcon(img));
				frame.getContentPane().add(lbIm1,c);
				frame.pack();
				
				// Increment curFrame index for next read or set to 0 to loop around.
				if (curFrame + 1 == numberOfFrames){
					curFrame = 0;
				}
				else{
					curFrame++;
				}
			}
		}

		public static void main(String[] args){
			part4 ren = new part4();
			ren.showMovie(args);
		}

	}
