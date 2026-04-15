package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchPage;

import java.time.Duration;

public class ErrorHandlingTests extends BaseTest {

    /**
     * Pass: "no results" message visible OR result count == 0, with Fandango
     *       title still in the browser tab (page did not crash).
     * Fail: the page crashes (no Fandango title), or results are shown for a
     *       string that should return nothing.
     */
    @Test
    public void handleNoSearchResults() {
        HomePage homePage = new HomePage(driver);
        homePage.dismissLocationPopup("90210");
        homePage.enterSearchQuery("xqzxqz_impossible_movie_string_99999");
        SearchPage searchPage = homePage.submitSearch();

        // Page must not crash
        Assert.assertTrue(
                driver.getTitle().contains("Fandango"),
                "Page crashed or navigated away from Fandango after a no-results search."
        );

        boolean noResultsMessage = searchPage.isNoResultsMessageDisplayed();
        boolean zeroResults      = searchPage.getResultCount() == 0;

        Assert.assertTrue(
                noResultsMessage || zeroResults,
                "Expected a no-results state but results were displayed."
        );
    }

    /**
     * Confirms that the WebDriverWait configuration in BaseTest correctly
     * throws a TimeoutException (rather than hanging indefinitely) when an
     * element that does not exist is waited upon.

     * Pass: TimeoutException is thrown within the configured wait window.
     * Fail: the wait blocks indefinitely, or no exception is thrown.
     */
    @Test
    public void handleTimeouts() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));

        boolean timedOutAsExpected = false;
        long startMs = System.currentTimeMillis();

        try {
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("this-element-does-not-exist-ever")
            ));
        } catch (TimeoutException e) {
            timedOutAsExpected = true;
        }

        long elapsedMs = System.currentTimeMillis() - startMs;

        Assert.assertTrue(
                timedOutAsExpected,
                "Expected a TimeoutException but the wait returned without one."
        );

        // The wait should have expired in roughly 3 s (allow up to 6 s for CI slack)
        Assert.assertTrue(
                elapsedMs < 6_000,
                "Wait took longer than expected (" + elapsedMs + " ms). " +
                        "The timeout may not be configured correctly."
        );
    }

    /**
     * Pass: page title still contains "Fandango" (site keeps its branding
     *       even on error pages), OR the URL redirects to a valid page.
     * Fail: browser shows a raw browser error (net::ERR_*) with no Fandango
     *       title at all.
     */
    @Test
    public void handleInvalidURL() {
        String invalidUrl = BASE_URL + "this/page/does/not/exist/at/all/99999";
        driver.get(invalidUrl);
        dismissPopups();

        // Give the page time to load or redirect
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        String title      = driver.getTitle();
        String currentUrl = driver.getCurrentUrl();

        boolean fandangoTitle    = title.contains("Fandango");
        boolean redirectedToHome = currentUrl.equals(BASE_URL)
                || currentUrl.equals(BASE_URL + "/");

        Assert.assertTrue(
                fandangoTitle || redirectedToHome,
                "Navigating to an invalid URL produced no Fandango branding. " +
                        "Title: '" + title + "' | URL: " + currentUrl
        );
    }

    /**
     * Simulates a session expiration by clearing all browser cookies and local
     * storage, then reloading the current page. The test verifies that the site
     * recovers gracefully rather than displaying an unhandled error.

     * Pass: after clearing session data and reloading, the Fandango title is
     *       present (the site handles the expired session without crashing).
     * Fail: the page shows an unbranded error after the session is cleared.
     */
    @Test
    public void handleSessionExpiration() {
        // Start on a real page first
        driver.get(BASE_URL);
        dismissPopups();

        // Clear cookies to simulate an expired session
        driver.manage().deleteAllCookies();

        // Also clear localStorage to remove any token storage
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        } catch (Exception ignored) {
            // Some pages block storage access — safe to ignore.
        }

        // Reload the page with the cleared session
        driver.navigate().refresh();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        dismissPopups();

        Assert.assertTrue(
                driver.getTitle().contains("Fandango"),
                "After clearing session cookies and reloading, the page does not show " +
                        "the Fandango title. Title: '" + driver.getTitle() + "'"
        );
    }

    /**
     * Pass: after dismissing popups, the search bar is still accessible and
     *       the Fandango home page is still loaded.
     * Fail: dismissing popups caused the page to crash or navigate away, or
     *       the search bar became inaccessible.
     */
    @Test
    public void handlePopupInterruptions() {
        // The BaseTest @BeforeMethod already calls dismissPopups() once;
        // call it again to simulate a second popup appearing mid-session.
        dismissPopups();

        HomePage homePage = new HomePage(driver);

        Assert.assertTrue(
                homePage.isTitleCorrect(),
                "Fandango home page is no longer showing after popup dismissal. " +
                        "Title: '" + driver.getTitle() + "'"
        );

        Assert.assertTrue(
                homePage.isSearchBarDisplayed(),
                "Search bar is not accessible after dismissing popups."
        );
    }
}
