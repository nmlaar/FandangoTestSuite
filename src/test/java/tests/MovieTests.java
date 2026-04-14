package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.MoviePage;

public class MovieTests extends BaseTest {

    @Test
    public void verifyMovieDetailsLoad() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        MoviePage moviePage = new MoviePage(driver);

        Assert.assertTrue(moviePage.isMovieDetailLoaded(), "The movie overview page should load successfully.");
        Assert.assertTrue(moviePage.getMovieTitle().toLowerCase().contains("mario"),
                "The page title should identify the selected movie.");
    }

    @Test
    public void verifyTrailerButton() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        MoviePage moviePage = new MoviePage(driver);

        Assert.assertTrue(moviePage.isTrailerButtonPresent(), "The movie page should expose a trailer entry point.");
    }

    @Test
    public void verifyRatingDisplayed() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        MoviePage moviePage = new MoviePage(driver);

        Assert.assertFalse(moviePage.getRating().isBlank(), "A rating badge or rating label should be displayed.");
    }

    @Test
    public void verifyCastSection() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(500);
        Thread.sleep(500);
        MoviePage moviePage = new MoviePage(driver);

        Assert.assertTrue(moviePage.isCastSectionDisplayed(),
                "The cast or talent section should be visible on the movie page.");
    }

    @Test
    public void verifyShowtimesAvailable() throws InterruptedException {
        driver.get(MOVIE_URL);
        Thread.sleep(1500);
        scrollDown(500);
        Thread.sleep(500);
        MoviePage moviePage = new MoviePage(driver);

        Assert.assertTrue(moviePage.isShowtimesSectionDisplayed(),
                "The movie page should expose showtimes, theaters, or ticketing controls.");
    }
}