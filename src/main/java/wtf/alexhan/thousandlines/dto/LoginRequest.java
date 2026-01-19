package wtf.alexhan.thousandlines.dto;

public class LoginRequest {
    private String username;
    private String password;
    private String getUserID;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserID() { return getUserID;}
}
