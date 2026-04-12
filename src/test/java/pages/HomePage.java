package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HomePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By[] searchInputLocators = new By[] {
            By.cssSelector("input[placeholder*='movie']"),
            By.xpath("//input[contains(@placeholder,'movie')]"),
            By.xpath("//input[contains(@aria-label,'movie')]"),
            By.xpath("//input[contains(@name,'search')]")
    };

    private final By[] searchButtonLocators = new By[] {
            By.cssSelector("button[type='submit']"),
            By.xpath("//button[normalize-space()='Go']"),
            By.xpath("//button[contains(@aria-label,'Search')]")
    };

    private final By[] trendingSectionLocators = new By[] {
            By.xpath("//h2[contains(normalize-space(),'Movies in Theaters')]"),
            By.xpath("//h2[contains(normalize-space(),'New & Coming soon')]"),
            By.xpath("//h1[contains(normalize-space(),'Movie Tickets and Times')]")
    };

    private final By[] signInLocators = new By[] {
            By.xpath("//a[contains(normalize-space(),'Sign In')]"),
            By.xpath("//a[contains(normalize-space(),'Join')]"),
            By.xpath("//button[contains(normalize-space(),'Sign In')]")
    };

    private final By[] locationInputLocators = new By[] {
            By.cssSelector("input[placeholder*='zipcode']"),
            By.cssSelector("input[placeholder*='zip']"),
            By.xpath("//input[contains(@placeholder,'city, state')]"),
            By.xpath("//input[contains(@placeholder,'zipcode')]")
    };

    private final By[] locationSubmitLocators = new By[] {
            By.xpath("//button[contains(normalize-space(),'Apply')]"),
            By.xpath("//button[contains(normalize-space(),'Save')]"),
            By.xpath("//button[contains(normalize-space(),'Submit')]"),
            By.xpath("//button[normalize-space()='Go']")
    };

    private final By[] pageHeadingLocators = new By[] {
            By.tagName("h1"),
            By.tagName("h2")
    };

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public boolean isLoaded() {
        return driver.getTitle().contains("Fandango") && isAnyElementVisible(trendingSectionLocators);
    }

    /**
     * Returns true when the page title contains "Fandango", without
     * requiring any specific section to be visible. Useful for a quick
     * title-only assertion.
     */
    public boolean isTitleCorrect() {
        return driver.getTitle().contains("Fandango");
    }

    public void enterSearchQuery(String query) {
        WebElement searchInput = firstVisible(searchInputLocators)
                .orElseThrow(() -> new IllegalStateException("Search input was not found on the home page."));

        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.click();
        searchInput.clear();
        searchInput.sendKeys(query);

        if (!query.equals(searchInput.getAttribute("value"))) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", searchInput, query);
        }
    }

    public SearchPage submitSearch() {
        Optional<WebElement> submitButton = firstClickable(searchButtonLocators);
        if (submitButton.isPresent()) {
            submitButton.get().click();
        } else {
            WebElement searchInput = firstVisible(searchInputLocators)
                    .orElseThrow(() -> new IllegalStateException("Search input was not found on the home page."));
            searchInput.sendKeys(Keys.ENTER);
        }
        return new SearchPage(driver);
    }

    /**
     * Clears the search input and immediately submits (empty-string search).
     */
    public SearchPage submitEmptySearch() {
        WebElement searchInput = firstVisible(searchInputLocators)
                .orElseThrow(() -> new IllegalStateException("Search input was not found on the home page."));
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.click();
        searchInput.clear();
        searchInput.sendKeys(Keys.ENTER);
        return new SearchPage(driver);
    }

    public boolean isSearchBarDisplayed() {
        return isAnyElementVisible(searchInputLocators);
    }

    public boolean isTrendingMoviesSectionDisplayed() {
        return isAnyElementVisible(trendingSectionLocators);
    }

    public boolean isSignInButtonVisible() {
        return isAnyElementVisible(signInLocators);
    }

    public boolean dismissLocationPopup(String zipCode) {
        Optional<WebElement> locationInput = firstVisible(locationInputLocators);
        if (locationInput.isEmpty()) {
            return true;
        }

        WebElement input = locationInput.get();
        String placeholder = Optional.ofNullable(input.getAttribute("placeholder")).orElse("").toLowerCase();
        if (placeholder.contains("movie")) {
            return true;
        }

        input.click();
        input.clear();
        input.sendKeys(zipCode);

        Optional<WebElement> submitButton = firstClickable(locationSubmitLocators);
        if (submitButton.isPresent()) {
            submitButton.get().click();
        } else {
            input.sendKeys(Keys.ENTER);
        }

        return true;
    }

    /**
     * Returns true when the location input is visible, meaning a
     * location pop-up (or location field) is present on the page.
     */
    public boolean isLocationPopupPresent() {
        return firstVisible(locationInputLocators)
                .map(el -> {
                    String placeholder = Optional.ofNullable(el.getAttribute("placeholder"))
                            .orElse("").toLowerCase();
                    return !placeholder.contains("movie");
                })
                .orElse(false);
    }

    private Optional<WebElement> firstVisible(By... locators) {
        return Arrays.stream(locators)
                .flatMap(locator -> driver.findElements(locator).stream())
                .filter(this::isDisplayed)
                .findFirst();
    }

    private Optional<WebElement> firstClickable(By... locators) {
        for (By locator : locators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    return Optional.of(wait.until(ExpectedConditions.elementToBeClickable(element)));
                } catch (Exception ignored) {
                    // Keep trying fallback locators.
                }
            }
        }
        return Optional.empty();
    }

    private boolean isAnyElementVisible(By... locators) {
        return firstVisible(locators).isPresent();
    }

    private boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception ignored) {
            return false;
        }
    }
}
