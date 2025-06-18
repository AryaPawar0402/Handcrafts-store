package com.handcraft.crafts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.handcraft.crafts.entity.UserInfo;
import com.handcraft.crafts.enums.Role;
import com.handcraft.crafts.enums.Status;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {

    // Used for login via email (username)
    Optional<UserInfo> findByEmail(String email);

    // Admin: Find all sellers whose status is PENDING
    List<UserInfo> findByRolesAndStatus(Role roles, Status status);
}
