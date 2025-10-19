package rahulshettyacademy.helpers;

import java.io.File;
import java.io.IOException;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
public class ExtentReportHelper {

    static ExtentReports extent;
    @SneakyThrows
    public static ExtentReports getReportObject()  {
        File report;
        report = new File(new File("target"), "reports");
        try {
            FileUtils.forceMkdir(report);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File reportHTML = new File(report,"index.html");
        ExtentSparkReporter reporter = new ExtentSparkReporter(reportHTML);
        reporter.config().setReportName("Automation Test Results");
        reporter.config().setDocumentTitle("Test Results");
        extent = new ExtentReports();
        extent.attachReporter(reporter);
        return extent;
    }
}
