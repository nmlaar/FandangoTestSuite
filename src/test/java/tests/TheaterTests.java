package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.TheaterPage;

public class TheaterTests extends BaseTest {
    private static final String MOVIE_URL = "https://www.fandango.com/the-super-mario-galaxy-movie-2026-242307/movie-overview";
    private static final String THEATERS_URL = "https://www.fandango.com/movie-theaters";

    @Test
    public void verifyTheaterPageLoads() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        TheaterPage theaterPage = new TheaterPage(driver);

        Assert.assertTrue(theaterPage.isTheaterListDisplayed(),
                "The movie page should display nearby theaters or theater controls.");
    }

    @Test
    public void verifyTheaterSearch() throws InterruptedException {
        driver.get(THEATERS_URL);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        TheaterPage theaterPage = new TheaterPage(driver);
        theaterPage.searchByLocation("33101");
        Thread.sleep(1000);
        scrollDown(400);
        Thread.sleep(500);

        Assert.assertTrue(theaterPage.isTheaterListDisplayed() || !theaterPage.getTheaterNames().isEmpty(),
                "Searching by ZIP code should expose theater results or theater content.");
    }

    @Test
    public void verifyTheaterSelection() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        TheaterPage theaterPage = new TheaterPage(driver);
        boolean theaterSelected = theaterPage.selectFirstTheater();
        Thread.sleep(1000);
        scrollDown(300);
        Thread.sleep(500);

        Assert.assertTrue(theaterSelected,
                "A visible theater entry should be selectable from the theater list.");
    }

    @Test
    public void verifyShowtimeFiltering() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        TheaterPage theaterPage = new TheaterPage(driver);
        String selectedBefore = theaterPage.getSelectedDateLabel();
        boolean filtered = theaterPage.filterByShowtime("Wednesday");
        Thread.sleep(1000);
        scrollDown(300);
        Thread.sleep(500);

        Assert.assertTrue(filtered,
                "Selecting a different showtime day should be handled successfully.");

        String selectedAfter = theaterPage.getSelectedDateLabel();
        Assert.assertTrue(selectedAfter.contains("Wed") || !selectedAfter.equals(selectedBefore),
                "The active showtime day should update after filtering.");
    }

    @Test
    public void verifyMapOrLocationPresent() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        TheaterPage theaterPage = new TheaterPage(driver);

        Assert.assertTrue(theaterPage.isMapDisplayed() || !theaterPage.getTheaterNames().isEmpty(),
                "The theater section should show a map link or location details.");
    }
}