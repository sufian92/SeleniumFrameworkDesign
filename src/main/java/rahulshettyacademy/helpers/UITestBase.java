package rahulshettyacademy.helpers;

import rahulshettyacademy.enums.SnapShotEnum;
import rahulshettyacademy.helpers.ExtentReportHelper;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
public class UITestBase extends TestWatcher {

    public static String CONN_URL = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.DB_URL);
    public static String USER = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.DBUSERNAME);
    public static String PASSWORD = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.DBPASSWORD);
    public static Logger ROOT_LOGGER = Logger.getLogger("org.eib.automation");

    @Getter
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Rule
    public TestName name = new TestName();
    @Getter
    private ConfigurationHelper config = ConfigurationHelper.INSTANCE;
    public String browser;
    protected static String testName;

    protected WebDriver driver;

    protected LocalTime timestamp = LocalTime.now();
    protected ThreadLocal<WebDriver> driverThread = new ThreadLocal<WebDriver>();
    protected ThreadLocal<String> testNameThread = new ThreadLocal<String>();

    protected ExtentTest test;
    protected static ExtentReports extent = ExtentReportHelper.getReportObject();
    protected ThreadLocal<ExtentTest> extentTest = new ThreadLocal<ExtentTest>();

    protected static ArrayList<String> failedTests = new ArrayList<String>();

    protected ThreadLocal<String> actualScreenshotNameThread = new ThreadLocal<String>();
    protected ThreadLocal<String> expectedScreenshotNameThread = new ThreadLocal<String>();
    String actualScreenshotName;
    String expectedScreenshotName;
    @Getter
    @Setter
    protected String expectedScreenshotFolder = "./src/test/resources/expectedImages/";
    @Getter
    @Setter
    protected String expectedScreenshotExtension = ".png";

    public UITestBase() {
    }

    public String getProperty(final String propName) {
        return config.getProperty(propName);
    }

    @Rule
    public final TestRule watchman = new TestWatcher() {

        @Override
        protected void starting(Description description) {
            if (browser != null) {
                test = extent.createTest(
                        description.getClassName() + "." + description.getMethodName() + "." + browser);
            } else {

                test = extent.createTest(
                        description.getClassName() + "." + description.getMethodName());
            }
            extentTest.set(test);
            if (Collections.frequency(failedTests, description.getMethodName()) > 0) {
                rootLog("START again test: " + test.getModel().getName());
            } else {
                rootLog("START test: " + test.getModel().getName());
            }
        }

        @Override
        protected void succeeded(Description description) {
            extentTest.get().log(Status.PASS, "Test Passed");
            rootLog("END TEST - SUCCESS : " + extentTest.get().getModel().getName());
        }

        @Override
        @SneakyThrows
        protected void failed(Throwable e, Description description) {

            failedTests.add(description.getMethodName());
            rootLog("END TEST - FAILED number " + Collections.frequency(failedTests, description.getMethodName())
                    + "  :  " + extentTest.get().getModel().getName() + "\n" + e.getMessage());

            File report;
            report = new File(new File("target"), "reports");
            try {
                FileUtils.forceMkdir(report);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            if (ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.RUN_ENVIRONMENT).equalsIgnoreCase("LOCAL") || Collections.frequency(failedTests, description.getMethodName()) > 1) {
                if (e.getMessage().contains("There are some layout issues on the page")) {
                    File expectedImage = new File(
                            "./src/test/resources/expectedImages/" + expectedScreenshotNameThread.get() +".png");

                    File expectedImagesTargetFolder = new File(report, "expectedImages");
                    try {
                        FileUtils.forceMkdir(expectedImagesTargetFolder);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    File expectedImageTarget = new File(expectedImagesTargetFolder, expectedScreenshotNameThread.get() + ".png");
                    try {
                        FileUtils.copyFile(expectedImage, expectedImageTarget);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                    extentTest.get().fail(e);
                    extentTest.get().info("Details of Expected" + "Test screenshot",
                            MediaEntityBuilder
                                    .createScreenCaptureFromPath(
                                            "./expectedImages/" + actualScreenshotNameThread.get() + ".png")
                                    .build());
                    extentTest.get().info("Details of Actual" + "Test screenshot &nbsp; &nbsp;",
                            MediaEntityBuilder
                                    .createScreenCaptureFromPath(
                                            "./actualImages/" + expectedScreenshotNameThread.get() + ".png")
                                    .build());
                    extentTest.get().info("Details of Marked" + "Test screenshot &nbsp;",
                            MediaEntityBuilder
                                    .createScreenCaptureFromPath(
                                            "./markedImages/" + actualScreenshotNameThread.get() + ".png")
                                    .build());
                } else {
                    // take screenshot in case of failure on jenkins
                    File localScreenshots = new File(new File("target/reports"), "screenshots");
                    try {
                        FileUtils.forceMkdir(localScreenshots);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    File screenshot = new File(localScreenshots, testNameThread.get() + "_" + timestamp.getHour()
                            + "." + timestamp.getMinute() + ".png");
                    try {
                        FileUtils.moveFile(
                                ((TakesScreenshot) driverThread.get()).getScreenshotAs(OutputType.FILE),
                                screenshot);
                    } catch (WebDriverException ex) {

                        ex.printStackTrace();

                    } catch (IOException ex) {

                        ex.printStackTrace();
                    }
                    extentTest.get().fail(e);
                    extentTest.get()
                            .addScreenCaptureFromPath("./screenshots/" + testNameThread.get() + "_"
                                            + timestamp.getHour() + "." + timestamp.getMinute() + ".png",
                                    description.getMethodName());
                }

            } else {
                extentTest.get().log(Status.WARNING, "Test Failed Once Before.. " + e.getMessage());
            }
        }

        @Override
        protected void finished(Description description) {

            extent.flush();
            if (driverThread.get() != null) {
                driverThread.get().quit();
            }
        }

        void rootLog(String message) {
            StringBuilder sb = new StringBuilder("\n******************************************\n");
            sb.append(message).append("\n******************************************\n");
            ROOT_LOGGER.info(sb.toString());
        }

    };

    public void setActualAndExpectedImgagesThread(String name){

        actualScreenshotNameThread.set(name);
        expectedScreenshotNameThread.set(name);
    }
}
