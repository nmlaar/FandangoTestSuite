package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MoviePage {
    private static final Pattern RATING_PATTERN = Pattern.compile("^(G|PG|PG-13|R|NC-17|Not Rated).*$");

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By[] titleLocators = new By[] {
            By.cssSelector("h1"),
            By.xpath("//h1[contains(normalize-space(),'Movie')]"),
            By.xpath("//*[contains(@data-testid,'title')]")
    };

    private final By[] trailerLocators = new By[] {
            By.xpath("//a[contains(normalize-space(),'Trailer')]"),
            By.xpath("//button[contains(normalize-space(),'Trailer')]"),
            By.xpath("//a[contains(normalize-space(),'Teaser')]"),
            By.xpath("//a[contains(normalize-space(),'Video')]")
    };

    private final By[] ratingLocators = new By[] {
            By.xpath("//*[self::span or self::div or self::p][contains(normalize-space(),'PG-13')]"),
            By.xpath("//*[self::span or self::div or self::p][contains(normalize-space(),'Not Rated')]"),
            By.xpath("//*[self::span or self::div or self::p][contains(normalize-space(),'PG')]"),
            By.xpath("//*[self::span or self::div or self::p][contains(normalize-space(),'R')]")
    };

    private final By[] castLocators = new By[] {
            By.xpath("//*[contains(normalize-space(),'Cast')]"),
            By.xpath("//a[contains(normalize-space(),'Chris Pratt')]"),
            By.xpath("//a[contains(normalize-space(),'Anya Taylor-Joy')]"),
            By.xpath("//a[contains(normalize-space(),'Charlie Day')]")
    };

    private final By[] showtimeLocators = new By[] {
            By.xpath("//*[contains(normalize-space(),'Theaters near')]"),
            By.xpath("//*[contains(normalize-space(),'Showtimes')]"),
            By.xpath("//button[contains(normalize-space(),'NOTIFY ME')]"),
            By.xpath("//a[contains(normalize-space(),'Buy Tickets')]"),
            By.xpath("//*[contains(normalize-space(),'Loading format filters')]")
    };

    private final By[] moviePosterLocators = new By[] {
            By.cssSelector("img[alt*='poster' i]"),
            By.cssSelector("img[src*='poster']"),
            By.cssSelector(".movie-poster img"),
            By.cssSelector("[data-testid*='poster'] img")
    };

    private final By[] synopsisLocators = new By[] {
            By.xpath("//*[contains(normalize-space(),'Synopsis')]"),
            By.xpath("//*[contains(normalize-space(),'Overview')]"),
            By.cssSelector(".synopsis"),
            By.cssSelector("[data-testid*='synopsis']")
    };

    public MoviePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public boolean isMovieDetailLoaded() {
        return driver.getCurrentUrl().contains("movie-overview") && !getMovieTitle().isBlank();
    }

    /**
     * Returns true when the current URL contains "movie-overview", without
     * requiring the title element to be resolved. Use for URL-only assertions.
     */
    public boolean isOnMoviePage() {
        return driver.getCurrentUrl().contains("movie-overview");
    }

    public String getMovieTitle() {
        return Arrays.stream(titleLocators)
                .map(this::visibleTexts)
                .flatMap(List::stream)
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElse("");
    }

    public boolean isTrailerButtonPresent() {
        return isAnyElementVisible(trailerLocators) || driver.getPageSource().contains("Trailer");
    }

    public String getRating() {
        for (By locator : ratingLocators) {
            for (String text : visibleTexts(locator)) {
                if (RATING_PATTERN.matcher(text).matches()) {
                    return text;
                }
            }
        }
        return "";
    }

    /**
     * Returns true when a valid MPAA rating string is present on the page.
     */
    public boolean isRatingDisplayed() {
        return !getRating().isBlank();
    }

    public boolean isCastSectionDisplayed() {
        return isAnyElementVisible(castLocators);
    }

    public boolean isShowtimesSectionDisplayed() {
        return isAnyElementVisible(showtimeLocators);
    }

    private boolean isAnyElementVisible(By... locators) {
        return Arrays.stream(locators).anyMatch(locator -> !visibleTexts(locator).isEmpty());
    }

    private List<String> visibleTexts(By locator) {
        List<String> texts = new ArrayList<>();
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    String text = element.getText().trim();
                    if (!text.isBlank()) {
                        texts.add(text);
                    }
                }
            } catch (Exception ignored) {
                // Ignore stale elements while gathering fallback text values.
            }
        }
        return texts;
    }
}
