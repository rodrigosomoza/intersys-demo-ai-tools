package ch.innovation.ai.tools.demo.controller;

import ch.innovation.ai.tools.demo.dto.UserInfo;
import ch.innovation.ai.tools.demo.dto.UserLookupRequest;
import ch.innovation.ai.tools.demo.dto.UserLookupResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserLookupController {

    private static final Map<String, UserInfo> USERS = new HashMap<>();

    static {
        USERS.put("A", new UserInfo("A", "User A", new BigDecimal("100.00")));
        USERS.put("B", new UserInfo("B", "User B", new BigDecimal("50.00")));
        USERS.put("C", new UserInfo("C", "User C", new BigDecimal("0.00")));
    }

    @PostMapping(path = "/lookup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserLookupResponse lookup(@RequestBody UserLookupRequest request) {
        List<UserInfo> users = new ArrayList<>();
        for (String id : request.userIds()) {
            UserInfo info = USERS.getOrDefault(id, new UserInfo(id, "Unknown", BigDecimal.ZERO));
            users.add(info);
        }
        return new UserLookupResponse(request.transactionId(), users);
    }
}
