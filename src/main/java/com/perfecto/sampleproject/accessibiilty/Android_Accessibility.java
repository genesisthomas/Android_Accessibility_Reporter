package com.perfecto.sampleproject.accessibiilty;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.model.Job;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResult;
import com.perfecto.reportium.test.result.TestResultFactory;
import com.perfecto.sampleproject.Utils.Utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;



/**
 * This template is for users that use DigitalZoom Reporting (ReportiumClient).
 * For any other use cases please see the basic template at https://github.com/PerfectoCode/Templates.
 * For more programming samples and updated templates refer to the Perfecto Documentation at: http://developers.perfectomobile.com/
 */
public class Android_Accessibility {
	AppiumDriver driver;
	ReportiumClient reportiumClient;
	@Test
	public void appiumTest() throws Exception {
		System.out.println("Run started");
		String browserName = "";
		DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
		//Replace <<cloud name>> with your perfecto cloud name (e.g. demo) or pass it as maven properties: -DcloudName=<<cloud name>>  
		String cloudName = "<<cloud name>>";
		//Replace <<security token>> with your perfecto security token or pass it as maven properties: -DsecurityToken=<<SECURITY TOKEN>>  More info: https://developers.perfectomobile.com/display/PD/Generate+security+tokens

		String securityToken = "<<security token>>";


		capabilities.setCapability("securityToken", Utils.fetchSecurityToken(securityToken));
		capabilities.setCapability("model", "Galaxy S10");

//				String app = "com.sec.android.app.popupcalculator";
//				String tag = "calculator";
//				String json = "/Users/genesisthomas/eclipse-workspace/Accessibility_Testing/Accessibility/calc_accessibility_report.json";

//				String app = "com.samsung.android.dialer";
//				String tag = "dialer";
//				String json = "/Users/genesisthomas/eclipse-workspace/Accessibility_Testing/Accessibility/Accessibility Report dialer-screen 08-39-36-476.json";


		//		String app = "io.perfecto.expense.tracker.hybrid";
		//		String tag = "expense";
		//		String json = "/Users/genesisthomas/eclipse-workspace/Accessibility_Testing/Accessibility/Accessibility Report expense 11-50-59-234.json";

		String app = "com.android.vending";
		String tag = "playstore";
		String json = "/Users/genesisthomas/eclipse-workspace/Accessibility_Testing/Accessibility/Accessibility Report playstore 15-14-17-898.json";
		String json2 = "/Users/genesisthomas/eclipse-workspace/Accessibility_Testing/Accessibility/Accessibility Report books 15-14-24-143.json";

		capabilities.setCapability("platform", "Android");
		capabilities.setCapability("pureAppiumBehaviour", false);
		capabilities.setCapability("appPackage", app);
		capabilities.setCapability("automationName", "Appium");
		capabilities.setCapability("scriptName", "Android_Accessibility");

		driver = new AndroidDriver(new URL("https://" + Utils.fetchCloudName(cloudName)  + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
		//      IOSDriver driver = new IOSDriver(new URL("https://" + host + "/nexperience/perfectomobile/wd/hub"), capabilities);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

		// Reporting client. For more details, see http://developers.perfectomobile.com/display/PD/Reporting
		PerfectoExecutionContext perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
				.withProject(new Project("My Project", "1.0"))
				.withJob(new Job("My Job", 45))
				.withContextTags("tag1")
				.withWebDriver(driver)
				.build();
		reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);
		reportiumClient.testStart("Android_Accessibility", new TestContext("accessibility"));
		reportiumClient.stepStart("Open App");
		// close the app in case it is running from previous iteration
		try {
			Map<String, Object> params2 = null;
			params2 = new HashMap<>();
			params2.put("identifier", app);
			driver.executeScript("mobile:application:close", params2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, Object> params = new HashMap<>();
		params.put("identifier", app);
		driver.executeScript("mobile:application:open", params);
		driver.context("NATIVE_APP");
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Check Accessibility Audit");
		//PRECONDITION:
		System.setProperty("accessibility_failed", "false");

		params = new HashMap<>();
		params.put("tag", tag);
		driver.executeScript("mobile:checkAccessibility:audit", params);
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Begin Accessibility verifier for " + tag);
		Utils.accessibility_highlighter(driver, reportiumClient, tag, json);

		driver.findElement(By.xpath("//*[@text=\"Books\"]")).click();
		driver.findElement(By.xpath("//*[@text=\"Ebooks\"]")).isDisplayed();

		params = new HashMap<>();
		tag = "books";
		params.put("tag", tag);
		driver.executeScript("mobile:checkAccessibility:audit", params);
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Begin Accessibility verifier for " + tag);
		Utils.accessibility_highlighter(driver, reportiumClient, tag, json2);

		if(System.getProperty("accessibility_failed").equalsIgnoreCase("true")){
			throw new RuntimeException("Accessibility errors are found");
		}

	}


	@AfterMethod
	public void afterMethod(ITestResult result) {
		TestResult testResult = null;
		if(result.getStatus() == result.SUCCESS) {
			testResult = TestResultFactory.createSuccess();
		}
		else if (result.getStatus() == result.FAILURE) {
			testResult = TestResultFactory.createFailure(result.getThrowable());
		}
		reportiumClient.testStop(testResult);
		// Retrieve the URL to the DigitalZoom Report 
		String reportURL = reportiumClient.getReportUrl();
		System.out.println(reportURL);
		driver.close();
		driver.quit();

	}

}
