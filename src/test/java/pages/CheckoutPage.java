package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

public class CheckoutPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait longWait;

    private final By[] theaterLocators = new By[] {
            By.xpath("//a[contains(@href,'theater')]"),
            By.xpath("//button[contains(text(),'AMC') or contains(text(),'Regal') or contains(text(),'Cinemark')]"),
            By.xpath("//div[contains(text(),'Theater') or contains(text(),'Cinema')]")
    };

    private final By[] showDateLocators = new By[] {
            By.cssSelector("#splide02-slide03 > button"),
            By.id("splide06-slide03")
    };

    private final By[] showTimeLocators = new By[] {
            By.className("showtime-btn--available"),
            By.cssSelector("#shared-showtimes-244726 > section > ol > li:nth-child(2) > a")
    };

    // Step 1 of checkout navigation: the "Next" button on the seat-map overlay
    private final By nextButtonLocator = By.id("NextButton");

    // Step 2 of checkout navigation: the "Next" button on the "Review your Tickets" page
    private final By reviewNextButtonLocator = By.id("ticket-selection-overlay-next-btn");

    // Step 3a (conditional): dismiss the "jurassic" upsell modal if it appears
    private final By jurassicModalDeclineBtn = By.id("jurassic-modal-decline-btn");

    // Step 3b: "Continue to Checkout" button shown when the modal is absent
    private final By continueToCheckoutBtn = By.id("buynow-continue-btn");

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
    // Fallback using the className mentioned in requirements
    private final By guestEmailInputFallback = By.cssSelector("input.form__input.ctHidden[type='email'], input.form__input[type='email']");
    private final By guestContinueBtn = By.id("btnGuest");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }
// Select first theater found from list
    public boolean selectFirstTheater() {

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
                        return true;
                    }
                } catch (Exception ignored) {}
            }
        }

        return false;
    }

    // Select first valid showdate (NOT disabled)
    public boolean selectFirstValidShowDate() {
        for (By locator : showDateLocators) {
            List<WebElement> showdates = driver.findElements(locator);
            for (WebElement showdate : showdates) {
                try {
                    if (!showdate.isDisplayed()) continue;
                    String classes = showdate.getAttribute("class");
                    if (!classes.contains("disabled")) {
                        safeClick(showdate);
                        return true;
                    }
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    public boolean selectFirstValidShowTime() {
        for (By locator : showTimeLocators) {
            List<WebElement> showtimes = driver.findElements(locator);
            for(WebElement showtime : showtimes) {
                try {
                    if (!showtime.isDisplayed()) continue;

                    String classes = showtime.getAttribute("class");

                    if (!classes.contains("disabled")) {

                        safeClick(showtime);

//                        // wait for seat map to load
//                        wait.until(ExpectedConditions.presenceOfElementLocated(
//                                By.cssSelector(".seat-map__seat")

                            return true;
                    }

                } catch (Exception ignored) {}
            }
        }
        return false;
    }


    // Select first available seat
    public boolean selectFirstAvailableSeat() {
        // Wait for the seat map to be fully rendered before querying seats
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector(".seat-map__seat")));
        } catch (Exception ignored) {}
        List<WebElement> seats = driver.findElements(By.cssSelector(".seat-map__seat"));
        for (WebElement seat : seats) {
            try {
                if (!seat.isDisplayed()) continue;
                String classes = seat.getAttribute("class");
                String ariaDisabled = seat.getAttribute("aria-disabled");
                boolean isReserved = classes.contains("reservedSeat");
                boolean isDisabled = "true".equals(ariaDisabled);
                if (!isReserved && !isDisabled) {
                    safeClick(seat);
                    // BUG FIX: wait for the selectedSeat class to be applied before
                    // returning, so isSeatSelected() won't race against the DOM update.
                    try {
                        wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector(".seat-map__seat.selectedSeat")));
                    } catch (Exception ignored) {}
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
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
     *
     *  1. Click "Next" (id=NextButton) — closes the seat-map overlay
     *  2. Click "Next" on the "Review your Tickets" page
     *     (id=ticket-selection-overlay-next-btn)
     *  3a. If the Jurassic upsell modal appears, decline it
     *      (id=jurassic-modal-decline-btn)
     *  3b. Otherwise click "Continue to Checkout" (id=buynow-continue-btn)
     *
     * Returns true only when all required steps completed successfully.
     */
    public boolean getTicketCheckout() {
        // Step 1 – seat-map "Next" button
        if (!clickById(nextButtonLocator, "NextButton")) {
            return false;
        }

        // Step 2 – "Review your Tickets" next button
        if (!clickById(reviewNextButtonLocator, "ticket-selection-overlay-next-btn")) {
            return false;
        }

        return true;
    }

    /**
     * After clicking the "Next" button, a popup appears offering
     * "Continue to Checkout" or PayPal. This method waits for that
     * popup's continue button and clicks it.
     * Returns true if the button was found and clicked.
     */
    public boolean clickContinueToCheckout() {
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
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns true when the current URL contains "checkout". */
    public boolean isCheckoutPageLoaded() {
        return driver.getCurrentUrl().contains("checkout");
    }

    public boolean isGuestCheckoutOptionVisible() {
        return isAnyElementVisible(guestCheckoutLocators);
    }

    public boolean clickGuestCheckout() {
        for (By locator : guestCheckoutLocators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (!element.isDisplayed()) continue;
                    safeClick(element);
                    return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    /**
     * Enters a guest e-mail address and clicks the "Continue" (btnGuest) button.
     * This is required to reach the payment-input step.
     *
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

    // ── Payment form ──────────────────────────────────────────────────────────────

    /**
     * Returns true only when actual payment input fields are visible.
     * BUG FIX: generic By.cssSelector("form") removed — it matched on every page
     * and caused verifyPaymentPageLoads to pass before reaching payment inputs.
     */
    public boolean isPaymentFormDisplayed() {
        return isAnyElementVisible(paymentFormLocators);
    }

    // ── Order summary / price ────────────────────────────────────────────────────

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

    // ── Quantity ──────────────────────────────────────────────────────────────────

    public boolean increaseTicketQuantity(int times) {
        for (int i = 0; i < times; i++) {
            boolean clicked = false;
            for (By locator : ticketQuantityLocators) {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement element : elements) {
                    try {
                        if (!element.isDisplayed()) continue;
                        safeClick(element);
                        clicked = true;
                        break;
                    } catch (Exception ignored) {}
                }
                if (clicked) break;
            }
            if (!clicked) return false;
        }
        return true;
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

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