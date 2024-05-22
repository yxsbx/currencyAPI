package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import br.com.ada.currencyapi.repository.feign.AwesomeAPIClient;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * assertThatThrownBy(): Verifica se uma exceção específica é capturada durante a execução de um bloco de código.
 * hasMessage: Verifica uma correspondência exata da mensagem de erro.
 * hasMessageContaining: Verifica se a mensagem de erro contém uma substring específica.
 * isEqualTo: Verifica se o valor de um objeto é igual ao valor esperado.
 * isInstanceOf: Verifica se o tipo de um objeto é o tipo esperado.
 * isInstanceOfAny: Verifica se o tipo de um objeto é um dos tipos esperados.
 * isNull: Verifica se um objeto é nulo.
 * isSameAs: Verifica se um objeto é o mesmo objeto (comparação de identidade) que o objeto esperado.
 * isNotSameAs: Verifica se um objeto não é o mesmo objeto (comparação de identidade) que o objeto esperado.
 * hasProperty: Verifica se um objeto contém uma propriedade específica.
 * hasFieldOrProperty: Verifica se um objeto contém um campo ou propriedade específica.
 * hasFieldOrPropertyWithValue: Verifica se um objeto contém um campo ou propriedade com um valor específico.
 * hasCause(): Verifica se o tipo da exceção contém a causa especificada.
 * hasRootCause(): Verifica se o tipo da exceção contém a causa raiz especificada.
 * assertThat(exception).hasRootCause(rootCause);

 * Comentários de diferenciação:
 * hasMessage: Verifica a mensagem exata da exceção.
 * hasMessageContaining: Verifica se a mensagem contém uma substring específica.
 * isSameAs: Verifica se dois objetos são exatamente o mesmo objeto (identidade).
 * isNotSameAs: Verifica se dois objetos não são o mesmo objeto (identidade).
 * hasProperty: Verifica se um objeto tem uma propriedade específica (getter).
 * hasFieldOrProperty: Verifica se um objeto tem um campo ou propriedade específica (campo ou getter).
 * hasCause: Verifica a causa imediata da exceção.
 * hasRootCause: Verifica a causa raiz da exceção.
 */

@SpringBootTest
@WebAppConfiguration
@Transactional
class CurrencyServiceIntegrationTest {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private AwesomeAPIClient awesomeApiClient;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        currencyRepository.deleteAll();
    }

    /**
     * Insere uma moeda no repositório.
     * Chama o método get do serviço.
     * Verifica se a resposta contém a moeda esperada.
     */

    @Test
    void testGet() {
        Currency currency = Currency.builder()
                .name("Dólar Americano")
                .code("USD")
                .build();
        currencyRepository.save(currency);

        var response = currencyService.get();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getLabel()).isEqualTo("USD - Dólar Americano");
    }

    @Test
    void testCreate() throws CurrencyException {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        Long id = currencyService.create(request);

        assertThat(id).isNotNull();
        Optional<Currency> savedCurrency = currencyRepository.findById(id);
        assertThat(savedCurrency).isPresent();
        assertThat(savedCurrency.get().getName()).isEqualTo("Dólar Americano");
        assertThat(savedCurrency.get().getCode()).isEqualTo("USD");
    }

    @Test
    void testCreateThrowsCurrencyException() {
        Currency existingCurrency = Currency.builder()
                .name("Dólar Americano")
                .code("USD")
                .build();
        currencyRepository.save(existingCurrency);

        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        assertThatThrownBy(() -> currencyService.create(request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin already exists");
    }

    @Test
    void testUpdate() throws CurrencyException {
        Currency existingCurrency = Currency.builder()
                .name("Euro")
                .code("EUR")
                .build();
        existingCurrency = currencyRepository.save(existingCurrency);

        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        currencyService.update(existingCurrency.getId(), request);

        Optional<Currency> updatedCurrency = currencyRepository.findById(existingCurrency.getId());
        assertThat(updatedCurrency).isPresent();
        assertThat(updatedCurrency.get().getName()).isEqualTo("Dólar Americano");
        assertThat(updatedCurrency.get().getCode()).isEqualTo("USD");
    }

    @Test
    void testUpdateThrowsCoinNotFoundException() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        assertThatThrownBy(() -> currencyService.update(1L, request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Coin not found");

        Optional<Currency> currency = currencyRepository.findById(1L);
        assertThat(currency).isEmpty();
    }

    @Test
    void testUpdateThrowsCurrencyException() {
        Currency existingCurrency1 = Currency.builder()
                .name("Euro")
                .code("EUR")
                .build();
        existingCurrency1 = currencyRepository.save(existingCurrency1);

        Currency existingCurrency2 = Currency.builder()
                .name("Dólar Americano")
                .code("USD")
                .build();
        currencyRepository.save(existingCurrency2);

        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        Currency finalExistingCurrency = existingCurrency1;
        assertThatThrownBy(() -> currencyService.update(finalExistingCurrency.getId(), request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin already exists");
    }

    @Test
    void testDelete() {
        Currency existingCurrency = Currency.builder()
                .name("Dólar Americano")
                .code("USD")
                .build();
        existingCurrency = currencyRepository.save(existingCurrency);

        currencyService.delete(existingCurrency.getId());

        Optional<Currency> deletedCurrency = currencyRepository.findById(existingCurrency.getId());
        assertThat(deletedCurrency).isEmpty();
    }

    @Test
    void testDeleteThrowsCoinNotFoundException() {
        assertThatThrownBy(() -> currencyService.delete(1L))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Coin not found");
    }

    @Test
    void testConvert() throws CoinNotFoundException {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        String currencyCode = request.getFrom() + request.getTo();
        Map<String, CurrencyAPIResponse> apiResponse = awesomeApiClient.getLastCurrency(request.getFrom() + "-" + request.getTo());

        assertThat(apiResponse).isNotNull();

        CurrencyAPIResponse currencyAPIResponse = apiResponse.get(currencyCode);

        assertThat(currencyAPIResponse).isNotNull();
        assertThat(currencyAPIResponse.getLow()).isNotNull();

        ConvertCurrencyResponse result = currencyService.convert(request);

        BigDecimal expectedAmount = request.getAmount().multiply(currencyAPIResponse.getLow());
        assertThat(result.getAmount()).isEqualTo(expectedAmount);
    }

    @Test
    void testConvertThrowsCoinNotFoundExceptionWhenResponseIsNull() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("INVALID");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> currencyService.convert(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Exchange rate not found");
    }

    @Test
    void testValidateCurrencyRequestThrowsCurrencyException() {
        CurrencyRequest request = new CurrencyRequest();

        assertThatThrownBy(() -> currencyService.create(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid CurrencyRequest");
    }

    @Test
    void testValidateCurrencyIdThrowsCurrencyException() {
        assertThatThrownBy(() -> currencyService.delete(null))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");

        assertThatThrownBy(() -> currencyService.delete(0L))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");
    }

    @Test
    void testValidateConvertRequestThrowsCurrencyException() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();

        assertThatThrownBy(() -> currencyService.convert(request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid ConvertCurrencyRequest");
    }
}
