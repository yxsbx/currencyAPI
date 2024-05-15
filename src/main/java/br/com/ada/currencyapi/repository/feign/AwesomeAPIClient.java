package br.com.ada.currencyapi.repository.feign;

import br.com.ada.currencyapi.domain.CurrencyAPIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(value = "awesome-api-client", url = "https://economia.awesomeapi.com.br")
public interface AwesomeAPIClient {
    @GetMapping("/json/last/{currencies}")
    Map<String, CurrencyAPIResponse> getLastCurrency(@PathVariable("currencies") List<String> codes);
}
