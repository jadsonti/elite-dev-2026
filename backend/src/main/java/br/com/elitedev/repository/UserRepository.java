package br.com.elitedev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.elitedev.domain.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}