package com.perfecto.sampleproject.Utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import com.perfecto.sampleproject.Utils.rgbUtils;
import javax.imageio.ImageIO;

import com.perfecto.reportium.client.ReportiumClient;

import io.appium.java_client.AppiumDriver;

public class GraphicUtils {
	public static pageUI setUiElement(AppiumDriver<?> driver, ReportiumClient reportiumClient, pageUI p, String name, int x, int y , int width , int height)
	{
		name = name.replaceAll("[^A-Za-z0-9\\s]+", "");
		int name_len = name.length();
		if (name_len > 19) {
			name = name.substring(0, Math.min(name_len, 20));
			name = name + "...";
		}
		System.out.print("Name: " + name + " :: x:" + x + " y:" + y + " width:" + width + " height:" + height + "\n");
		UIElement e = new UIElement(name, x, y, width, height);
		e.setFilename(p.getNameWitoutSuff()+name+".jpg");
		slicePart(p.getFullFileName(),e);
		Utils.report_comment(driver, "Screenshot location:");
		reportiumClient.reportiumAssert(e.getFilename(), true);
		drowRecPNG(p.getFullFileName(),e);
		Utils.report_comment(driver, "Full Screenshot location:");
		reportiumClient.reportiumAssert(p.getFullFileName(), true);
		p.addUIElement(e);
		return p;
	}

	private static void slicePart(String origFileName, UIElement u)
	{
		try {
			BufferedImage image = ImageIO.read(new File(origFileName));
			BufferedImage slice = image.getSubimage(u.getX(),u.getY(),u.getWidth(),u.getHeight());
			ImageIO.write(slice, "jpg", new File(u.getFilename()));
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}

	// this is to handle file PNG from standard Appium
	private static void drowRecPNG(String file,UIElement u)
	{
		File imageFile = new File(file);

		BufferedImage origIimg = null;
		BufferedImage img = null; //read PNG from standart appuim

		try {
			origIimg = ImageIO.read(imageFile);

			// create a blank, RGB, same width and height, and a white background
			img = new BufferedImage(origIimg.getWidth(),origIimg.getHeight(), BufferedImage.TYPE_INT_RGB);
			img.createGraphics().drawImage(origIimg, 0, 0, Color.WHITE, null);

			Graphics2D graph = img.createGraphics();

			graph.setColor(Color.RED);

			BasicStroke stroke = new BasicStroke(5); //5 pixels wide
			graph.setStroke(stroke);

			graph.drawRect(u.getX(),u.getY(),u.getWidth(),u.getHeight());
			//   checkRGBv(file,x, y, width, height);
			if (u.getName() !=null)
			{
				graph.setFont(new Font( "SansSerif", Font.PLAIN, 24 ));
				graph.drawString(u.getName(),u.getX(),u.getY());
			}
			ImageIO.write(img, "jpg", new File(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getImageHigh(File file)
	{
		BufferedImage origIimg = null;
		BufferedImage img = null; //read PNG from standart appuim

		try {
			origIimg = ImageIO.read(file);
			return  origIimg.getHeight();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	private  static int[] getPixelData(BufferedImage img, int x, int y) {
		int argb = img.getRGB(x, y);

		int rgb[] = new int[] {
				(argb >> 16) & 0xff, //red
				(argb >>  8) & 0xff, //green
				(argb      ) & 0xff  //blue
		};


		return rgb;
	}

	public static  void findFontSize(UIElement u)
	{
		int startFontLine=0;
		int stopFontLine=0;
		boolean foundFontRGBinLine=false;
		boolean lookingForStart=true;
		int[] backRBG = rgbUtils.getRGBarrayFromString(u.getBackgroundColor());
		int background = rgbUtils.getIntFromColor(backRBG[0],backRBG[1],backRBG[2]);
		int[] fontRBG = rgbUtils.getRGBarrayFromString(u.getTextColor());
		int fontRGBColor = rgbUtils.getIntFromColor(fontRBG[0],fontRBG[1],fontRBG[2]);
		try{

			BufferedImage baseFile = ImageIO.read(new File(u.getFixJpg()));
			for(int y = 0; y < baseFile.getHeight(); y++){
				for(int x = 0; x < baseFile.getWidth(); x++){
					int RBG= baseFile.getRGB(x,y);
					if (RBG == fontRGBColor)
					{
						// this links contains text
						foundFontRGBinLine = true;
						x=baseFile.getWidth();
					}
				}
				// check the line data , if contains text based on mode (start , end text)
				if (lookingForStart)
				{
					//look for the first line with text
					if (foundFontRGBinLine) {
						lookingForStart = false;
						startFontLine=y;
					}
				}
				else
				{
					//look for the end of  text line without text
					if (!foundFontRGBinLine) {
						stopFontLine=y;
						//stop the loop dont need more loop
						y=baseFile.getHeight();
					}
				}
				//set the value for the before next line
				foundFontRGBinLine = false;
			}
			System.out.println(u.getName()+" fontSizeRation:"+ (stopFontLine-startFontLine) + " from "+baseFile.getHeight() );
		}catch(IOException e){
			System.out.println(e);
		}
	}

	public static int  getClosestRGB(int[] rgb,String background,String text)
	{


		int diffA = 0;
		int diffB = 0;

		int[] rgbA = rgbUtils.getRGBarrayFromString(background);
		//check if the rbg is near the the rgb high
		for (int i=0;i< rgb.length;i++)
		{
			diffA = diffA + Math.abs(rgb[i]-rgbA[i]);
		}
		int[] rgbB = rgbUtils.getRGBarrayFromString(text);

		for (int i=0;i< rgb.length;i++)
		{
			diffB = diffB + Math.abs(rgb[i]-rgbB[i]);
		}

		if (diffA>diffB)
			return rgbUtils.getIntFromColor(rgbB[0],rgbB[1],rgbB[2]);
		else
			return rgbUtils.getIntFromColor(rgbA[0],rgbA[1],rgbA[2]);
	}



	public static void drowRec(String file,int x,int y ,int width ,int height)
	{
		drowRec( file, x, y , width , height, null);
	}

	private static boolean rgbComp(int[] a,int[] b)
	{
		if (b==null)
			return false;
		if (b.length==0)
			return false;

		int val = Math.abs(a[0]-b[0]) +  Math.abs(a[1]-b[1]) +  Math.abs(a[2]-b[2]) ;
		if (val > 300  )
			return false ;
		else
			return  true;
	}

	public static void checkImageRGBOld(String image)
	{
		BufferedImage img;

		try {
			img = ImageIO.read(new File(image));

			int[][] pixelData = new int[img.getHeight() * img.getWidth()][3];
			int[] rgb;

			int counter = 0;
			for(int i = 0; i < img.getHeight(); i++){
				for(int j = 0; j < img.getWidth(); j++){

					rgb = GraphicUtils.getPixelData(img, j, i);
					for(int k = 0; k< rgb.length; k++){
						pixelData[counter][k] = rgb[k];
					}
					counter++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void drowRec(String file,int x,int y ,int width ,int height,String numerToAdd)
	{
		File imageFile = new File(file);
		BufferedImage img = null;
		try {
			img = ImageIO.read(imageFile);
			Graphics2D graph = img.createGraphics();

			graph.setColor(Color.red);

			BasicStroke stroke = new BasicStroke(5); //5 pixels wide
			graph.setStroke(stroke);

			graph.drawRect(x, y, width, height);
			if (numerToAdd !=null)
			{
				graph.setFont(new Font( "SansSerif", Font.BOLD, 30 ));
				graph.drawString(numerToAdd,x,y);
			}

			ImageIO.write(img, "jpg", new File(file+"2.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
