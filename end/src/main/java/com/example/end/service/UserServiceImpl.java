package com.example.end.service;

import com.example.end.config.TokenResolver;
import com.example.end.domain.dto.*;
import com.example.end.domain.mapper.UserEditMapper;
import com.example.end.domain.mapper.UserViewMapper;
import com.example.end.domain.model.User;
import com.example.end.exception.ApiException;
import com.example.end.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final FileService fileService;
    private final JavaMailSender mailSender;
    private final TokenResolver tokenResolver;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserViewMapper userViewMapper;
    private final UserEditMapper userEditMapper;

    @Override
    public Path updateProfileImage(String userId, MultipartFile file) {
        var user = getUserOrThrow(userId);

        if(file.isEmpty() && user.getImageUrl() != null) {
            fileService.delete(Paths.get(user.getImageUrl()));
            return null;
        }

        var imagePath = Path.of(user.getId().toString());

        Path fullPath;
        if(user.getImageUrl() == null) {
            fullPath = fileService.saveProfileImage(file, imagePath);
        } else {
            fullPath = fileService.replaceProfileImage(file, imagePath);
        }

        user.setImageUrl(fullPath.toString());
        userRepository.save(user);

        return fullPath;
    }

    @Override
    public LoggedInUser updateUserInfo(String userId, UpdateUserRequest request) {
        var user = getUserOrThrow(userId);

        if(!isReferencesValid(request, user)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid contact references");
        }

        userEditMapper.update(request, user);
        user = userRepository.save(user);
        return userViewMapper.toLoggedInUser(user);
    }

    private boolean isReferencesValid(UpdateUserRequest request, User user) {
        final var all = user.getContacts();

        var foldersValid =  request
                .folders()
                .values()
                .stream()
                .allMatch(all::containsAll);

        if(!foldersValid) {
            return false;
        }

        var remaindersValid = request.remainders()
                .stream()
                .map(User.Remainder::id)
                .allMatch(all::contains);

        return remaindersValid;
    }

    @Override
    public LoggedInUser addContacts(String userId, Set<ObjectId> add) {
        var user = getUserOrThrow(userId);
        user.getContacts().addAll(add);
        user = userRepository.save(user);
        return userViewMapper.toLoggedInUser(user);
    }

    @Override
    public LoggedInUser removeContacts(String userId, Set<ObjectId> toRemove) {
        var user = getUserOrThrow(userId);

        var contacts = user.getContacts();
        contacts.removeAll(toRemove);

        var folders = user.getFolders();
        folders.values().forEach(folder -> folder.retainAll(contacts));

        var remainders = user.getRemainders();
        remainders.removeIf(remainder -> toRemove.contains(remainder.id()));

        user = userRepository.save(user);
        return userViewMapper.toLoggedInUser(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private User getUserOrThrow(String userId) {
        return userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        String.format("user with id: %s doesn't exist", userId)));
    }

    @Override
    public void changePassword(String userId, UpdatePasswordRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userId, request.oldPassword())
        );

        var user = (User) authentication.getPrincipal();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void register(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "user with this email already exists");
        }

        var user = userEditMapper.create(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);

        var token = tokenResolver.generateToken(user.getId().toHexString(), Map.of("email", user.getEmail()));
        sendEmailConfirmation(user.getEmail(), token);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password())
        );

        var user = (User) authentication.getPrincipal();

        var loggedInUser = userViewMapper.toLoggedInUser(user);
        var token = tokenResolver.generateToken(user.getId().toHexString());
        return new AuthResponse(loggedInUser, token);
    }


    @Override
    public void changeEmail(String userId, String email) {
        var token = tokenResolver.generateToken(userId, Map.of("email", email));
        sendEmailConfirmation(email, token);
    }

    private void sendEmailConfirmation(String email, String token) {
        var subject = "Registration Confirmation";
        var confirmationUrl = "http://localhost:8080/api/v1/registration?token=" + token;
        var message = "Click on the link below to confirm your email";

        var mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject(subject);
        mailMessage.setText(message + "\r\n" + confirmationUrl);

        mailSender.send(mailMessage);
    }

    @Override
    public void confirmRegistration(String token) {
        var jwt = tokenResolver.decodeFromToken(token);
        var user = userRepository.findById(new ObjectId(jwt.getSubject())).get();
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void confirmEmailChange(String token) {
        var jwt = tokenResolver.decodeFromToken(token);
        var user = userRepository.findById(new ObjectId(jwt.getSubject())).get();
        user.setEmail(jwt.getClaimAsString("email"));
        userRepository.save(user);
    }
}
