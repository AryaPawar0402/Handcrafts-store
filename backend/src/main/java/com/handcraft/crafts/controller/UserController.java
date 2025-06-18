package com.handcraft.crafts.controller;

import com.handcraft.crafts.entity.AuthRequest;
import com.handcraft.crafts.entity.UserInfo;
import com.handcraft.crafts.enums.Role;  // ✅ Corrected import
import com.handcraft.crafts.service.JwtService;
import com.handcraft.crafts.service.TokenBlacklistService;
import com.handcraft.crafts.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000"})
@Tag(name = "User Authentication & Management", description = "Handles registration, login, profile, and role-specific operations")
public class UserController {

    @Autowired
    private UserInfoService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "User with this email already exists"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserInfo userInfo) {
        try {
            if (userService.loadUserByEmail(userInfo.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this email already exists.");
            }
            String result = userService.addUser(userInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Login user and return JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

            UserInfo user = userService.loadUserByEmail(authRequest.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            List<String> rolesList = List.of(user.getRoles().name());
            String token = jwtService.generateToken(authRequest.getEmail(), rolesList);

            return ResponseEntity.ok().body("{\"token\": \"" + token + "\"}");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password!");
        }
    }

    @Operation(summary = "Logout user and blacklist token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "No token provided")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            return ResponseEntity.ok("Logout successful. Token blacklisted.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No token provided.");
        }
    }

    @Operation(summary = "Get logged-in user's info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User info returned"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserInfo user = userService.loadUserByEmail(email);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
            }
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Update user's profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(Authentication authentication, @RequestBody UserInfo updatedUser) {
        try {
            String email = authentication.getName();
            UserInfo existingUser = userService.loadUserByEmail(email);

            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
            }

            existingUser.setName(updatedUser.getName());
            existingUser.setPassword(updatedUser.getPassword());
            userService.saveUser(existingUser);

            return ResponseEntity.ok("Profile updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get pending sellers (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending sellers list"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/users/pending-sellers")
    public ResponseEntity<?> getPendingSellers(Authentication authentication) {
        try {
            UserInfo requester = userService.loadUserByEmail(authentication.getName());
            if (requester.getRoles() != Role.ROLE_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            List<UserInfo> pendingSellers = userService.getPendingSellers();
            return ResponseEntity.ok(pendingSellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get approved sellers (Admin and Seller)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Approved sellers list"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/users/approved-sellers")
    public ResponseEntity<?> getApprovedSellers(Authentication authentication) {
        try {
            UserInfo requester = userService.loadUserByEmail(authentication.getName());
            if (requester.getRoles() != Role.ROLE_ADMIN && requester.getRoles() != Role.ROLE_SELLER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            List<UserInfo> approvedSellers = userService.getApprovedSellers();
            return ResponseEntity.ok(approvedSellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Approve a seller (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seller approved"),
            @ApiResponse(responseCode = "404", description = "Seller not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/users/approve/{sellerId}")
    public ResponseEntity<?> approveSeller(@PathVariable int sellerId, Authentication authentication) {
        try {
            UserInfo requester = userService.loadUserByEmail(authentication.getName());
            if (requester.getRoles() != Role.ROLE_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can approve sellers");
            }

            boolean result = userService.approveSeller(sellerId);
            if (result) {
                return ResponseEntity.ok("Seller approved successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Seller not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Suspend a user (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User suspended"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/users/suspend/{userId}")
    public ResponseEntity<?> suspendUser(@PathVariable int userId, Authentication authentication) {
        try {
            UserInfo requester = userService.loadUserByEmail(authentication.getName());
            if (requester.getRoles() != Role.ROLE_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can suspend users");
            }

            boolean result = userService.suspendUser(userId);
            if (result) {
                return ResponseEntity.ok("User suspended successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
