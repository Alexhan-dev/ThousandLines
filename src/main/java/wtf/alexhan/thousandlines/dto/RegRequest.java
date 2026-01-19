package wtf.alexhan.thousandlines.dto;

public class RegRequest {
    private String username;
    private String password;
    private String email;
    private String inviteCode; // 新增邀请码字段

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getInviteCode() { return inviteCode; } // 新增邀请码的getter
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; } // 新增邀请码的setter
}