package com.seaway.game.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameUsers;

@Repository
public interface GameUsersRepository extends JpaRepository<GameUsers, Integer> {

	GameUsers findByWxUid(String wxUid);

	GameUsers findByWxSid(String wxSid);

	GameUsers findByWxUidAndEnabled(String wxUid, boolean enabled);

}
