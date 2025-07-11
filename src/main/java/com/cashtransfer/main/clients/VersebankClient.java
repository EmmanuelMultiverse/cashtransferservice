package com.cashtransfer.main.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import com.cashtransfer.main.model.VersebankClientRequest;
import com.cashtransfer.main.model.VersebankResponse;

@FeignClient(name = "versebank", url = "http://versebank:5001")
public interface VersebankClient {

	@PostMapping("/deposit")
	VersebankResponse transferCashToBank(VersebankClientRequest transferRequest);

	@PostMapping("/withdrawal")
	VersebankResponse transferCashToMulticashAccount(VersebankClientRequest transferRequest);

}
