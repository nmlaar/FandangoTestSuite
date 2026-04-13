package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import com.aventstack.extentreports.*;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import java.lang.reflect.Method;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BaseTest {
    protected static ExtentReports extent;
    protected ExtentTest test;
    protected static final String BASE_URL = "https://www.fandango.com/";
    protected static final String MOVIE_URL = "https://www.fandango.com/the-super-mario-galaxy-movie-" +
            "2026-242307/movie-overview";
    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeSuite
    public void setupReport() {
        extent = ReportManager.getInstance();
    }

    @BeforeMethod
    public void setUp(Method method) {
        test = extent.createTest(method.getName());
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 14_6) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.default_content_setting_values.notifications", 2
        ));

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        maskAutomationFlag();
        driver.get(BASE_URL);
        clearBrowserState();
        dismissBrowserAlertIfPresent();
        dismissPopups();
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result != null) {
            if (result.getStatus() == ITestResult.FAILURE) {
                String screenshotPath = takeScreenshot(result.getName());
                test.fail(result.getThrowable());

                if (screenshotPath != null) {
                    test.addScreenCaptureFromPath(screenshotPath);
                }

            } else if (result.getStatus() == ITestResult.SUCCESS) {
                test.pass("Test passed");
            } else {
                test.skip("Test skipped");
            }
        }
        driver.quit();
    }

    @AfterSuite
    public void tearDownReport() {
        extent.flush();
    }

    protected void clearBrowserState() {
        try {
            driver.manage().deleteAllCookies();
        } catch (Exception ignored) {}

        try {
            ((JavascriptExecutor) driver).executeScript(
                    "try { window.localStorage.clear(); } catch(e) {}" +
                            "try { window.sessionStorage.clear(); } catch(e) {}"
            );
        } catch (Exception ignored) {}
    }

    protected Optional<WebElement> findVisibleElement(By... locators) {
        for (By locator : locators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (element.isDisplayed()) {
                        return Optional.of(element);
                    }
                } catch (Exception ignored) {
                    // Ignore stale or detached elements while scanning fallbacks.
                }
            }
        }
        return Optional.empty();
    }

    protected boolean clickIfPresent(By... locators) {
        Optional<WebElement> element = findVisibleElement(locators);
        if (element.isEmpty()) {
            return false;
        }

        try {
            wait.until(ExpectedConditions.elementToBeClickable(element.get())).click();
            return true;
        } catch (Exception ignored) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element.get());
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        }
    }

    protected void dismissPopups() {
        dismissBrowserAlertIfPresent();

        By[] popupButtons = new By[] {
                By.xpath("//*[@id='onetrust-accept-btn-handler']"),
                By.xpath("//button[contains(normalize-space(),'Accept')]"),
                By.xpath("//button[contains(normalize-space(),'No Thanks')]"),
                By.xpath("//button[contains(normalize-space(),'Ok')]")
        };

        for (By locator : popupButtons) {
            clickIfPresent(locator);
        }
    }

    protected String takeScreenshot(String testName) {
        try {
            Path screenshotsDirectory = Path.of("target", "screenshots");
            Files.createDirectories(screenshotsDirectory);

            String sanitizedName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
            Path screenshotPath = screenshotsDirectory.resolve(sanitizedName + ".png");

            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.copy(new java.io.ByteArrayInputStream(screenshot),
                    screenshotPath,
                    StandardCopyOption.REPLACE_EXISTING);

            return screenshotPath.toString();

        } catch (IOException ignored) {
            return null;
        }
    }

    private void maskAutomationFlag() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});"
            );
        } catch (Exception ignored) {
            // Ignore if the browser blocks script injection that early.
        }
    }

    private void dismissBrowserAlertIfPresent() {
        try {
            Alert alert = driver.switchTo().alert();
            alert.dismiss();
        } catch (NoAlertPresentException ignored) {
            // No browser alert is present.
        } catch (Exception ignored) {
            // Some sites block alert access during navigation; it is safe to ignore here.
        }
    }
}