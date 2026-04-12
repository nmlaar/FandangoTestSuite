package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SearchPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By[] resultSectionLocators = new By[] {
            By.xpath("//*[contains(normalize-space(),'Search results for')]"),
            By.xpath("//a[contains(@href,'/movie-overview')]"),
            By.xpath("//button[contains(normalize-space(),'Movies')]")
    };

    private final By movieResultLinks = By.xpath("//a[contains(@href,'/movie-overview')]");

    private final By[] noResultLocators = new By[] {
            By.xpath("//*[contains(normalize-space(),'No results')]"),
            By.xpath("//*[contains(normalize-space(),'No matches')]"),
            By.xpath("//*[contains(normalize-space(),'did not match')]")
    };

    private final By[] suggestionLocators = new By[] {
            By.xpath("//*[@role='listbox']//*[self::li or self::a or self::button]"),
            By.xpath("//*[contains(@class,'autocomplete') or contains(@class,'typeahead') or contains(@class,'suggest')]//*[self::li or self::a or self::button]")
    };

    private final By[] searchInputLocators = new By[] {
            By.cssSelector("input[placeholder*='movie']"),
            By.xpath("//input[contains(@placeholder,'movie')]"),
            By.xpath("//input[contains(@name,'search')]")
    };

    public SearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public boolean isSearchResultsDisplayed() {
        return driver.getCurrentUrl().contains("/search") && isAnyElementVisible(resultSectionLocators);
    }

    /**
     * Returns true when the URL contains "/search", regardless of whether
     * any results are showing. Useful for verifying the page transitioned.
     */
    public boolean isOnSearchPage() {
        return driver.getCurrentUrl().contains("/search");
    }

    public int getResultCount() {
        waitForResultsContainer();
        return visibleElements(movieResultLinks).size();
    }

    /**
     * Returns true when at least one movie result link is visible.
     */
    public boolean hasResults() {
        waitForResultsContainer();
        return !visibleElements(movieResultLinks).isEmpty();
    }

    public List<String> getAutoSuggestions() {
        Set<String> suggestions = new LinkedHashSet<>();
        for (By locator : suggestionLocators) {
            for (WebElement element : driver.findElements(locator)) {
                String text = element.getText().trim();
                if (isDisplayed(element) && !text.isBlank()) {
                    suggestions.add(text);
                }
            }
        }
        return new ArrayList<>(suggestions);
    }

    /**
     * Returns true when the autocomplete dropdown contains at least one entry.
     */
    public boolean hasAutoSuggestions() {
        return !getAutoSuggestions().isEmpty();
    }

    public MoviePage clickFirstResult() {
        waitForResultsContainer();
        WebElement firstResult = visibleElements(movieResultLinks).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No movie results were available to click."));
        firstResult.click();
        return new MoviePage(driver);
    }

    public String getNoResultsMessage() {
        return Arrays.stream(noResultLocators)
                .map(this::visibleElements)
                .flatMap(List::stream)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElse("");
    }

    /**
     * Returns true when a "no results" message element is visible.
     */
    public boolean isNoResultsMessageDisplayed() {
        return !getNoResultsMessage().isBlank();
    }

    /**
     * Returns true when the search input field is present on the search
     * results page (Fandango keeps it visible for follow-up searches).
     */
    public boolean isSearchInputDisplayed() {
        for (By locator : searchInputLocators) {
            for (WebElement element : driver.findElements(locator)) {
                if (isDisplayed(element)) return true;
            }
        }
        return false;
    }

    private void waitForResultsContainer() {
        for (By locator : resultSectionLocators) {
            try {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("/search"),
                        ExpectedConditions.visibilityOfElementLocated(locator)
                ));
                return;
            } catch (Exception ignored) {
                // Try the next fallback locator.
            }
        }
    }

    private boolean isAnyElementVisible(By... locators) {
        return Arrays.stream(locators).anyMatch(locator -> !visibleElements(locator).isEmpty());
    }

    private List<WebElement> visibleElements(By locator) {
        List<WebElement> visible = new ArrayList<>();
        for (WebElement element : driver.findElements(locator)) {
            if (isDisplayed(element)) {
                visible.add(element);
            }
        }
        return visible;
    }

    private boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception ignored) {
            return false;
        }
    }
}
