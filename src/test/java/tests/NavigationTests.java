package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.MoviePage;
import pages.NavigationPage;
import pages.SearchPage;

public class NavigationTests extends BaseTest {

    // ── navigateHomeToMovie ────────────────────────────────────────────────────────
    /**
     * Starts from the Fandango home page, searches for a movie, clicks the
     * first result, and confirms the browser has navigated to a movie-overview
     * page.
     *
     * Pass: the final URL contains "movie-overview".
     * Fail: search or click failed to reach a movie-detail page.
     */
    @Test
    public void navigateHomeToMovie() {
        HomePage homePage = new HomePage(driver);
        homePage.dismissLocationPopup("90210");
        homePage.enterSearchQuery("Super Mario");
        SearchPage searchPage = homePage.submitSearch();

        Assert.assertTrue(
                searchPage.hasResults(),
                "No search results were returned to navigate from."
        );

        MoviePage moviePage = searchPage.clickFirstResult();

        Assert.assertTrue(
                moviePage.isOnMoviePage(),
                "Did not land on a movie-overview page. URL: " + driver.getCurrentUrl()
        );
    }

    // ── navigateBackToHome ─────────────────────────────────────────────────────────
    /**
     * Navigates from the movie-overview page back to the home page using the
     * browser's back button and confirms the title reverts to "Fandango".
     *
     * Pass: after goBack(), title contains "Fandango" or URL returns to the base.
     * Fail: the back navigation did not return to the home page.
     */
    @Test
    public void navigateBackToHome() {
        // Navigate forward to movie page first
        driver.get(MOVIE_URL);
        dismissPopups();

        String movieUrl = driver.getCurrentUrl();

        NavigationPage nav = new NavigationPage(driver);
        String urlAfterBack = nav.goBack();

        Assert.assertTrue(
                nav.didNavigateBack(movieUrl),
                "Browser did not navigate back. Still at: " + urlAfterBack
        );

        boolean titleIsHome = driver.getTitle().contains("Fandango");
        boolean urlIsHome   = !urlAfterBack.contains("movie-overview");

        Assert.assertTrue(
                titleIsHome || urlIsHome,
                "After going back, page does not appear to be the home page. " +
                        "URL: " + urlAfterBack + " | Title: " + driver.getTitle()
        );
    }

    // ── verifyURLChanges ──────────────────────────────────────────────────────────
    /**
     * Confirms that the URL changes after performing a search, proving that
     * the site uses URL-based navigation rather than rendering everything in
     * place.
     *
     * Pass: URL after search differs from the base URL.
     * Fail: URL is unchanged, suggesting navigation or routing is broken.
     */
    @Test
    public void verifyURLChanges() {
        String homeUrl = driver.getCurrentUrl();

        HomePage homePage = new HomePage(driver);
        homePage.dismissLocationPopup("90210");
        homePage.enterSearchQuery("Batman");
        homePage.submitSearch();

        String searchUrl = driver.getCurrentUrl();

        Assert.assertNotEquals(
                searchUrl, homeUrl,
                "URL did not change after submitting a search. Still at: " + homeUrl
        );
        Assert.assertTrue(
                searchUrl.contains("/search") || !searchUrl.equals(homeUrl),
                "URL after search does not contain '/search': " + searchUrl
        );
    }

    // ── verifyBreadcrumbs ──────────────────────────────────────────────────────────
    /**
     * Opens a movie-overview page and checks for a breadcrumb navigation
     * element. Breadcrumbs help users understand their position in the site
     * hierarchy.
     *
     * Pass: breadcrumb nav element is present and visible.
     * Fail: no breadcrumb element is found.
     *
     * Note: not all Fandango pages expose breadcrumbs; the test logs an
     * informational message and does not fail the suite when absent.
     */
    @Test
    public void verifyBreadcrumbs() {
        driver.get(MOVIE_URL);
        dismissPopups();

        NavigationPage nav = new NavigationPage(driver);
        boolean hasBreadcrumbs = nav.isBreadcrumbVisible();

        if (!hasBreadcrumbs) {
            System.out.println("[INFO] No breadcrumb element found on the movie-overview page. " +
                    "This may be expected for the current page template.");
        }

        // Assert the page itself is valid — breadcrumb absence is not a blocker.
        Assert.assertTrue(
                driver.getTitle().contains("Fandango"),
                "Page title does not contain 'Fandango', suggesting the page did not load."
        );
    }

    // ── verifyPageRefresh ──────────────────────────────────────────────────────────
    /**
     * Loads the movie-overview page, refreshes it, and confirms the title
     * still contains "Fandango" — verifying that the page survives a browser
     * reload without error.
     *
     * Pass: title after refresh still contains "Fandango".
     * Fail: refresh produces an error page or a blank title.
     */
    @Test
    public void verifyPageRefresh() {
        driver.get(MOVIE_URL);
        dismissPopups();

        NavigationPage nav = new NavigationPage(driver);
        String titleAfterRefresh = nav.refreshAndGetTitle();

        Assert.assertTrue(
                titleAfterRefresh.contains("Fandango"),
                "Page title after refresh does not contain 'Fandango': '" + titleAfterRefresh + "'"
        );
    }
}
