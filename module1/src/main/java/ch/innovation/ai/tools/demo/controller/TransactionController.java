package ch.innovation.ai.tools.demo.controller;

import ch.innovation.ai.tools.demo.dto.TransactionRequest;
import ch.innovation.ai.tools.demo.dto.TransactionResult;
import ch.innovation.ai.tools.demo.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public TransactionResult create(@Valid @RequestBody TransactionRequest request) {
        return transactionService.process(request);
    }
}
