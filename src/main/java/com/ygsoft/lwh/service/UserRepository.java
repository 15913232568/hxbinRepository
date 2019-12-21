package com.ygsoft.lwh.service;

import com.ygsoft.lwh.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User,String> {

}
