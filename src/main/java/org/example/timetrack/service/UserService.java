package org.example.timetrack.service;

import org.example.timetrack.dto.UserDTO;
import org.example.timetrack.entity.User;
import org.example.timetrack.security.JwtUtils;
import org.example.timetrack.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // Метод для создания пользователя (регистрация)
    public UserDTO createUser(UserDTO userDTO) {
        // Проверяем, существует ли уже пользователь с таким именем
        Optional<User> existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with this username already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword())); // Хешируем пароль
        userRepository.save(user);

        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    // Метод для получения пользователя по ID
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    // Метод для получения всех пользователей
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }

    // Метод для обновления данных пользователя
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole());
        userRepository.save(user);

        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    // Метод для удаления пользователя
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Метод для аутентификации пользователя
    public String authenticate(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtils.generateToken(user.getUsername());
            }
        }
        throw new RuntimeException("Invalid username or password");
    }

    // Метод для поиска пользователя по имени
    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }

    public void logout(String token) {
        tokenBlacklistService.blacklistToken(token);  // Add the token to blacklist
    }
}
