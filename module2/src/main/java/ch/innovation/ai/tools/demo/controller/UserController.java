package ch.innovation.ai.tools.demo.controller;

import ch.innovation.ai.tools.demo.dto.UserBalanceRequest;
import ch.innovation.ai.tools.demo.dto.UserBalanceResponse;
import ch.innovation.ai.tools.demo.dto.UserBalanceUpdateRequest;
import ch.innovation.ai.tools.demo.dto.UserBalanceUpdateResponse;
import ch.innovation.ai.tools.demo.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserAccountService userAccountService;

    public UserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/balances")
    public ResponseEntity<UserBalanceResponse> getUserBalances(@Valid @RequestBody UserBalanceRequest request) {
        UserBalanceResponse response = userAccountService.getUserBalances(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/balances")
    public ResponseEntity<UserBalanceUpdateResponse> updateUserBalance(@Valid @RequestBody UserBalanceUpdateRequest request) {
        UserBalanceUpdateResponse response = userAccountService.updateUserBalance(request);
        return ResponseEntity.ok(response);
    }
}
