package com.perfecto.sampleproject.Utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This class is the utils class for exporting Reportium data
 */
public class Reporting {

	// ***************************************************************************************
	// Set your "CQL_NAME" and the "PERFECTO_SECURITY_TOKEN" in the ReportiumExportUtils file.
	// ***************************************************************************************

	public static Path main(String executionId, String scriptName, String deviceId) throws Exception {
		Thread.sleep(5000);
		Path exportRoot = Files.createTempDirectory("Accessibility");

		Path testFolder = null;
		// get all the tests of the execution as JSON
		JsonObject testExecutionsJson = ReportiumExportUtils.retrieveTestExecutions(executionId);
		JsonArray testExecutionsArray = testExecutionsJson.getAsJsonArray("resources");
		if (testExecutionsArray.size() == 0) {
			System.out.println("There are no test executions for that driver execution ID");
		} else {
			int testCounter = 1;
			// store each test's data in a separate folder
			for (JsonElement testExecutionElement : testExecutionsArray) {
				JsonObject testJson = testExecutionElement.getAsJsonObject();
				String testId = testJson.get("id").getAsString();
				String testName = testJson.get("name").getAsString();

				testFolder = Paths.get(exportRoot.toString(), "test-" + String.format("%03d", testCounter) + "-" + FilenameUtils.normalize(testName));
				Files.createDirectories(testFolder);

				downloadAttachments(testJson, testFolder);

				testCounter++;
			}
		}
		return 	testFolder;
	}

	private static void downloadAttachments(JsonObject testJson, Path testFolder) throws IOException, URISyntaxException {
		String testName = testJson.get("name").getAsString();
		JsonArray attachmentsArray = testJson.getAsJsonArray("artifacts");
		if (attachmentsArray.size() > 0) {
			Path attachmentsDir = Paths.get(testFolder.toString(), "attachments");
			Files.createDirectories(attachmentsDir);

			for (JsonElement attachmentElement : attachmentsArray) {
				JsonObject artifactJson = attachmentElement.getAsJsonObject();
				String type = artifactJson.get("type").getAsString();
				Path attachmentDir = Paths.get(attachmentsDir.toString(), type.toLowerCase());
				Files.createDirectories(attachmentDir);
				String path = artifactJson.get("path").getAsString();
				Path artifactPath = Paths.get(attachmentDir.toString(), FilenameUtils.getName(path));
				ReportiumExportUtils.downloadFileToFS(artifactPath, new URI(path));
			}
		} else {
			System.out.println("No attachments found for test execution '" + testName + "'");
		}
	}

	private static void downloadVideos(JsonObject testJson, Path testFolder) throws IOException, URISyntaxException {
		String testName = testJson.get("name").getAsString();
		JsonArray videosArray = testJson.getAsJsonArray("videos");
		if (videosArray.size() > 0) {
			Path videosDir = Paths.get(testFolder.toString(), "videos");
			Files.createDirectories(videosDir);

			for (JsonElement videosElement : videosArray) {
				JsonObject videoJson = videosElement.getAsJsonObject();
				String downloadUrl = videoJson.get("downloadUrl").getAsString();
				Path videoPath = Paths.get(videosDir.toString(), FilenameUtils.getName(downloadUrl));
				ReportiumExportUtils.downloadFileToFS(videoPath, new URI(downloadUrl));
			}
		} else {
			System.out.println("No videos found for test execution '" + testName + "'");
		}
	}
}