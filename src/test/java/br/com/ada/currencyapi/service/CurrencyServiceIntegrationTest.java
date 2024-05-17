package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import br.com.ada.currencyapi.repository.feign.AwesomeAPIClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
class CurrencyServiceIntegrationTest {

    @Autowired
    private CurrencyService currencyService;

    @MockBean
    private AwesomeAPIClient awesomeApiClient;

    @MockBean
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Arrange: Cria um objeto Currency com o ID 1, nome "Dólar Americano" e código "USD".
     * Mock: Configura o mock currencyRepository para retornar uma lista contendo essa moeda quando o método findAll() for chamado.
     * Act: Chama o método get do currencyService.
     * Assert: Verifica se a resposta contém exatamente um elemento e se o rótulo desse elemento é "USD - Dólar Americano". Verifica também se o método findAll() do repositório foi chamado uma vez.
     */

    @Test
    void testGet() {
        Currency currency = Currency.builder()
                .id(1L)
                .name("Dólar Americano")
                .code("USD")
                .build();

        when(currencyRepository.findAll()).thenReturn(Collections.singletonList(currency));

        var response = currencyService.get();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getLabel()).isEqualTo("USD - Dólar Americano");
        verify(currencyRepository, times(1)).findAll();
    }

    /**
     * Arrange: Cria um objeto Currency com o ID 1, nome "Dólar Americano" e código "USD".
     * Mock: Configura o mock currencyRepository para retornar uma lista contendo essa moeda quando o método findAll() for chamado.
     * Act: Chama o método get do currencyService.
     * Assert: Verifica se a resposta contém exatamente um elemento e se o rótulo desse elemento é "USD - Dólar Americano".
     * Verifica também se o método findAll() do repositório foi chamado uma vez.
     */

    @Test
    void testCreate() throws CurrencyException {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        when(currencyRepository.findByName(anyString())).thenReturn(null);
        when(currencyRepository.save(any(Currency.class))).thenReturn(Currency.builder().id(1L).build());

        Long id = currencyService.create(request);

        assertThat(id).isEqualTo(1L);
        verify(currencyRepository, times(1)).findByName(anyString());
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    /**
     * Arrange: Cria um objeto CurrencyRequest com o nome "Dólar Americano" e o código "USD".
     * Mock: Configura o mock currencyRepository para retornar uma moeda ao buscar por nome.
     * Act & Assert: Verifica se o método create lança uma CurrencyException com a mensagem "Coin already exists".
     * Verifica também se o método findByName foi chamado uma vez e o método save não foi chamado.
     */

    @Test
    void testCreateThrowsCurrencyException() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        when(currencyRepository.findByName(anyString())).thenReturn(Currency.builder().build());

        assertThatThrownBy(() -> currencyService.create(request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin already exists");

        verify(currencyRepository, times(1)).findByName(anyString());
        verify(currencyRepository, times(0)).save(any(Currency.class));
    }

    /**
     * Arrange: Cria um objeto CurrencyRequest e uma moeda existente Currency.
     * Mock: Configura o mock currencyRepository para retornar a moeda existente ao buscar por ID e null ao buscar por nome.
     * Configura para retornar a moeda existente ao salvar.
     * Act: Chama o método update do currencyService com ID 1 e o request.
     * Assert: Verifica se os campos da moeda existente foram atualizados.
     * Verifica também se os métodos findById, findByName e save foram chamados uma vez cada.
     */

    @Test
    void testUpdate() throws CurrencyException {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        Currency existingCurrency = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build();

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.findByName(anyString())).thenReturn(null);
        when(currencyRepository.save(any(Currency.class))).thenReturn(existingCurrency);

        currencyService.update(1L, request);

        assertThat(existingCurrency.getName()).isEqualTo("Dólar Americano");
        assertThat(existingCurrency.getCode()).isEqualTo("USD");
        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(1)).findByName(anyString());
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    /**
     * Arrange: Cria um objeto CurrencyRequest com o nome "Dólar Americano" e o código "USD".
     * Mock: Configura o mock currencyRepository para retornar Optional.empty() ao buscar por ID.
     * Act & Assert: Verifica se o método update lança uma CoinNotFoundException com a mensagem "Coin not found".
     * Verifica também se o método findById foi chamado uma vez e os métodos findByName e save não foram chamados.
     */

    @Test
    void testUpdateThrowsCoinNotFoundException() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.update(1L, request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Coin not found");

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(0)).findByName(anyString());
        verify(currencyRepository, times(0)).save(any(Currency.class));
    }

    /**
     * Arrange: Cria um objeto CurrencyRequest e uma moeda existente Currency com o mesmo nome mas ID diferente.
     * Mock: Configura o mock currencyRepository para retornar a moeda existente ao buscar por nome.
     * Act & Assert: Verifica se o método update lança uma CurrencyException com a mensagem "Coin already exists".
     * Verifica também se os métodos findById e findByName foram chamados uma vez e o método save não foi chamado.
     */

    @Test
    void testUpdateThrowsCurrencyException() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        Currency existingCurrency = Currency.builder()
                .id(2L)
                .name("Dólar Americano")
                .code("USD")
                .build();

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.of(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build()));
        when(currencyRepository.findByName(anyString())).thenReturn(existingCurrency);

        assertThatThrownBy(() -> currencyService.update(1L, request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin already exists");

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(1)).findByName(anyString());
        verify(currencyRepository, times(0)).save(any(Currency.class));
    }

    /**
     * Arrange: Cria uma moeda existente Currency.
     * Mock: Configura o mock currencyRepository para retornar a moeda existente ao buscar por ID e não fazer nada ao deletar.
     * Act: Chama o método delete do currencyService com ID 1.
     * Assert: Verifica se os métodos findById e deleteById foram chamados uma vez cada.
     */

    @Test
    void testDelete() {
        Currency existingCurrency = Currency.builder()
                .id(1L)
                .name("Dólar Americano")
                .code("USD")
                .build();

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.of(existingCurrency));
        doNothing().when(currencyRepository).deleteById(anyLong());

        currencyService.delete(1L);

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(1)).deleteById(anyLong());
    }

    /**
     * Arrange: Configura o mock currencyRepository para retornar Optional.empty() ao buscar por ID.
     * Act & Assert: Verifica se o método delete lança uma CoinNotFoundException com a mensagem "Coin not found".
     * Verifica também se o método findById foi chamado uma vez e o método deleteById não foi chamado.
     */

    @Test
    void testDeleteThrowsCoinNotFoundException() {
        when(currencyRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.delete(1L))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Coin not found");

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(0)).deleteById(anyLong());
    }

    /**
     * Arrange: Cria um objeto ConvertCurrencyRequest com os detalhes da conversão.
     * Mock: Configura o mock awesomeApiClient para retornar uma resposta com o valor da taxa de conversão.
     * Act: Chama o método convert do currencyService com o request.
     * Assert: Verifica se o valor convertido é igual a 500. Verifica também se o método getLastCurrency foi chamado uma vez.
     */

    @Test
    void testConvert() throws CoinNotFoundException {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        CurrencyAPIResponse response = new CurrencyAPIResponse();
        response.setLow(BigDecimal.valueOf(5));

        when(awesomeApiClient.getLastCurrency(anyString())).thenReturn(Map.of("USD-BRL", response));

        ConvertCurrencyResponse result = currencyService.convert(request);

        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(500));
        verify(awesomeApiClient, times(1)).getLastCurrency(anyString());
    }

    /**
     * Arrange: Cria um objeto ConvertCurrencyRequest com os detalhes da conversão.
     * Mock: Configura o mock awesomeApiClient para retornar um mapa vazio ao buscar pela taxa de conversão.
     * Act & Assert: Verifica se o método convert lança uma CoinNotFoundException com a mensagem "Exchange rate not found".
     * Verifica também se o método getLastCurrency foi chamado uma vez.
     */

    @Test
    void testConvertThrowsCoinNotFoundException() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        when(awesomeApiClient.getLastCurrency(anyString())).thenReturn(Collections.emptyMap());

        assertThatThrownBy(() -> currencyService.convert(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Exchange rate not found");

        verify(awesomeApiClient, times(1)).getLastCurrency(anyString());
    }

    /**
     * Arrange: Cria um objeto CurrencyRequest vazio.
     * Act & Assert: Verifica se o método create lança uma CurrencyException com a mensagem "Invalid CurrencyRequest".
     */

    @Test
    void testValidateCurrencyRequestThrowsCurrencyException() {
        CurrencyRequest request = new CurrencyRequest();

        assertThatThrownBy(() -> currencyService.create(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid CurrencyRequest");
    }

    /**
     * Act & Assert: Verifica se o método delete lança uma CurrencyException com a mensagem "Invalid Currency ID" quando o ID é null ou 0.
     */

    @Test
    void testValidateCurrencyIdThrowsCurrencyException() {
        assertThatThrownBy(() -> currencyService.delete(null))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");

        assertThatThrownBy(() -> currencyService.delete(0L))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");
    }

    /**
     * Arrange: Cria um objeto ConvertCurrencyRequest vazio.
     * Act & Assert: Verifica se o método convert lança uma CurrencyException com a mensagem "Invalid ConvertCurrencyRequest".
     */

    @Test
    void testValidateConvertRequestThrowsCurrencyException() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();

        assertThatThrownBy(() -> currencyService.convert(request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid ConvertCurrencyRequest");
    }
}