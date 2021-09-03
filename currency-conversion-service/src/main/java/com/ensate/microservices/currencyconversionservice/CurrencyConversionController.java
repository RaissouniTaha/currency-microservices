package com.ensate.microservices.currencyconversionservice;

import java.math.BigDecimal;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CurrencyConversionController {
	
	private Logger logger = LoggerFactory.getLogger(CurrencyConversionController.class);
	
	@Autowired
	private CurrencyExchangeProxy proxy;
	
	@GetMapping(path = "/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion calculateCurrencyConversion(
			@PathVariable(name = "from") String from,
			@PathVariable(name = "to") String to,
			@PathVariable(name = "quantity") BigDecimal quantity
			) {
		// CHANGE-KUBERNETES
		logger.info("calculateCurrencyConversion called with {} to {} with {}",from,to);
		
		HashMap<String,String> uriVariables = new HashMap<String, String>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);
		ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity(
				"http://localhost:8000/currency-exchange/from/{from}/to/{to}",
				CurrencyConversion.class,
				uriVariables);
		CurrencyConversion currencyConversionResponse = responseEntity.getBody();
		CurrencyConversion currencyConversion = new CurrencyConversion(from,
																to,
																quantity,
																currencyConversionResponse.getConversionMultiple(),
													quantity.multiply(currencyConversionResponse.getConversionMultiple()),
																currencyConversionResponse.getEnvironment()+ " Rest Template ");
currencyConversion.setId(currencyConversionResponse.getId());
		return currencyConversion;
		
	}
	@GetMapping(path = "/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion calculateCurrencyConversionFeign(
			@PathVariable(name = "from") String from,
			@PathVariable(name = "to") String to,
			@PathVariable(name = "quantity") BigDecimal quantity
			) {
		
		// CHANGE-KUBERNETES
		logger.info("calculateCurrencyConversionFeign called with {} to {} with {}",from,to);
		
		CurrencyConversion currencyConversionResponse = proxy.retrieveExchangeValue(from, to);
		CurrencyConversion currencyConversion = new CurrencyConversion(from,
																to,
																quantity,
																currencyConversionResponse.getConversionMultiple(),
													quantity.multiply(currencyConversionResponse.getConversionMultiple()),
																currencyConversionResponse.getEnvironment()+" feign ");
currencyConversion.setId(currencyConversionResponse.getId());
		return currencyConversion;
		
	}
}
