package part4;

	import java.awt.*;
	import java.awt.image.*;
	import java.awt.event.*;
	import java.io.*;
	import javax.swing.*;

	
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


		public void showMovie(String[] args){
			
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
			int ind = 0;			
			int offset = curFrame*width*height*3;
			int pix;
			for(int y = 0; y < height; y++){
				
				for(int x = 0; x < width; x++){
					
					// If this block matches our criteria, read it and set it as the pixel to add. 
					if (y > blockY*64 
						&& y < blockY*64 + 64
						&& x > blockX*64 
						&& x < blockX*64 + 64)
					{
						byte r = bytes[ind+offset];
						byte g = bytes[ind+width*height+offset];
						byte b = bytes[ind+2*width*height+offset];
						
						pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | ( b &0xff);
					}
					// Else write a black pixel.
					else{
						pix = 0;
					}
					img.setRGB(x,y,pix);
					ind++;
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
