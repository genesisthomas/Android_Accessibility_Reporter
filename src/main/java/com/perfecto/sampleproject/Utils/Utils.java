package com.perfecto.sampleproject.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.internal.TestResult;

import com.google.gson.Gson;
import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.model.Job;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResultFactory;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Utils {
	/**
	 * fetches cloud name
	 * @param cloudName
	 * @return
	 * @throws Exception
	 */
	public static String fetchCloudName(String cloudName) throws Exception {
		//Verifies if cloudName is hardcoded, else loads from Maven properties 
		String finalCloudName = cloudName.equalsIgnoreCase("<<cloud name>>") ? System.getProperty("cloudName") : cloudName;
		//throw exceptions if cloudName isnt passed:
		if(finalCloudName == null || finalCloudName.equalsIgnoreCase("<<cloud name>>"))
			throw new RuntimeException("Please replace <<cloud name>> with your perfecto cloud name (e.g. demo) or pass it as maven properties: -DcloudName=<<cloud name>>");
		else
			System.setProperty("cloudName", finalCloudName);
		return finalCloudName;
	}

	/**
	 * Fetches security token
	 * @param securityToken
	 * @return
	 * @throws Exception
	 */
	public static String fetchSecurityToken(String securityToken) {
		//Verifies if securityToken is hardcoded, else loads from Maven properties
		String finalSecurityToken = securityToken.equalsIgnoreCase("<<security token>>") ? System.getProperty("securityToken") : securityToken;
		//throw exceptions if securityToken isnt passed:
		if(finalSecurityToken == null || finalSecurityToken.equalsIgnoreCase("<<security token>>"))
			throw new RuntimeException("Please replace <<security token>> with your perfecto security token or pass it as maven properties: -DsecurityToken=<<SECURITY TOKEN>>");
		else
			System.setProperty("securityToken", finalSecurityToken);
		return finalSecurityToken;
	}

	/**
	 * Creates reportium client
	 * @param driver
	 * @param reportiumClient
	 * @return
	 */
	public static ReportiumClient setReportiumClient(RemoteWebDriver driver, ReportiumClient reportiumClient) {
		PerfectoExecutionContext perfectoExecutionContext;
		// Reporting client. For more details, see https://developers.perfectomobile.com/display/PD/Java
		if(System.getProperty("reportium-job-name") != null) {
			perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
					.withProject(new Project("My Project", "1.0"))
					.withJob(new Job(System.getProperty("reportium-job-name") , Integer.parseInt(System.getProperty("reportium-job-number"))))
					.withContextTags("tag1")
					.withWebDriver(driver)
					.build();
		} else {
			perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
					.withProject(new Project("My Project", "1.0"))
					.withContextTags("tag1")
					.withWebDriver(driver)
					.build();
		}
		reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);
		return reportiumClient;
	}

	/**
	 * Asserts text
	 * @param WebElement
	 * @param reportiumClient
	 * @param text
	 */
	public static void assertText(WebElement data, ReportiumClient reportiumClient, String text) {
		assert data.getText().equals(text) : "Actual text : " + data.getText() + ". It did not match with expected text: " +  text;
		if(reportiumClient != null)
			reportiumClient.reportiumAssert("Verify Field: " + data.getText() , data.getText().equals(text));
	}

	/**
	 * Assert title
	 * @param title
	 * @param reportiumClient
	 */
	public static void assertTitle(String title, ReportiumClient reportiumClient) {
		if (!title.equals("Web & Mobile App Testing | Continuous Testing | Perfecto")) {
			reportiumClient.reportiumAssert("Title is mismatched", false);
			throw new RuntimeException("Title is mismatched");
		}else {
			reportiumClient.reportiumAssert("Title is matching", true);
		}
	}


	public static int getScreenShot(RemoteWebDriver d, String folder, String name) throws IOException {
		Capabilities cs = d.getCapabilities();
		d.getPageSource();
		File scrFile = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
		try {
			// Copy paste file at destination folder location
			FileUtils.copyFile(scrFile, new File(folder + "/" + name+".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}


		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 1;
	}

	public static String get_deviceid(AppiumDriver driver) {
		Map<String, Object> params = new HashMap<>();
		params.put("property", "deviceId");
		String id = (String) driver.executeScript("mobile:device:info", params);
		return id;
	}

	public static void delete_audio_from_repo(String host, String securityToken, String key) {
		try {
			URL url = new URL("https://" + host + "/services/repositories/media/" + key + "?operation=delete&securityToken=" + securityToken);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void switchToContext(RemoteWebDriver driver, String context) {
		RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
		Map<String,String> params = new HashMap<String,String>();
		params.put("name", context);
		executeMethod.execute(DriverCommand.SWITCH_TO_CONTEXT, params);
	}

	public static String getCurrentContextHandle(RemoteWebDriver driver) {
		RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
		String context =  (String) executeMethod.execute(DriverCommand.GET_CURRENT_CONTEXT_HANDLE, null);
		return context;
	}

	public static List<String> getContextHandles(RemoteWebDriver driver) {
		RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
		List<String> contexts =  (List<String>) executeMethod.execute(DriverCommand.GET_CONTEXT_HANDLES, null);
		return contexts;
	}

	public static void maxVolume(RemoteWebDriver driver) {
		int i = 0;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("keySequence", "VOL_UP");
		for (i = 0; i < 12; i++)
			driver.executeScript("mobile:presskey", params);
	}

	public static void unzip_report(AppiumDriver driver, ReportiumClient reportiumClient, File[] files, File destination, String screen) throws IOException {
		for (File file : files) {
			if (file.isDirectory()) {
				System.out.println("Directory: " + file.getName());
				unzip_report(driver, reportiumClient, file.listFiles(), destination, screen);
			} else {
				System.out.println("File: " + file.getName());
				if(file.getName().endsWith(".zip")){
					try {
						ZipFile zipFile = new ZipFile(file);
						FileUtils.forceMkdir(destination);
						File parentFolder = file.getParentFile();
						zipFile.extractAll(parentFolder.toString());
						File[] listOfFiles = parentFolder.listFiles();
						for (File parentFile : listOfFiles) {
							String name = parentFile.getName();
							if(name.startsWith("Accessibility Report " + screen) && name.endsWith(".json")){
								report_comment(driver, "temporary json file location:");
								reportiumClient.reportiumAssert(parentFile.toString(), true);
								FileUtils.copyFile(parentFile, new File(destination + File.separator + name));
							}
						}
					} catch (ZipException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static File get_json_filepath(File dir) {
		File[] matchingFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith("Accessibility") && name.endsWith(".json");
			}
		});
		if (matchingFiles.length < 1) {
			throw new RuntimeException("No json files found in "+ dir.toString());
		}
		System.out.println(matchingFiles[0].toString());
		return matchingFiles[0];
	}

	public static void take_screenshot(AppiumDriver driver, ReportiumClient reportiumclient, String screen) throws Exception {
		File file = new File(System.getProperty("user.dir") + File.separator + "Accessibility" + File.separator + Utils.get_deviceid(driver) + File.separator + screen + File.separator + "Screenshots");
		if (!file.exists()) {
			file.mkdirs();
		}
		String dir = file.toString();
		Utils.getScreenShot(driver, dir, screen);
		report_comment(driver, "local Screenshot:");
		reportiumclient.reportiumAssert(dir , true);
	}

	public static void accessibility_highlighter(AppiumDriver driver, ReportiumClient reportiumClient, String executionId, String scriptName, String deviceId, String[] screens, String appPackage)
			throws Exception {
		//TODO override custom values if required
		int expectedHeight = 48;
		int expectedWidth = 48;
		int expectedXSize = 24;
		int expectedYSize = 24;  

		String path = Reporting.main(executionId, scriptName, deviceId).toString();
		File[] f = new File(path).listFiles();

		String browserName = "";
		DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
		String cloudName = System.getProperty("cloudName");
		String securityToken = System.getProperty("securityToken");
		capabilities.setCapability("securityToken", securityToken);
		capabilities.setCapability("deviceName", deviceId);
		String app = "com.samsung.android.dialer";
		capabilities.setCapability("platform", "Android");
		capabilities.setCapability("pureAppiumBehaviour", false);
		capabilities.setCapability("appPackage", appPackage);
		capabilities.setCapability("automationName", "Appium");
		capabilities.setCapability("scriptName"+ "_accessibility_report", scriptName);
		try {
			driver = new AndroidDriver(new URL("https://" + Utils.fetchCloudName(cloudName)  + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

			String projectName = "Accessibility";
			String testVersion = "1.0";
			PerfectoExecutionContext perfectoExecutionContext = null;
			if(System.getenv("jobName") != null) {
				String jobName = System.getenv("jobName").trim().toString();
				int jobNumber = Integer.parseInt(System.getenv("jobNumber").trim().toString());
				perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
						.withProject(new Project(projectName, testVersion))
						.withJob(new Job(jobName, jobNumber))
						.withContextTags().withWebDriver(driver).build();
			}else{
				perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
						.withProject(new Project(projectName, testVersion))
						.withContextTags().withWebDriver(driver).build();
			}
			reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);
			reportiumClient.testStart(scriptName + "_accessibility_report", new TestContext("accessibility", scriptName));
			int i = 0;
			for (i = 0; i < screens.length; i++) { 
				String screen = screens[i]; 
				reportiumClient.stepStart("Analysing screen: " + screen);

				//Downloads accessibility results of json to local accessibility folders
				File json = new File(System.getProperty("user.dir") + File.separator + "Accessibility" + File.separator + deviceId + File.separator + screen + File.separator + "json");
				if (!json.exists()) {
					json.mkdirs();
				}else {
					FileUtils.deleteDirectory(json);
					json.mkdirs();
				}
				unzip_report(driver, reportiumClient, f, json, screen);
				json = get_json_filepath(json);
				report_comment(driver, "local json:");
				reportiumClient.reportiumAssert(json.toString(), true);
				
				//prepared path of screenshots
				File file = new File(System.getProperty("user.dir") + File.separator + "Accessibility" + File.separator + deviceId + File.separator + screen + File.separator + "Screenshots");
				if (!file.exists()) {
					throw new RuntimeException("No screenshots found in " + file.toString());
				}
				String localScreenshot = file.toString();
				System.out.print("Processing page: " + screen + " \n");
				pageUI page = new pageUI(screen, localScreenshot);
				report_comment(driver, "local Screenshot:");
				reportiumClient.reportiumAssert(page.getFullFileName(), true);
				Gson gson = new Gson(); 
				BufferedReader br = new BufferedReader(new FileReader(json));
				if(br.readLine().toString().equalsIgnoreCase("No issues found!")) {
					System.out.println("No accessibility issue found");
					reportiumClient.reportiumAssert("Congrats! No accessibility issue found in " + screen, true);
					br.close();
				}else {
					br.close();
					br = new BufferedReader(new FileReader(json));
					Android_Scanner[] scanArray = gson.fromJson(br, Android_Scanner[].class);
					for(Android_Scanner scan : scanArray) {
						String message = scan.getmessage();
						String fulldetails = "id: " + scan.getid() + ", Type: " + scan.gettype() + ", Message: " + message + ", class: " + scan.getclass() + ", leftX: " + scan.getleftX() + ", rightX: " + scan.getrightX() +", topY: " + scan.gettopY() +", bottomY: " + scan.getbottomY();
						try {
							String content = scan.getcontentDescription();
							String expected_text = "";

							if(content != null){
								if(!content.equals("")) {
									expected_text = scan.getid() + " " + content;
								} else {
									if(scan.getclass().contains(".")){
										String[] split = scan.getclass().toString().split("\\.");
										expected_text = scan.getid() + " " + split[split.length - 1];
									}else {
										expected_text = scan.getid() + " " + scan.getclass();
									}
								}
							}else if(scan.getclass().contains(".")){
								String[] split = scan.getclass().toString().split("\\.");
								expected_text = scan.getid() + " " + split[split.length - 1];
							}else {
								expected_text = scan.getid() + " " + scan.getclass();
							}
							reportiumClient.stepStart("analysing: " + expected_text);
							System.out.println("Analyzing message: " + expected_text);
							if(message.contains("View falls below the minimum recommended size for touch targets." )){
								String regex = "Actual.*is\\s.*$";
								Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
								Matcher matcher = pattern.matcher(message);
								int actual = 0;
								int actualxsize = 0;
								int actualysize = 0;
								while (matcher.find()) {
									if(message.contains("Actual height is")) {
										actual = Integer.parseInt(matcher.group(0).split("dp.$")[0].split("is ")[1]);
										if (actual >= expectedHeight) {
											assertPass(reportiumClient, "Actual view height: " + actual + " is above custom expected view height: " + expectedHeight + "px");
										}else {
											prepare_screenshot(driver, reportiumClient, page, scan, "Actual view height: " + actual + " is below custom expected view height: " + expectedHeight + "px. \n Original json:" + fulldetails, expected_text);
										}
									} else if (message.contains("Actual width is")) {
										actual = Integer.parseInt(matcher.group(0).split("dp.$")[0].split("is ")[1]);
										if (actual >= expectedWidth) { 
											assertPass(reportiumClient, "Actual view width: " + actual + " is above custom expected view width: " + expectedWidth + "px");
										}else {
											prepare_screenshot(driver, reportiumClient, page, scan, "Actual view width: " + actual + " is below custom expected view width: " + expectedWidth + "px. Original json:" + fulldetails, expected_text);
										}
									} else if (message.contains("Actual size is")) {
										actualxsize = Integer.parseInt(matcher.group(0).split("dp.$")[0].split("is ")[1].split("x")[0]);
										if (actualxsize >= expectedXSize) {
											assertPass(reportiumClient, "Actual view x size: " + actualxsize + " is above custom expected view x size: " + expectedYSize + "px");
										}else {
											prepare_screenshot(driver, reportiumClient, page, scan, "Actual view x size: " + actualysize + " is below custom expected view x size: " + expectedXSize + "px. Original json:" + fulldetails, expected_text);
										}
										actualysize = Integer.parseInt(matcher.group(0).split("dp.$")[0].split("is ")[1].split("x")[1]);
										if (actualysize >= expectedYSize) {
											assertPass(reportiumClient, "Actual view y size: " + actualysize + " is above custom expected view y size: " + expectedYSize + "px");
										}else {
											prepare_screenshot(driver, reportiumClient, page, scan, "Actual view y size: " + actualysize + " is below custom expected view y size: " + expectedYSize + "px. Original json:" + fulldetails, expected_text);
										}
									}else {
										prepare_screenshot(driver, reportiumClient, page, scan, fulldetails, expected_text);
									}
								}
							}else {
								prepare_screenshot(driver, reportiumClient, page, scan, fulldetails, expected_text);
							}
						}catch(Exception e){
							e.printStackTrace();
							if(e.getMessage().contains("negative or zero width") || e.getMessage().contains("(x + width) is outside of Raster"))
								assertFailure(driver, reportiumClient, "json: " + fulldetails, "Object not visible within the screnshot.");
							else
								assertFailure(driver, reportiumClient, fulldetails, e.getMessage());
						}
					}
				}
			}
			try {
				if(System.getProperty("accessibility_failed").equalsIgnoreCase("true")){
					try {
						reportiumClient.testStop(TestResultFactory.createFailure("Accessibility errors exists!"));
					}catch(Exception e2) {}
					try {
						driver.close();
						driver.quit();
					}catch(Exception e3) {System.out.println(e3.getMessage());}
					org.testng.Assert.fail("Accessibility errors exists!");
				}
			}catch(Exception e) {}
			reportiumClient.testStop(TestResultFactory.createSuccess());
			// Retrieve the URL to the DigitalZoom Report 
			String reportURL = reportiumClient.getReportUrl();
			System.out.println(reportURL);

			driver.close();
			driver.quit();
		}catch(Exception e) {
			try {
				reportiumClient.testStop(TestResultFactory.createFailure("Unable to analyse accessibility report: " + e.getMessage().toString()));
			}catch(Exception e2) {}
			try {
				driver.close();
				driver.quit();
			}catch(Exception e3) {System.out.println(e3.getMessage());}
			System.out.println(e.getMessage());
			org.testng.Assert.fail("Unable to analyse accessibility report: " + e.getMessage().toString());
		}
	}

	private static pageUI prepare_screenshot(AppiumDriver driver, ReportiumClient reportiumClient, pageUI page,
			Android_Scanner scan, String fulldetails, String expected_text) {
		page = GraphicUtils.setUiElement(driver, reportiumClient, page, expected_text, scan.getleftX(), scan.gettopY(), scan.getrightX() - scan.getleftX(), scan.getbottomY() - scan.gettopY());
		if(expected_text.contains("Actual")) {
			assertFailure(driver, reportiumClient, "json:" + fulldetails, expected_text );
		}else {
			if(scan.getcontentDescription()!=null) {
				assertFailure(driver, reportiumClient, "json:" + fulldetails, "Accessibility of json id: " + scan.getid() + ", contentDescription: " + scan.getcontentDescription() + " failed with message: " + scan.getmessage());
			}else {
				assertFailure(driver, reportiumClient, "json:" + fulldetails, "Accessibility of json id: " + scan.getid() + ", class: " + scan.getclass() + " failed with message: " + scan.getmessage());
			}
		}
		return page;
	}

	private static void assertFailure(AppiumDriver driver, ReportiumClient reportiumClient, String comment, String desc) {
		System.setProperty("accessibility_failed", "true");
		System.out.println("Fail: " + desc);
		Utils.report_comment(driver, comment);
		reportiumClient.reportiumAssert(desc, false);
	}

	private static boolean assertPass(ReportiumClient reportiumClient, String desc) {
		System.out.println("Pass: " + desc);
		reportiumClient.reportiumAssert(desc, true);
		return true;
	}

	public static void report_comment(AppiumDriver driver, String text){
		HashMap<Object, Object> params = new HashMap<>();
		params.put("text", text);
		driver.executeScript("mobile:comment", params);
	}
}

