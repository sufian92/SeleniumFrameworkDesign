package rahulshettyacademy.helpers;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static rahulshettyacademy.helpers.ConfigurationHelper.SNAPSHOTSTRATEGY_VIEWPORT;
import static rahulshettyacademy.helpers.ConfigurationHelper.VALUE_SNAPSHOT_IGNORE_MODE;
import static rahulshettyacademy.helpers.ConfigurationHelper.VALUE_SNAPSHOT_MODE;
import static rahulshettyacademy.helpers.ConfigurationHelper.VALUE_SNAPSHOT_UPDATE_MODE;
public class SnapShotHelper {

    /**
     * Method that takes a snapshot of the UI
     * @return Screenshot
     */
    private static Screenshot takeSnapShot(WebDriver driver,
                                           String snapShotName,
                                           Set<By> locators,
                                           boolean forDialog) throws IOException {

        try {
            // remove fonts from page
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    "document.querySelectorAll(\"link[href='https://fonts.googleapis.com/css?family=Roboto:300,400,500']\")[0].remove();");
        } catch (Exception e) {
            // TODO Auto-generated catch block

        }

        boolean scrollViewportAllowed = ConfigurationHelper.INSTANCE
                .getProperty(ConfigurationHelper.SNAPSHOTSTRATEGY, SNAPSHOTSTRATEGY_VIEWPORT)
                .trim().equalsIgnoreCase(SNAPSHOTSTRATEGY_VIEWPORT);

        boolean doViewport = !forDialog;
        final AShot aShot = new AShot();
        if(locators != null && !locators.isEmpty()) {
            aShot.coordsProvider(new WebDriverCoordsProvider());
            aShot.ignoredElements(locators);
            //issue due locators
            doViewport = true;
        }

        if (doViewport && scrollViewportAllowed) {
            Integer wait = Integer.parseInt(ConfigurationHelper.INSTANCE
                    .getProperty(ConfigurationHelper.SNAPSHOTS_VIEWPORT_WAIT,
                            ConfigurationHelper.SNAPSHOTS_VIEWPORT_WAIT_DEFAULT));
            aShot.shootingStrategy(ShootingStrategies.viewportPasting(wait));
        }

