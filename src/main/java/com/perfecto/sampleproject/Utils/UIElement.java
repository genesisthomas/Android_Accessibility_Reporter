package com.perfecto.sampleproject.Utils;

import java.util.List;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.perfecto.reportium.client.ReportiumClient;

public class UIElement {

	String filename;
	String text;
	int x;
	int y ;
	int width;
	int height;
	String backgroundColor;
	String textColor;
	String FixJpg;

	public UIElement(String name)
	{
		text = name;
	}

	public UIElement(String text,int x,int y ,int width ,int height)
	{
		this.text=text;
		this.height=height;
		this.width=width;
		this.x =x;
		this.y=y;
	}

	public String getFilename() {
		return filename;
	}

	public String getName() {
		return text;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public String getFixJpg() {
		return FixJpg;
	}

	public void setFixJpg(String fixJpg) {
		FixJpg = fixJpg;
	}

	public void getScreenshot(RemoteWebDriver driver, int index) {
		((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
		
	}
}
