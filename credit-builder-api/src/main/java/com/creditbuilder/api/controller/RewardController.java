package com.creditbuilder.api.controller;

import com.creditbuilder.api.entity.Reward;
import com.creditbuilder.api.repository.UserRepository;
import com.creditbuilder.api.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<List<Reward>> myRewards(
            @AuthenticationPrincipal UserDetails userDetails
            ){
        UUID userId = getUserId(userDetails);
        return ResponseEntity.ok(rewardService.getMyRewards(userId));
    }

    private UUID getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }
}