        Screenshot actualScreenshot = aShot.takeScreenshot(driver);
        File actualImages;
        actualImages = new File(new File("target/reports"), "actualImages");
        FileUtils.forceMkdir(actualImages);
        File screenshot = new File(actualImages, snapShotName + ".png");
        ImageIO.write(actualScreenshot.getImage(), "png", screenshot);
        return actualScreenshot;
    }

    /**
     * Method that gets the expected snapshot from file
     * @return Screenshot
     */
    private static Screenshot getExpectedSnapShot(File file) throws IOException {

        Screenshot expectedScreenshot = new Screenshot(ImageIO.read(file));

        return expectedScreenshot;
    }

    /**
     * Method that compares two snapshots
     */
    private static void compareImages(Screenshot expectedScreenshot,Screenshot actualScreenshot,String markedImageName,
                                      boolean ignoreMode) throws Exception {

        Set<Coords> ignoredAreas = actualScreenshot.getIgnoredAreas();
        Set<Coords> coordsTocompare = actualScreenshot.getCoordsToCompare();

        expectedScreenshot.setIgnoredAreas(ignoredAreas);
        expectedScreenshot.setCoordsToCompare(coordsTocompare);

        File diffFile = null;
        File diffFileTransparent = null;
        ImageDiff diff = new ImageDiffer().makeDiff(expectedScreenshot, actualScreenshot);
        int size = diff.getDiffSize();

        final int pixelsThreshold = Integer.parseInt(ConfigurationHelper.INSTANCE.getProperty(
                ConfigurationHelper.PIXELS_THRESHOLD,
                "0"
        ));

        if (size > pixelsThreshold) {

            File reports;
            reports = new File("target/reports");

            File markedImages = new File(reports, "markedImages");
            File markedImagesTransparent = new File(reports, "markedImagesTransparent");
            FileUtils.forceMkdir(markedImages);
            FileUtils.forceMkdir(markedImagesTransparent);
            diffFile = new File(markedImages, markedImageName + ".png");
            diffFileTransparent = new File(markedImagesTransparent, markedImageName + "Transparent.png");
            ImageIO.write(diff.getMarkedImage(), "png", diffFile);
            ImageIO.write(diff.getTransparentMarkedImage(), "png", diffFileTransparent);
            if(!ignoreMode) {
                throw new Exception("There are some layout issues on the page. Number of different pixels detected: " + size);
            }
        }

    }

    /**
     * Method that overrides the expected images
     */
    private static void overrideExpectedImages(Screenshot actualScreenshot, String snapshot) throws Exception {

        File screenshot = null;
        String runEnvironment = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.RUN_ENVIRONMENT)
                .toLowerCase();

        switch (runEnvironment) {
            case ConfigurationHelper.VALUE_RUN_ENVIROMENT_LOCAL:

                screenshot = new File("./src/test/resources/expectedImages/" + snapshot + ".png");

                ImageIO.write(actualScreenshot.getImage(), "png", screenshot);

                break;

            case ConfigurationHelper.VALUE_RUN_ENVIROMENT_JENKINS:

                System.out.println("image is available in actual screenshots folder");

                break;
        }


    }

    /**
     * Method that takes snapshot of UI, compares it to an expected snapshot and creates a marked .png image highlighting differences
     * @param driver - driver instance
     * @param snapShotName - Name of the actual and expected snapshots
     * @param file - The expected snapshot file
     */
    public static void compareSnapShots(WebDriver driver, String snapShotName,File file) throws Exception {

        compareSnapShots(driver, snapShotName, file, null);
    }

    /**
     * Method that takes snapshot of UI, compares it to an expected snapshot and creates a marked .png image highlighting differences
     * @param driver - driver instance
     * @param snapShotName - Name of the actual and expected snapshots
     * @param file - The expected snapshot file
     */
    public static void compareSnapShotsDialog(WebDriver driver, String snapShotName,File file) throws Exception {

        compareSnapShots(driver, snapShotName, file, null, true);
    }

    /**
     * Method that takes snapshot of UI, compares it to an expected snapshot and creates a marked .png image highlighting differences
     * @param driver - driver instance
     * @param snapShotName - Name of the actual and expected snapshots
     * @param file - The expected snapshot file
     * @param forDialog - in case you need a screenshot of a dialog.
     */
    public static void compareSnapShots(WebDriver driver, String snapShotName,File file, boolean forDialog) throws Exception {

        compareSnapShots(driver, snapShotName, file, null, forDialog);
    }

    /**
     * Method that takes snapshot of UI, compares it to an expected snapshot and creates a marked .png image highlighting differences
     * @param driver - driver instance
     * @param snapShotName - Name of the actual and expected snapshots
     * @param file - The expected snapshot file
     * @param ignoredElements - The ignored elements that will not be considered.e.g: Timestamps
     */
    public static void compareSnapShots(WebDriver driver, String snapShotName,File file,Set<By> ignoredElements) throws Exception {

        compareSnapShots(driver, snapShotName, file, ignoredElements, false);
    }

    /**
     * Method that takes snapshot of UI, compares it to an expected snapshot and creates a marked .png image highlighting differences
     * @param driver - driver instance
     * @param snapShotName - Name of the actual and expected snapshots
     * @param file - The expected snapshot file
     * @param ignoredElements - The ignored elements that will not be considered.e.g: Timestamps
     */
    public static void compareSnapShots(WebDriver driver,
                                        String snapShotName,
                                        File file,
                                        Set<By> ignoredElements,
                                        boolean forDialog) throws Exception {

        Screenshot actualScreenshot = takeSnapShot(driver, snapShotName,ignoredElements, forDialog);

        String mode =
                ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.SNAPSHOTMODE, VALUE_SNAPSHOT_MODE);
        if (mode.equalsIgnoreCase(VALUE_SNAPSHOT_UPDATE_MODE)) {
            overrideExpectedImages(actualScreenshot,snapShotName);
        } else {
            Screenshot expectedScreenshot = getExpectedSnapShot(file);
            compareImages(expectedScreenshot, actualScreenshot, snapShotName,
                    mode.equalsIgnoreCase(VALUE_SNAPSHOT_IGNORE_MODE));
        }
    }
}
