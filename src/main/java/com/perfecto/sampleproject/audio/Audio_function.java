package com.perfecto.sampleproject.audio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.model.Job;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResultFactory;
import com.perfecto.sampleproject.Utils.Utils;

import io.appium.java_client.android.AndroidDriver;


/**
 * This template is for users that use DigitalZoom Reporting (ReportiumClient).
 * For any other use cases please see the basic template at https://github.com/PerfectoCode/Templates.
 * For more programming samples and updated templates refer to the Perfecto Documentation at: http://developers.perfectomobile.com/
 */
public class Audio_function {

	public static void main(String[] args) throws MalformedURLException, IOException {
		System.out.println("Run started");

		String browserName = "";
		DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
		String host = "ps.perfectomobile.com";
		capabilities.setCapability("user", "genesist@perfectomobile.com");
		capabilities.setCapability("securityToken", "");
		capabilities.setCapability("deviceName", "RF8M329FK3X");
		capabilities.setCapability("platform", "Android");
		capabilities.setCapability("pureAppiumBehaviour", true);
		capabilities.setCapability("useAppiumForHybrid", true);

		// Use the automationName capability to define the required framework - Appium (this is the default) or PerfectoMobile.
		capabilities.setCapability("automationName", "Appium");

		capabilities.setCapability("scriptName", "Screenreader_Android");

		AndroidDriver driver = new AndroidDriver(new URL("https://" + host + "/nexperience/perfectomobile/wd/hub"), capabilities);
		//      IOSDriver driver = new IOSDriver(new URL("https://" + host + "/nexperience/perfectomobile/wd/hub"), capabilities);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

		// Reporting client. For more details, see http://developers.perfectomobile.com/display/PD/Reporting
		PerfectoExecutionContext perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
				.withProject(new Project("My Project", "1.0"))
				.withJob(new Job("My Job", 45))
				.withContextTags("tag1")
				.withWebDriver(driver)
				.build();
		ReportiumClient reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);

		try {
			reportiumClient.testStart("Screenreader_Android", new TestContext("screenreader"));

			// write your code here


			reportiumClient.stepStart("Open You Tube");

			// close the app in case it is running from previous iteration
			try {
				Map<String, Object> params2 = null;
				params2 = new HashMap<>();
				params2.put("name", "YouTube");
				driver.executeScript("mobile:application:close", params2);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Map<String, Object> params = new HashMap<>();
			params.put("name", "YouTube");
			driver.executeScript("mobile:application:open", params);
			driver.context("NATIVE_APP");
			Utils.maxVolume(driver);
			Thread.sleep(3000);
			reportiumClient.stepEnd();
			reportiumClient.stepStart("Navigate to audio search");
			WebDriverWait wait = new WebDriverWait(driver, 30);
			WebElement search = wait.until(ExpectedConditions.visibilityOf(driver.findElementByXPath("//*[@content-desc=\"Search\"]")));
			params = new HashMap<>();
			params.put("tag", "login-screen");
			driver.executeScript("mobile:checkAccessibility:audit", params);
			search.click();
			driver.findElementByXPath("//*[@resource-id=\"com.google.android.youtube:id/voice_search\"]").click();
			reportiumClient.stepStart("Run Audio Search");
			// Create audio file from String
			String key = "PRIVATE:mysong.wav";
			Map<String, Object> params6 = new HashMap<>();
			params6.put("text","Metallica Master of Puppets");
			params6.put("repositoryFile",key);
			driver.executeScript("mobile:text:audio", params6);
			// inject Audio to device
			Map<String, Object> params1 = new HashMap<>();
			params1.put("key",key);
			params1.put("wait","wait");
			driver.executeScript("mobile:audio:inject", params1);
			// verify audio -> text is displayed on the screen.
			Map<String, Object> params5 = new HashMap<>();
			params5.put("content", "Metallica Master of Puppets");
			params5.put("timeout", 30);
			String res = (String) driver.executeScript("mobile:checkpoint:text", params5);
			Thread.sleep(10000);
			// Click on visible text
			params6 = new HashMap<>();
			params6.put("label", "Metallica Master of Puppets");
			params6.put("timeout", 30);
			String res2 = (String) driver.executeScript("mobile:button-text:click", params6);
			
			Thread.sleep(10000);
			Map<String, Object> params4 = new HashMap<>();
			params4.put("timeout", "30");
			params4.put("duration","1");
			String audioR = (String) driver.executeScript("mobile:checkpoint:audio", params4);

			Map<String, Object> params2 = null;
			params2 = new HashMap<>();
			params2.put("name", "YouTube");
			driver.executeScript("mobile:application:close", params2);


			if(audioR.equalsIgnoreCase("true")) {
				reportiumClient.reportiumAssert("Audio is playing",true);
				reportiumClient.testStop(TestResultFactory.createSuccess());

			} else {
				reportiumClient.reportiumAssert("Audio failed",false);
				reportiumClient.testStop(TestResultFactory.createFailure("Audio failed"));
			}

		} catch (Exception e) {
			reportiumClient.testStop(TestResultFactory.createFailure(e.getMessage(), e));
			e.printStackTrace();
		} finally {
			try {
				driver.quit();

				// Retrieve the URL to the DigitalZoom Report (= Reportium Application) for an aggregated view over the execution
				String reportURL = reportiumClient.getReportUrl();
				System.out.println(reportURL);
				// Retrieve the URL to the Execution Summary PDF Report
				String reportPdfUrl = (String)(driver.getCapabilities().getCapability("reportPdfUrl"));
				// For detailed documentation on how to export the Execution Summary PDF Report, the Single Test report and other attachments such as
				// video, images, device logs, vitals and network files - see http://developers.perfectomobile.com/display/PD/Exporting+the+Reports

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("Run ended");
	}
}
