package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.UIValidationPage;

public class UIValidationTests extends BaseTest {

    // ── verifyFontsAndColors ──────────────────────────────────────────────────────
    /**
     * Reads the computed CSS font-family and background-color of the page body
     * and asserts that both are non-empty / non-default values, confirming that
     * the site's stylesheet has loaded and been applied.
     * Pass: body has a non-blank font-family AND a non-transparent background.
     * Fail: computed styles are empty or fully transparent, indicating CSS did
     *       not load correctly.
     */
    @Test
    public void verifyFontsAndColors() {
        UIValidationPage ui = new UIValidationPage(driver);

        String fontFamily = ui.getBodyFontFamily();
        Assert.assertFalse(
                fontFamily.isBlank(),
                "Body font-family was blank — stylesheet may not have loaded."
        );
        System.out.println("[INFO] Body font-family: " + fontFamily);

        Assert.assertTrue(
                ui.isBackgroundColorApplied(),
                "Body background-color was transparent or empty. " +
                        "Raw value: '" + ui.getBodyBackgroundColor() + "'"
        );
    }

    // ── verifyResponsiveLayout ────────────────────────────────────────────────────
    /**
     * Resizes the browser to a typical mobile viewport (390 × 844) and
     * confirms that the page body width does not overflow horizontally,
     * indicating the layout is responsive.
     * Pass: document.body.scrollWidth ≤ viewport width (with 20 px tolerance).
     * Fail: the page overflows horizontally at a mobile viewport size.
     */
    @Test
    public void verifyResponsiveLayout() {
        UIValidationPage ui = new UIValidationPage(driver);

        try {
            boolean responsive = ui.isLayoutResponsive(390, 844);
            Assert.assertTrue(
                    responsive,
                    "Page layout overflows horizontally at a 390 px viewport width, " +
                            "suggesting the layout is not responsive."
            );
        } finally {
            // Restoring the window so later tests are not affected.
            ui.restoreDesktopSize();
        }
    }

    // ── verifyButtonsClickable ────────────────────────────────────────────────────
    /**
     * Counts the number of visible, enabled buttons on the home page and
     * asserts there is at least one — confirming that interactive controls
     * are present and in an enabled state.
     * Pass: at least one button/submit element is visible and enabled.
     * Fail: no clickable buttons are found on the page.
     */
    @Test
    public void verifyButtonsClickable() {
        UIValidationPage ui = new UIValidationPage(driver);

        int clickableCount = ui.getClickableButtonCount();

        Assert.assertTrue(
                clickableCount > 0,
                "No clickable buttons were found on the home page."
        );
        System.out.println("[INFO] Clickable button count: " + clickableCount);
    }

    // ── verifyImagesLoad ──────────────────────────────────────────────────────────
    /**
     * Checks every {@code <img>} element on the page and counts those whose
     * {@code naturalWidth} is 0 (i.e. failed to load). The test asserts that
     * zero images have failed to load.
     * Pass: all images have loaded (no broken images found).
     * Fail: one or more images have naturalWidth == 0.
     */
    @Test
    public void verifyImagesLoad() {
        UIValidationPage ui = new UIValidationPage(driver);

        int totalImages  = ui.getTotalImageCount();
        int brokenImages = ui.getBrokenImageCount();

        System.out.println("[INFO] Total images: " + totalImages +
                " | Broken: " + brokenImages);

        Assert.assertEquals(
                brokenImages, 0,
                brokenImages + " image(s) failed to load out of " + totalImages + " total."
        );
    }

    // ── verifyNoBrokenLinks ────────────────────────────────────────────────────────
    /**
     * Scans every anchor tag on the page for obviously invalid hrefs (empty,
     * "#", or "javascript:void(0)") and asserts that none are found on visible,
     * labelled links.
     * Pass: no anchor tags with meaningful link text have an obviously
     *       broken/placeholder href.
     * Fail: one or more visible links point to "#" or equivalent.
     * Note: this test performs a DOM-only check and does not make HTTP
     * requests. It catches placeholder links that were never wired up, not
     * server-side 404s.
     */
    @Test
    public void verifyNoBrokenLinks() {
        UIValidationPage ui = new UIValidationPage(driver);

        var brokenLinks = ui.getObviouslyBrokenLinks();

        if (!brokenLinks.isEmpty()) {
            System.out.println("[INFO] Broken link candidates found:");
            brokenLinks.forEach(link -> System.out.println("  " + link));
        }

        Assert.assertTrue(
                ui.hasNoBrokenLinks(),
                "Found " + brokenLinks.size() + " obviously broken link(s): " + brokenLinks
        );
    }
}
