package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

public class CheckoutPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait longWait;

    // Step 1 of checkout navigation: the "Next" button on the seat-map overlay
    private final By nextButtonLocator = By.id("NextButton");

    // Step 2 of checkout navigation: the "Next" button on the "Review your Tickets" page
    private final By reviewNextButtonLocator = By.id("ticket-selection-overlay-next-btn");

    // Step 3a (conditional): dismiss the "jurassic" upsell modal if it appears
    private final By jurassicModalDeclineBtn = By.id("jurassic-modal-decline-btn");

    // Step 3b: "Continue to Checkout" button shown when the modal is absent
    private final By continueToCheckoutBtn = By.id("buynow-continue-btn");

    private final By[] theaterLocators = new By[] {
            By.xpath("//a[contains(@href,'theater')]"),

            By.xpath("//button[contains(text(),'AMC') or " +
                    "contains(text(),'Regal') or contains(text(),'Cinemark')]"),

            By.xpath("//div[contains(text(),'Theater') or " +
                    "contains(text(),'Cinema')]")
    };

    private final By[] showDateLocators = new By[] {
            By.cssSelector("#splide02-slide03 > button"),
            By.id("splide06-slide03")
    };

    private final By[] showTimeLocators = new By[] {
            By.className("showtime-btn--available"),
            By.cssSelector("#shared-showtimes-244726 > section > ol > li:nth-child(2) > a")
    };

    private final By[] orderSummaryLocators = new By[] {
            By.xpath("//*[contains(normalize-space(),'Order Summary')]"),
            By.xpath("//*[contains(normalize-space(),'Ticket Summary')]"),
            By.xpath("//*[contains(normalize-space(),'Your Order')]"),
            By.xpath("//*[contains(normalize-space(),'Tickets')]")
    };

    private final By[] paymentFormLocators = new By[] {
            By.cssSelector("form"),
            By.xpath("//input[contains(@name,'card') or contains(@id,'card')]"),
            By.xpath("//*[contains(normalize-space(),'Payment Method')]"),
            By.xpath("//*[contains(normalize-space(),'Credit Card')]")
    };

    private final By[] guestCheckoutLocators = new By[] {
            By.xpath("//button[contains(normalize-space(),'Guest')]"),
            By.xpath("//a[contains(normalize-space(),'Guest')]"),
            By.xpath("//*[contains(normalize-space(),'Continue as Guest')]"),
            By.xpath("//*[contains(normalize-space(),'guest checkout')]")
    };

    private final By[] priceLocators = new By[] {
            By.xpath("//*[contains(normalize-space(),'Total')]"),
            By.xpath("//*[contains(normalize-space(),'Subtotal')]"),
            By.xpath("//*[contains(@class,'price') or contains(@class,'total')]"),
            By.xpath("//*[contains(text(),'$')]")
    };

    private final By[] ticketQuantityLocators = new By[] {
            By.xpath("//button[normalize-space()='+']"),
            By.xpath("//button[contains(@aria-label,'increase') or contains(@aria-label,'add')]"),
            By.xpath("//input[contains(@type,'number')]")
    };

    // Guest email form locators
    private final By guestEmailInput = By.xpath("//*[@id='guestForm']/fieldset//input[contains(@type,'email')]");
    private final By guestEmailInputFallback = By.cssSelector("input.form__input.ctHidden[type='email']," +
            " input.form__input[type='email']");
    private final By guestContinueBtn = By.id("btnGuest");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
