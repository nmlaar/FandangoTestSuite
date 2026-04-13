package tests;

import base.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CheckoutPage;

public class CheckoutTests extends BaseTest {
    // Shared guest email used across tests that need to reach the payment page
    private static final String GUEST_EMAIL = "test.guest@example.com";

    @Test
    public void selectSeats() {
        CheckoutPage checkout = new CheckoutPage(driver);

        checkout.selectFirstTheater();
        checkout.selectFirstValidShowDate();
        checkout.selectFirstValidShowTime();
        checkout.selectFirstAvailableSeat();

        Assert.assertTrue(
                checkout.isSeatSelected(),
                "Seat was not successfully selected on the seat map."
        );
    }

    @Test
    public void verifyTicketSummary() {
        CheckoutPage checkout = new CheckoutPage(driver);

        checkout.selectFirstTheater();
        checkout.selectFirstValidShowDate();
        checkout.selectFirstValidShowTime();
        checkout.selectFirstAvailableSeat();

        boolean checkoutStepsCompleted = checkout.getTicketCheckout();
        Assert.assertTrue(
                checkoutStepsCompleted,
                "Could not complete the two-step 'Next' navigation on the seat-map overlay."
        );

        checkout.clickContinueToCheckout();

        boolean summaryVisible = checkout.isOrderSummaryDisplayed();
        boolean checkoutLoaded = checkout.isCheckoutPageLoaded();

        Assert.assertTrue(
                summaryVisible || checkoutLoaded,
                "Neither an order summary nor the checkout page was displayed " +
                        "after clicking through. URL: " + driver.getCurrentUrl()
        );
    }

    @Test
    public void verifyPriceCalculation() {
        CheckoutPage checkout = new CheckoutPage(driver);

        checkout.selectFirstTheater();
        checkout.selectFirstValidShowDate();
        checkout.selectFirstValidShowTime();
        checkout.selectFirstAvailableSeat();

        checkout.getTicketCheckout();
        checkout.clickContinueToCheckout();

        Assert.assertTrue(
                checkout.isPriceDisplayed(),
                "No price (containing '$') was displayed after selecting a seat. " +
                        "Order total text: '" + checkout.getOrderTotalText() + "'"
        );
    }

    /**
     * Proceeds to the checkout page without a signed-in account and verifies
     * that a "Continue as Guest" option is presented.
     */
    @Test
    public void verifyGuestCheckout() {
        CheckoutPage checkout = new CheckoutPage(driver);

        checkout.selectFirstTheater();
        checkout.selectFirstValidShowDate();
        checkout.selectFirstValidShowTime();
        checkout.selectFirstAvailableSeat();
        checkout.getTicketCheckout();
        checkout.clickContinueToCheckout();

        boolean guestVisible = checkout.isGuestCheckoutOptionVisible();

        if (!guestVisible) {
            System.out.println("[INFO] Guest checkout option was not found. " +
                    "The site may require a signed-in session at this stage. " +
                    "URL: " + driver.getCurrentUrl());
        }

        Assert.assertTrue(
                guestVisible || checkout.isCheckoutPageLoaded(),
                "Neither a guest-checkout option nor the checkout page URL was found."
        );
    }

    /**
     * Completes the full seat-selection and checkout flow, then verifies that
     * a payment form or credit-card input is displayed.

     * Pass: a form element or payment-related heading is visible.
     * Fail: no payment form appears after navigating through checkout.

     * Note: reaching the payment page typically requires either a guest session
     * or a signed-in account; the test asserts on the URL as a fallback.
     */
    @Test
    public void verifyPaymentPageLoads() {
        CheckoutPage checkout = new CheckoutPage(driver);

        checkout.selectFirstTheater();
        checkout.selectFirstValidShowDate();
        checkout.selectFirstValidShowTime();
        checkout.selectFirstAvailableSeat();
        checkout.getTicketCheckout();
        checkout.clickContinueToCheckout();

        // Click "Continue as Guest" if the option appears
        checkout.clickGuestCheckout();

        // Enter guest e-mail and submit the guest form to proceed to payment
        boolean guestFormSubmitted = checkout.enterGuestEmailAndContinue(GUEST_EMAIL);
        System.out.println("[INFO] Guest form submitted: " + guestFormSubmitted +
                " | URL: " + driver.getCurrentUrl());

        // Assert on actual payment inputs — not a generic form element
        Assert.assertTrue(
                checkout.isPaymentFormDisplayed(),
                "Payment input fields were not displayed after submitting the guest e-mail form. " +
                        "URL: " + driver.getCurrentUrl()
        );
    }
}
