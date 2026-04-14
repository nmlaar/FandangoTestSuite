package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;

public class HomeTests extends BaseTest {

    @Test
    public void verifyHomePageLoads() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        Assert.assertTrue(homePage.isLoaded(), "The Fandango home page should load successfully.");
    }

    @Test
    public void verifySearchBarExists() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        Assert.assertTrue(homePage.isSearchBarDisplayed(), "The global search bar should be visible.");
    }

    @Test
    public void verifyLocationPopupHandling() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        Thread.sleep(1500);
        boolean locationHandled = homePage.dismissLocationPopup("33101");
        Thread.sleep(1000);
        scrollDown(300);
        Thread.sleep(500);
        Assert.assertTrue(locationHandled,
                "Location prompts should be handled safely when they appear.");
    }

    @Test
    public void verifyTrendingMoviesDisplayed() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        Assert.assertTrue(homePage.isTrendingMoviesSectionDisplayed(),
                "The home page should show a trending or in-theaters section.");
    }

    @Test
    public void verifySignInButtonVisible() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        Thread.sleep(1500);
        scrollDown(300);
        Thread.sleep(500);
        Assert.assertTrue(homePage.isSignInButtonVisible(), "The sign-in entry point should be visible.");
    }
}