package it.unicas.project.template.address.model.dao;

import java.sql.SQLException;

public interface UserDAO {
    boolean authenticate(String username, String password) throws SQLException;
    boolean register(String username, String password) throws SQLException;
}