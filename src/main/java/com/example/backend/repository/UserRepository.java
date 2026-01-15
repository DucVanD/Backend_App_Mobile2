package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

        Optional<User> findByEmail(String email);

        boolean existsByEmail(String email);

        boolean existsByPhone(String phone);

        @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
                        "(LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "u.role IN :roles")
        org.springframework.data.domain.Page<User> search(String keyword,
                        @org.springframework.data.repository.query.Param("roles") java.util.List<com.example.backend.entity.enums.Role> roles,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role IN :roles")
        org.springframework.data.domain.Page<User> findByRoles(
                        @org.springframework.data.repository.query.Param("roles") java.util.List<com.example.backend.entity.enums.Role> roles,
                        org.springframework.data.domain.Pageable pageable);
}
