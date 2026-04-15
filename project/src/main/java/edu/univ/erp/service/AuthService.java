package edu.univ.erp.service;

import edu.univ.erp.data.AuthDao;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.util.PasswordUtil;

public class AuthService {
    private final AuthDao authDao = new AuthDao();
    public UserAuth authenticate(String username, String plainPassword) {
        UserAuth user = authDao.getUserByUsername(username);
        if (user == null) {
            System.out.println("User not found in database.");
            return null;
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            System.out.println("User is inactive or locked.");
            return null;
        }
        boolean match = PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
        return match ? user : null;
    }
    public boolean changePassword(String username, String newPlainPassword) {
        String newHash = PasswordUtil.hashPassword(newPlainPassword);
        return authDao.updatePassword(username, newHash);
    }
}
