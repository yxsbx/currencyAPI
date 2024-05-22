package br.com.ada.currencyapi.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CurrencyConverterSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver/chrome.exe");

        driver = new ChromeDriver();
        driver.get("http://localhost:5173");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testCreateCurrency() {
        WebElement newCurrencyCode = driver.findElement(By.xpath("//input[@placeholder='Código da Moeda (e.g., USD)']"));
        WebElement newCurrencyName = driver.findElement(By.xpath("//input[@placeholder='Nome da Moeda (e.g., Dólar Americano)']"));
        WebElement addCurrencyButton = driver.findElement(By.xpath("//button[contains(text(),'Adicionar Moeda')]"));

        newCurrencyCode.sendKeys("TEST");
        newCurrencyName.sendKeys("Test Currency");

        addCurrencyButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement currencySelect = driver.findElement(By.xpath("//select/option[contains(text(),'TEST - Test Currency')]"));
        assertNotNull(currencySelect);
    }

    @Test
    public void testConvertCurrency() {
        WebElement amountInput = driver.findElement(By.xpath("//input[@type='text']"));
        WebElement fromCurrencySelect = driver.findElement(By.xpath("//select[1]"));
        WebElement toCurrencySelect = driver.findElement(By.xpath("//select[2]"));
        WebElement convertButton = driver.findElement(By.xpath("//button[contains(text(),'Converter')]"));

        amountInput.clear();
        amountInput.sendKeys("10");

        fromCurrencySelect.sendKeys("USD - Dollars");
        toCurrencySelect.sendKeys("EUR - Euro");

        convertButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement result = driver.findElement(By.xpath("//h2[contains(text(),'equivale a')]"));
        assertNotNull(result);
    }
}