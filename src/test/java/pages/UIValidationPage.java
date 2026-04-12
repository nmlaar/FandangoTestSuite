package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class UIValidationPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Locators ────────────────────────────────────────────────────────────────

    private final By[] imageLocators = new By[] {
            By.tagName("img")
    };

    private final By[] linkLocators = new By[] {
            By.tagName("a")
    };

    private final By[] buttonLocators = new By[] {
            By.tagName("button"),
            By.cssSelector("a[role='button']"),
            By.cssSelector("input[type='submit']")
    };

    // ── Constructor ─────────────────────────────────────────────────────────────

    public UIValidationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ── Images ────────────────────────────────────────────────────────────────────

    /**
     * Returns the total number of {@code <img>} elements found on the page
     * (visible or not). A non-zero count confirms images are present in the DOM.
     */
    public int getTotalImageCount() {
        return driver.findElements(By.tagName("img")).size();
    }

    /**
     * Counts images whose {@code naturalWidth} property is 0, which indicates
     * the image file failed to load. Returns the count of broken images.
     */
    public int getBrokenImageCount() {
        int broken = 0;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (WebElement img : driver.findElements(By.tagName("img"))) {
            try {
                Object width = js.executeScript("return arguments[0].naturalWidth;", img);
                if (width instanceof Long && (Long) width == 0) broken++;
            } catch (Exception ignored) {}
        }
        return broken;
    }

    /**
     * Returns true when all images on the page have loaded successfully
     * (i.e. no image has naturalWidth == 0).
     */
    public boolean allImagesLoaded() {
        return getBrokenImageCount() == 0;
    }

    // ── Links ─────────────────────────────────────────────────────────────────────

    /**
     * Returns every href value found on the page.
     */
    public List<String> getAllLinkHrefs() {
        List<String> hrefs = new ArrayList<>();
        for (WebElement anchor : driver.findElements(By.tagName("a"))) {
            try {
                String href = anchor.getAttribute("href");
                if (href != null && !href.isBlank()) hrefs.add(href);
            } catch (Exception ignored) {}
        }
        return hrefs;
    }

    /**
     * Returns hrefs that are obviously broken — empty string, "#", or
     * "javascript:void(0)" — without making network requests.
     */
    public List<String> getObviouslyBrokenLinks() {
        List<String> broken = new ArrayList<>();
        for (WebElement anchor : driver.findElements(By.tagName("a"))) {
            try {
                String href = anchor.getAttribute("href");
                if (href == null || href.isBlank()
                        || href.equals("#")
                        || href.equalsIgnoreCase("javascript:void(0)")) {
                    String text = anchor.getText().trim();
                    if (!text.isBlank()) broken.add(text + " → " + href);
                }
            } catch (Exception ignored) {}
        }
        return broken;
    }

    /**
     * Returns true when no obviously broken links are found on the page.
     */
    public boolean hasNoBrokenLinks() {
        return getObviouslyBrokenLinks().isEmpty();
    }

    // ── Buttons ───────────────────────────────────────────────────────────────────

    /**
     * Returns the count of visible buttons that are enabled and clickable.
     */
    public int getClickableButtonCount() {
        int count = 0;
        for (By locator : buttonLocators) {
            for (WebElement element : driver.findElements(locator)) {
                try {
                    if (element.isDisplayed() && element.isEnabled()) count++;
                } catch (Exception ignored) {}
            }
        }
        return count;
    }

    /**
     * Returns true when at least one button is visible and enabled.
     */
    public boolean hasClickableButtons() {
        return getClickableButtonCount() > 0;
    }

    // ── Responsive Layout ─────────────────────────────────────────────────────────

    /**
     * Resizes the browser window to {@code width} × {@code height} pixels,
     * then returns true when the body width reported by JavaScript is ≤ the
     * requested width, confirming the layout responded to the resize.
     */
    public boolean isLayoutResponsive(int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
        try {
            Thread.sleep(500); // allow CSS transitions to settle
        } catch (InterruptedException ignored) {}
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object bodyWidth = js.executeScript("return document.body.scrollWidth;");
        if (bodyWidth instanceof Long) {
            return (Long) bodyWidth <= width + 20; // 20px tolerance for scrollbars
        }
        return true;
    }

    /**
     * Restores the browser window to a standard desktop size.
     */
    public void restoreDesktopSize() {
        driver.manage().window().setSize(new Dimension(1280, 800));
        driver.manage().window().maximize();
    }

    // ── Fonts and Colors ──────────────────────────────────────────────────────────

    /**
     * Uses JavaScript to read the computed font-family of the {@code <body>}
     * element. Returns the value as a string (e.g. "Roboto, sans-serif").
     */
    public String getBodyFontFamily() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            return (String) js.executeScript(
                    "return window.getComputedStyle(document.body).fontFamily;");
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Returns true when the body's computed font-family is non-empty,
     * confirming that a CSS font is applied.
     */
    public boolean isFontApplied() {
        return !getBodyFontFamily().isBlank();
    }

    /**
     * Uses JavaScript to read the computed background-color of the
     * {@code <body>} element. Returns the raw CSS value string.
     */
    public String getBodyBackgroundColor() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            return (String) js.executeScript(
                    "return window.getComputedStyle(document.body).backgroundColor;");
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Returns true when a background-color value is applied to the body
     * and it is not fully transparent ("rgba(0, 0, 0, 0)").
     */
    public boolean isBackgroundColorApplied() {
        String color = getBodyBackgroundColor();
        return !color.isBlank() && !color.equals("rgba(0, 0, 0, 0)");
    }
}