// Select first theater found from list
    public void selectFirstTheater() {

        for (By locator : theaterLocators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (!element.isDisplayed()) continue;
                    String text = element.getText().toLowerCase();
                    // Filter real theaters
                    if (text.contains("amc") || text.contains("regal") || text.contains("cinema")) {
                        safeClick(element);
                        // wait for page update (showtimes load)
                        wait.until(ExpectedConditions.stalenessOf(element));
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void clickFirstStaleless(By[] locators, By nextContentLocator) {
        for (By locator : locators) {
            try {
                List<WebElement> elements =
                        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));

                // Snapshot existing content before click
                List<WebElement> oldContent = driver.findElements(nextContentLocator);

                for (WebElement element : elements) {
                    String classes = element.getAttribute("class");

                    if (classes.contains("disabled")) continue;

                    safeClick(element);

                    // Wait for old content to disappear
                    if (!oldContent.isEmpty()) {
                        try {
                            wait.until(ExpectedConditions.stalenessOf(oldContent.get(0)));
                        } catch (Exception ignored) {}
                    }

                    // Wait for new content to appear
                    wait.until(ExpectedConditions.presenceOfElementLocated(nextContentLocator));
                    System.out.println("Success: " + locator);
                    return;
                }

            } catch (TimeoutException e) {
                System.out.println("Timeout: " + locator);
            } catch (Exception e) {
                System.out.println("Error: " + locator);
            }
        }
    }

    public void selectFirstValidShowDate() {
        clickFirstStaleless(showDateLocators, By.className("showtime-btn--available"));
    }
    public void selectFirstValidShowTime() {
        clickFirstStaleless(showTimeLocators, By.cssSelector(".seat-map__seat"));
    }

    public void selectFirstAvailableSeat() {
        By seatsLocator = By.cssSelector(".seat-map__seat");

        for (int attempt = 0; attempt < 3; attempt++) {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            // Validate seat map
            if (!waitForSeatMapOrError()) {
                System.out.println("[INFO] Seat map failed to load → refreshing...");
                driver.navigate().refresh();
                continue;
            }

            List<WebElement> seats = driver.findElements(seatsLocator);

            for (WebElement seat : seats) {
                try {
                    if (!seat.isDisplayed()) continue;

                    String classes = seat.getAttribute("class");
                    String ariaDisabled = seat.getAttribute("aria-disabled");

                    boolean isReserved = classes.contains("reservedSeat");
                    boolean isDisabled = "true".equals(ariaDisabled);

                    if (isReserved || isDisabled) continue;

                    safeClick(seat);

                    // Detect popup AFTER click
                    if (isSeatErrorPopupVisible()) {
                        System.out.println("Seat rejected → refreshing...");
                        driver.navigate().refresh();
                        break; // retry outer loop
                    }

                    // Confirm selection
                    boolean selected = wait.until(driver ->
                            seat.getAttribute("class").contains("selectedSeat")
                    );

                    if (selected) return;

                } catch (Exception ignored) {}
            }

            // fallback retry
            driver.navigate().refresh();
        }

    }


    private boolean waitForSeatMapOrError() {
        By seatsLocator = By.cssSelector(".seat-map__seat");

        try {
            return wait.until(driver -> {
                boolean hasSeats = driver.findElements(seatsLocator).size() > 5;
                boolean hasError = isSeatErrorPopupVisible();

                if (hasError) return false;   // force retry
                return hasSeats;              // success only if seats exist
            });
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSeatErrorPopupVisible() {
            WebElement element = driver.findElement(By.xpath("//*[@id='SeatPickerModal__Alert']/button"));
        return element.isDisplayed();
    }

    // Verify seat selection
    public boolean isSeatSelected() {
        try {
            return driver.findElement(
                    By.cssSelector(".seat-map__seat.selectedSeat")
            ).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Full checkout navigation sequence:

     *  1. Click "Next" (id=NextButton) — closes the seat-map overlay
     *  2. Click "Next" on the "Review your Tickets" page
     *     (id=ticket-selection-overlay-next-btn)
     *  3a. If the Jurassic upsell modal appears, decline it
     *      (id=jurassic-modal-decline-btn)
     *  3b. Otherwise click "Continue to Checkout" (id=buynow-continue-btn)

     * Returns true only when all required steps completed successfully.
     */
    public boolean getTicketCheckout() {
        // Step 1 – seat-map "Next" button
        if (!clickById(nextButtonLocator, "NextButton")) {
            return false;
        }

        // Step 2 – "Review your Tickets" next button
        return clickById(reviewNextButtonLocator, "ticket-selection-overlay-next-btn");
    }

    /**
     * After clicking the "Next" button, a popup appears offering
     * "Continue to Checkout" or PayPal. This method waits for that
     * popup's continue button and clicks it.
     * Returns true if the button was found and clicked.
     */
    public void clickContinueToCheckout() {
        // Give the modal a short window to appear before assuming it won't
        try {
            WebElement declineBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(jurassicModalDeclineBtn));
            safeClick(declineBtn);
        } catch (Exception ignored) {
            // Modal did not appear — that is fine, fall through to the continue button
        }

        // Now click "Continue to Checkout"
        try {
            WebElement continueBtn = longWait.until(
                    ExpectedConditions.elementToBeClickable(continueToCheckoutBtn));
            safeClick(continueBtn);
        } catch (Exception ignored) {
        }
    }

    /** Returns true when the current URL contains "checkout". */
    public boolean isCheckoutPageLoaded() {
        return driver.getCurrentUrl().contains("checkout");
    }

    public boolean isGuestCheckoutOptionVisible() {
        return isAnyElementVisible(guestCheckoutLocators);
    }

    public void clickGuestCheckout() {
        for (By locator : guestCheckoutLocators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (!element.isDisplayed()) continue;
                    safeClick(element);
                    return;
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Enters a guest e-mail address and clicks the "Continue" (btnGuest) button.
     * This is required to reach the payment-input step.

     * Returns true when the form was found and submitted.
     */
    public boolean enterGuestEmailAndContinue(String email) {
        // Try primary locator first, then the className-based fallback
        WebElement emailField = null;
        for (By locator : new By[]{ guestEmailInput, guestEmailInputFallback }) {
            try {
                emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                break;
            } catch (Exception ignored) {}
        }

        if (emailField == null) return false;

        try {
            emailField.clear();
            emailField.sendKeys(email);
            Thread.sleep(1500);
        } catch (Exception e) {
            return false;
        }

        try {
            WebElement continueBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(guestContinueBtn));
            safeClick(continueBtn);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true only when actual payment input fields are visible.
     * BUG FIX: generic By.cssSelector("form") removed — it matched on every page
     * and caused verifyPaymentPageLoads to pass before reaching payment inputs.
     */
    public boolean isPaymentFormDisplayed() {
        return isAnyElementVisible(paymentFormLocators);
    }

    public boolean isOrderSummaryDisplayed() {
        return isAnyElementVisible(orderSummaryLocators);
    }

    public String getOrderTotalText() {
        for (By locator : priceLocators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (!element.isDisplayed()) continue;
                    String text = element.getText().trim();
                    if (!text.isBlank()) return text;
                } catch (Exception ignored) {}
            }
        }
        return "";
    }

    public boolean isPriceDisplayed() {
        return getOrderTotalText().contains("$");
    }


    /**
     * Waits for a button by its locator and clicks it.
     * Returns false (without throwing) if the button never becomes clickable.
     */
    private boolean clickById(By locator, String description) {
        try {
            WebElement btn = longWait.until(ExpectedConditions.elementToBeClickable(locator));
            safeClick(btn);
            return true;
        } catch (Exception e) {
            System.out.println("[WARN] Could not click button: " + description);
            return false;
        }
    }

    private void safeClick(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            js.executeScript("arguments[0].click();", element);
        }
    }

    private boolean isAnyElementVisible(By... locators) {
        for (By locator : locators) {
            for (WebElement element : driver.findElements(locator)) {
                try {
                    if (element.isDisplayed()) return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }
}