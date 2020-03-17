package az.gdg.msauth.dao;

import az.gdg.msauth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Integer>{

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);
}

