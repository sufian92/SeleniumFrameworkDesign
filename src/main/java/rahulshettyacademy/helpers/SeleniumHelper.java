package rahulshettyacademy.helpers;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

public class SeleniumHelper {

    public static WebDriver init(WebDriver driver) throws MalformedURLException {
        String browserName = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.BROWSER).toLowerCase();
        String runConfig = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.RUN_CONFIGURATION).toLowerCase();
        String runEnvironment = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.RUN_ENVIRONMENT).toLowerCase();

        if (runConfig.contains("podman-grid")) {
            if (runEnvironment.equals("jenkins") || runEnvironment.equals("local")) {
                driver = createRemoteWebDriver("edge");
            }
        } else if (runConfig.equals("normal") && runEnvironment.equals("local")) {
            driver = createLocalWebDriver(browserName);
        }

        if (driver != null) {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
 //           driver.manage().window().maximize();
        }
        return driver;
    }


    private static WebDriver createRemoteWebDriver(String browser) throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        if ("chrome".equalsIgnoreCase(browser)) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu", "--headless", "--window-size=1920x1080");
            capabilities.setBrowserName("chrome");
            //capabilities.setVersion("118.0");
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        } else if ("edge".equalsIgnoreCase(browser)) {
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu", "--headless", "--window-size=1920x1080");

            // Set the browser name to "edge"
            capabilities.setBrowserName("MicrosoftEdge");
            //capabilities.setVersion("133.0");

        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        // Create and return the RemoteWebDriver instance pointing to the grid hub
        return new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capabilities);
    }

    private static WebDriver createLocalWebDriver(String browserName) {
        if (browserName.contains("chrome")) {
            ChromeOptions options = new ChromeOptions();
            System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
            if (browserName.contains("headless")) {
                options.addArguments("headless");
            }
            options.addArguments("--remote-allow-origins=*");
            WebDriver driver = new ChromeDriver(options);
            driver.manage().window().setSize(new Dimension(1440, 900));
            return driver;
        } else if (browserName.equals("firefox")) {
            System.setProperty("webdriver.gecko.driver", "/Users/rahulshetty//documents//geckodriver");
            return new FirefoxDriver();
        } else if (browserName.equals("edge")) {
            System.setProperty("webdriver.edge.driver", "edge.exe");
            return new EdgeDriver();
        }
        return null;
    }

    private static void waitForContainer(String containerName) throws InterruptedException, IOException {
        while (true) {
            ProcessBuilder checkProcessBuilder = new ProcessBuilder("bash", "-c", "podman ps --filter name=" + containerName + " --format '{{.Status}}'");
            checkProcessBuilder.redirectErrorStream(true);
            Process checkProcess = checkProcessBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
            String status = reader.readLine();
            if (status != null && status.contains("Up")) {
                break;
            }
            System.out.println("Waiting for container to start...");
            Thread.sleep(2000);
        }
    }
}
