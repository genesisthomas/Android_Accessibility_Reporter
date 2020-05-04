package com.perfecto.sampleproject.accessibiilty;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
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


public class Android_Pass_scenario {
	AppiumDriver driver;
	ReportiumClient reportiumClient;
	ThreadLocal executionId = new ThreadLocal();  
	String scriptName = getClass().getSimpleName();
	String appPackage  = "io.perfecto.expense.tracker.hybrid";
	String[] tag = {"expense"};
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
		capabilities.setCapability("model", "Galaxy S6");
		capabilities.setCapability("platform", "Android");
		capabilities.setCapability("pureAppiumBehaviour", false);
		capabilities.setCapability("appPackage", appPackage);
		capabilities.setCapability("automationName", "Appium");
		capabilities.setCapability("scriptName", scriptName);
		try {
			driver = new AndroidDriver(new URL("https://" + Utils.fetchCloudName(cloudName)  + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
		}catch(Exception e){
			throw new RuntimeException("Unable to create driver. " + e.getMessage());
		}
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		PerfectoExecutionContext perfectoExecutionContext = null;
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
		reportiumClient.testStart(scriptName, new TestContext(scriptName));
		audit(appPackage, tag[0]);

		//post condition:
		try {
			if(System.getProperty("accessibility_failed").equalsIgnoreCase("true")){
				org.testng.Assert.fail("Accessibility errors exists!");
			}
		}catch(Exception e) {}
	}


	private void audit(String app, String tag) throws Exception {
		reportiumClient.stepStart("Open App: " + app);
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
		driver.getPageSource().toString();
		params = new HashMap<>();
		params.put("tag", tag);
		driver.executeScript("mobile:checkAccessibility:audit", params);
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Take local screenshot for " + tag);
		Utils.take_screenshot(driver, reportiumClient, tag);
		reportiumClient.stepEnd();
	}


	@AfterMethod
	public void afterMethod(ITestResult result) throws Exception {
		TestResult testResult = null;
		if(result.getStatus() == result.SUCCESS) {
			testResult = TestResultFactory.createSuccess();
		}
		else if (result.getStatus() == result.FAILURE) {
			testResult = TestResultFactory.createFailure(result.getThrowable());
		}
		reportiumClient.testStop(testResult);
		String reportURL = reportiumClient.getReportUrl();
		System.out.println(reportURL);

		Capabilities capabilities = driver.getCapabilities();
		executionId.set((String) capabilities.getCapability("executionId"));
		String deviceId = (String) capabilities.getCapability("deviceName");

		driver.close();
		driver.quit();
		Utils.accessibility_highlighter(driver, reportiumClient, executionId.get().toString(), scriptName, deviceId, tag, appPackage);
	}


}
