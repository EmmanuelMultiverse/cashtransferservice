package com.cashtransfer.main.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.cashtransfer.main.model.BankTransferRequest;

@FeignClient(name = "multibank-client", url = "http://multibank:8080/api")
public interface MultibankExternalClient {

	@PostMapping("/transaction/deposit")
	String depositToAccount(BankTransferRequest transferRequest);

	@PostMapping("/transaction/withdrawal")
	String transferToMulticashAccount(BankTransferRequest transferRequest);

}
