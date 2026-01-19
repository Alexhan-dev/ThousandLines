package wtf.alexhan.thousandlines.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import wtf.alexhan.thousandlines.dto.RegRequest;
import wtf.alexhan.thousandlines.model.*;
import wtf.alexhan.thousandlines.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final UsedInviteCodeRepository usedInviteCodeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, InviteCodeRepository inviteCodeRepository, UsedInviteCodeRepository usedInviteCodeRepository) {
        this.userRepository = userRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.usedInviteCodeRepository = usedInviteCodeRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void createCreatorUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new RuntimeException("用户名或邮箱已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(UserRole.CREATOR);

        userRepository.save(user);
    }

    public UserRole getRoleFromInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.isEmpty()) {
            return UserRole.USER; // 默认角色
        }else {

            Optional<InviteCode> optionalInviteCode = inviteCodeRepository.findByCode(inviteCode);
            if (optionalInviteCode.isPresent()) {
                if (optionalInviteCode.get().getRole() == UserRole.ADMIN){

                    return UserRole.ADMIN;
                }
                 else if (optionalInviteCode.get().getRole() == UserRole.CREATOR){
                     return UserRole.CREATOR;
                 }
                 else throw new RuntimeException("无效的邀请码角色");

            } else throw new RuntimeException("无效的邀请码");

        }
    }

    public void registerUser(RegRequest regRequest) {
        if (userRepository.existsByUsername(regRequest.getUsername()) || userRepository.existsByEmail(regRequest.getEmail())) {
            throw new RuntimeException("用户名或邮箱已存在");
        }

        UserRole role = getRoleFromInviteCode(regRequest.getInviteCode());

        User user = new User();
        user.setUsername(regRequest.getUsername());
        user.setPassword(passwordEncoder.encode(regRequest.getPassword()));
        user.setEmail(regRequest.getEmail());
        user.setRole(role);

        userRepository.save(user);

        // 记录邀请码使用情况
        UsedInviteCode usedInviteCode = new UsedInviteCode();
        usedInviteCode.setInviteCode(regRequest.getInviteCode());
        usedInviteCode.setUserId(String.valueOf(user.getId()));
        usedInviteCode.setUsedDate(LocalDateTime.now());
        usedInviteCode.setSuccess(true);

        deleteInviteCodeByCode(regRequest.getInviteCode());

        usedInviteCodeRepository.save(usedInviteCode);
    }
    public void deleteInviteCodeByCode(String code) {
        Optional<InviteCode> optionalInviteCode = inviteCodeRepository.findById(code);
        if (optionalInviteCode.isPresent()) {
            inviteCodeRepository.delete(optionalInviteCode.get());
        } else {
            throw new RuntimeException("邀请码不存在");
        }
    }
}