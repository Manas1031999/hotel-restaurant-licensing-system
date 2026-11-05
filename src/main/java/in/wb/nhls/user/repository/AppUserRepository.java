package in.wb.nhls.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.wb.nhls.user.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
