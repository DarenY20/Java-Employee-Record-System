package beans;

import model.User;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.persistence.*;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.List;

@Named("userBean")
@SessionScoped
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String fullName;
    private User loggedInUser;

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("UserPU");

    public String signUp() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            List<User> result = query.getResultList();
            if (!result.isEmpty()) return "usernameTaken";

            em.getTransaction().begin();
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(hashPassword(password));
            user.setFullName(fullName);
            em.persist(user);
            em.getTransaction().commit();
            loggedInUser = user;
            return "welcome";
        } finally {
            em.close();
        }
    }

    public String login() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            List<User> result = query.getResultList();

            if (!result.isEmpty() && result.get(0).getPasswordHash().equals(hashPassword(password))) {
                loggedInUser = result.get(0);
                return "welcome";
            }
            return "loginFailed";
        } finally {
            em.close();
        }
    }

    public String logout() {
        loggedInUser = null;
        username = null;
        password = null;
        fullName = null;
        return "index";
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public String getGreeting() {
        return "Hello, " + (loggedInUser != null ? loggedInUser.getFullName() : "Guest") + "!";
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Getters and setters...
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

}
