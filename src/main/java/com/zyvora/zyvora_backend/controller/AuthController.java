package com.zyvora.controller;

import com.zyvora.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Quick health check - hit this to confirm the backend is alive
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Zyvora auth module is running");
    }

    // Frontend calls this right after /oauth/callback to fetch
    // the logged-in user's profile using the JWT it just received
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("picture", user.getPicture());
        response.put("provider", user.getProvider());
        response.put("readinessScore", user.getReadinessScore());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless JWT - logout is handled client-side by deleting the token.
        // This endpoint exists for the frontend to call for consistency / future blacklist logic.
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}