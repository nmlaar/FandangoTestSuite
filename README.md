# Fandango Test Suite

End-to-end UI tests for [Fandango](https://www.fandango.com/) using **Selenium WebDriver**, **TestNG**, and the **Page Object Model**. The suite exercises home page, search, movie detail, and theater flows against the live site.

## Prerequisites

- **JDK 17+** (project targets Java 17; newer JDKs are fine)
- **Google Chrome** installed
- **Maven** (optional if you run tests only from IntelliJ)
- **IntelliJ IDEA** (recommended)

Dependencies are managed in `pom.xml` (Selenium, TestNG, WebDriverManager for ChromeDriver).

## How to run tests

### IntelliJ (recommended)

1. Open this folder as a project and let Maven import finish.
2. **Full suite:** right-click `testng.xml` → **Run 'Fandango Test Suite'** (or use the green gutter icon).

   This runs all four test classes with parallel execution (`parallel="classes"`, `thread-count="2"`).

3. **Single class:** open e.g. `src/test/java/tests/HomeTests.java` → right-click the class → **Run**.

4. **Single method:** use the green gutter icon next to a `@Test` method.

Do **not** run `BaseTest.java` as the only “test”: it has no `@Test` methods. It only provides `@BeforeMethod` / `@AfterMethod` setup used by `HomeTests`, `SearchTests`, `MovieTests`, and `TheaterTests`.

### Command line

From the project root (where `pom.xml` lives):

```bash
mvn test
```

Surefire is configured to use `testng.xml`.

## Project layout

```
src/test/java/
├── base/BaseTest.java          # WebDriver setup, popups, screenshots on failure
├── pages/                      # Page objects (Home, Search, Movie, Theater, Checkout)
└── tests/                      # TestNG test classes (4 classes, 5 methods each)
testng.xml                      # Combined suite for all tests
pom.xml
```

## Failure screenshots

If a test fails, a PNG may be saved under:

`target/screenshots/`

## Automation notes (live site)

Fandango is a real production site. Tests can be affected by:

- Cookie / privacy banners and other overlays (handled in `BaseTest` where possible)
- Bot detection or rate limiting on repeated runs
- UI or URL changes (selectors may need updates)

Prefer running in a **visible** Chrome window; avoid hammering the site with many parallel or back-to-back runs.

## License

This is a sample/educational test project; ensure your use complies with Fandango’s terms of service.
