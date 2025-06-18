package com.handcraft.crafts.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.handcraft.crafts.entity.UserInfo;
import com.handcraft.crafts.enums.Role;
import com.handcraft.crafts.enums.Status;
import com.handcraft.crafts.repository.UserInfoRepository;
;

@Service
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository repository;
    private final PasswordEncoder encoder;

    public UserInfoService(UserInfoRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    // Used by Spring Security
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Load full User entity by email
    public UserInfo loadUserByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    // Register new user or seller
    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));

        // Set default role if not provided
        Role role = userInfo.getRoles();
        if (role == null) {
            role = Role.ROLE_USER;
        }
        userInfo.setRoles(role);

        // Set status based on role
        if (role == Role.ROLE_SELLER) {
            userInfo.setStatus(Status.PENDING);
        } else {
            userInfo.setStatus(Status.APPROVED);
        }

        repository.save(userInfo);
        return "User registered successfully.";
    }

    // Save or update a user (used in profile)
    public void saveUser(UserInfo userInfo) {
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
    }

    // Admin: Approve seller
    public boolean approveSeller(int sellerId) {
        return updateUserStatus(sellerId, Status.APPROVED);
    }

    // Admin: Suspend user
    public boolean suspendUser(int userId) {
        return updateUserStatus(userId, Status.SUSPENDED);
    }

    private boolean updateUserStatus(int userId, Status newStatus) {
        Optional<UserInfo> userOpt = repository.findById(userId);
        if (userOpt.isPresent()) {
            UserInfo user = userOpt.get();
            user.setStatus(newStatus);
            repository.save(user);
            return true;
        }
        return false;
    }

    // Admin & Seller: List sellers by status
    public List<UserInfo> getPendingSellers() {
        return repository.findByRolesAndStatus(Role.ROLE_SELLER, Status.PENDING);
    }

    public List<UserInfo> getApprovedSellers() {
        return repository.findByRolesAndStatus(Role.ROLE_SELLER, Status.APPROVED);
    }
}
