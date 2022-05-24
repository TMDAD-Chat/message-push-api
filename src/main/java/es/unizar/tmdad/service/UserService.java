package es.unizar.tmdad.service;

import es.unizar.tmdad.repository.entity.UserEntity;

public interface UserService {

    UserEntity getUser(String argument);
}
