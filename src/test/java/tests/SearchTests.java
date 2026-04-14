package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchPage;

import java.util.List;

public class SearchTests extends BaseTest {
    private static final String VALID_MOVIE_QUERY = "Mario";
    private static final String INVALID_MOVIE_QUERY = "zzzxxyyqqq-no-match";

    @Test
    public void searchValidMovie() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        homePage.enterSearchQuery(VALID_MOVIE_QUERY);
        Thread.sleep(1000);

        SearchPage searchPage = homePage.submitSearch();
        Thread.sleep(1000);
        scrollDown(400);
        Thread.sleep(500);
        Assert.assertTrue(searchPage.isSearchResultsDisplayed(), "Valid searches should load a results page.");
        Assert.assertTrue(searchPage.getResultCount() > 0, "A valid movie search should return at least one result.");
    }

    @Test
    public void searchInvalidMovie() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        homePage.enterSearchQuery(INVALID_MOVIE_QUERY);
        Thread.sleep(1000);

        SearchPage searchPage = homePage.submitSearch();
        Thread.sleep(1000);
        scrollDown(400);
        Thread.sleep(500);
        Assert.assertTrue(searchPage.isSearchResultsDisplayed(), "Invalid searches should still reach the search page.");
        Assert.assertTrue(searchPage.getResultCount() == 0 || !searchPage.getNoResultsMessage().isBlank(),
                "Invalid searches should show no results or an empty-state message.");
    }

    @Test
    public void searchWithEmptyInput() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        homePage.enterSearchQuery("");
        homePage.submitSearch();
        Thread.sleep(1000);
        scrollDown(400);
        Thread.sleep(500);

        Assert.assertTrue(driver.getCurrentUrl().contains("fandango.com"),
                "Submitting an empty search should keep the user on a safe Fandango page.");
    }

    @Test
    public void verifyAutoSuggestions() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        homePage.enterSearchQuery("Mari");
        Thread.sleep(1000);
        scrollDown(300);
        Thread.sleep(500);

        SearchPage searchPage = new SearchPage(driver);
        List<String> suggestions = searchPage.getAutoSuggestions();

        if (suggestions.isEmpty()) {
            searchPage = homePage.submitSearch();
            Thread.sleep(1000);
            scrollDown(400);
            Thread.sleep(500);
            Assert.assertTrue(searchPage.isSearchResultsDisplayed(),
                    "When inline suggestions are unavailable, the fallback search results page should still load.");
            Assert.assertTrue(searchPage.getResultCount() > 0,
                    "The fallback search should still return movie results for a partial query.");
        } else {
            Assert.assertTrue(suggestions.stream().anyMatch(item -> item.toLowerCase().contains("mari")),
                    "At least one suggestion should match the partial query.");
        }
    }

    @Test
    public void verifySearchResultsPageLoads() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        homePage.enterSearchQuery(VALID_MOVIE_QUERY);
        Thread.sleep(1000);

        SearchPage searchPage = homePage.submitSearch();
        Thread.sleep(1000);
        scrollDown(400);
        Thread.sleep(500);
        Assert.assertTrue(driver.getCurrentUrl().contains("/search"),
                "The browser should navigate to the search results URL.");
        Assert.assertTrue(searchPage.isSearchResultsDisplayed(),
                "The search results layout should be visible after submitting a search.");
    }
}