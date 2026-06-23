package com.study.auth.Repository;

import com.study.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    @Modifying // notice INSERT or UPDATE or DELETE
    @Transactional // 메서드를 트랜잭션으로 묶음, @Modifying은 무조건 transactional 위에서 작동
    @Query("update User u set u.tokkenkey = :tokken where u.id = :id") // 테이블 명 x 엔티티 클래스명 o
    int updateTokkenKey(@Param("id") Long id, @Param("tokken") String tokken); // @Param("[쿼리 내 변수명]") [변수값]

}