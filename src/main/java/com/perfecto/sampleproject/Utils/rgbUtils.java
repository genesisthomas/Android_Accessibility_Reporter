package com.perfecto.sampleproject.Utils;

public class rgbUtils {

	public static int[] getRGBarray(int rgb)
	{
		int blue = rgb & 0xff;
		int green = (rgb & 0xff00) >> 8;
		int red = (rgb & 0xff0000) >> 16;
		int[] rgba ={red,green,blue};
		return rgba;
	}

	public static int[] getRGBarrayFromString(String rgb)
	{
		String[] rgbs= rgb.split(":");
		int[] rgba = {Integer.valueOf(rgbs[0]).intValue(),
				Integer.valueOf(rgbs[1]).intValue(),
				Integer.valueOf(rgbs[2]).intValue()
		};
		return rgba;
	}
	public static int getIntFromColor(int Red, int Green, int Blue){
		Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
		Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
		Blue = Blue & 0x000000FF; //Mask out anything not blue.
		return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
	}


	public static double  calcRatio (String rgb1,String rgb2)
	{
		double l1 = getLuminance(rgb1);
		double l2 = getLuminance(rgb2);
		double r = 0.0;
		if (l1 >l2)
		{
			r =(l1 + 0.05) / (l2 + 0.05);
		}
		else
		{
			r = (l2 + 0.05) / (l1 + 0.05);
		}
		double roundRatio = Math.round (r * 100.0) / 100.0;
		return roundRatio;
	}

	private static double getLuminance (String rgb)
	{
		String[] rgba = rgb.trim().split(":");
		int r = Integer.parseInt(rgba[0]);
		int g = Integer.parseInt(rgba[1]);
		int b = Integer.parseInt(rgba[2]);
		double L = 0.2126 * getRGBg(r) + 0.7152 *  getRGBg(g)  + 0.0722 *  getRGBg(b) ;
		return L;
	}

	private static double getRGBg(int val)
	{
		//  if R <= 10 then Rg = R/3294, else Rg = (R/269 + 0.0513)^2.4
		if (val <=10)
		{
			return val/3294.0;
		}
		else
		{
			return Math.pow(val/269.0+0.0513,2.4);
		}
	}





}
