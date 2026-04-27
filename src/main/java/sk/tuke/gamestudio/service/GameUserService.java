package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.GameUser;

public interface GameUserService {
    GameUser createUser(GameUser user) throws GameUserException;

    GameUser getUserById(Long id) throws GameUserException;

    GameUser getUserByUsername(String username) throws GameUserException;

    boolean existsByUsername(String username) throws GameUserException;

    GameUser updateUser(GameUser user) throws GameUserException;
}
