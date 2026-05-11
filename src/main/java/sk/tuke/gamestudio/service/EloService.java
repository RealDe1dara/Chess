package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Elo;

import java.util.List;

public interface EloService {
    void setElo(Elo elo) throws EloException;
    Integer getElo(String game, String player) throws EloException;
    void reset() throws EloException;
    List<Elo> getTopElo(String game) throws EloException;
    default void renamePlayer(String oldName, String newName) throws EloException {}
}