package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private StudentRepository studentRepo;


    // =========================================================
    // STUDENT REGISTRATION REQUEST
    // =========================================================

    @PostMapping("/register")
    public String register(@RequestBody Student student) {

        // REQUIRED FIELD VALIDATION

        if (student.getName() == null ||
                student.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (student.getDob() == null ||
                student.getDob().trim().isEmpty()) {
            return "DOB is required";
        }

        if (student.getCourse() == null ||
                student.getCourse().trim().isEmpty()) {
            return "Course is required";
        }

        if (student.getStream() == null ||
                student.getStream().trim().isEmpty()) {
            return "Stream is required";
        }

        if (student.getEmail() == null ||
                student.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        if (student.getMobile() == null ||
                student.getMobile().trim().isEmpty()) {
            return "Mobile is required";
        }

        if (student.getUsername() == null ||
                student.getUsername().trim().isEmpty()) {
            return "Username is required";
        }

        if (student.getPassword() == null ||
                student.getPassword().trim().isEmpty()) {
            return "Password is required";
        }


        // EMAIL VALIDATION

        if (!student.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {

            return "Invalid email format";
        }


        // MOBILE VALIDATION

        if (!student.getMobile().matches("\\d{10}")) {
            return "Mobile number must be 10 digits";
        }


        // PASSWORD VALIDATION

        if (student.getPassword().length() < 6) {
            return "Password must be at least 6 characters";
        }


        // AGE VALIDATION

        if (student.getAge() < 15 || student.getAge() > 100) {
            return "Invalid age";
        }


        // USERNAME CHECK

        if (userRepo.findByUsername(student.getUsername()) != null) {
            return "Username already exists";
        }


        // EMAIL CHECK

        boolean emailExists = studentRepo.findAll()
                .stream()
                .anyMatch(s ->
                        s.getEmail() != null &&
                        s.getEmail().equalsIgnoreCase(student.getEmail())
                );

        if (emailExists) {
            return "Email already exists";
        }


        // MOBILE CHECK

        boolean mobileExists = studentRepo.findAll()
                .stream()
                .anyMatch(s ->
                        s.getMobile() != null &&
                        s.getMobile().equals(student.getMobile())
                );

        if (mobileExists) {
            return "Mobile already exists";
        }


        // SAVE STUDENT PROFILE

        Student newStudent = new Student();

        newStudent.setName(student.getName());
        newStudent.setAge(student.getAge());
        newStudent.setCourse(student.getCourse());
        newStudent.setStream(student.getStream());
        newStudent.setDob(student.getDob());
        newStudent.setEmail(student.getEmail());
        newStudent.setMobile(student.getMobile());
        newStudent.setUsername(student.getUsername());

        studentRepo.save(newStudent);


        // CREATE LOGIN USER

        User user = new User();

        user.setUsername(student.getUsername());
        user.setPassword(encoder.encode(student.getPassword()));
        user.setRole("STUDENT");
        user.setStatus("PENDING");

        userRepo.save(user);

        return "Account request submitted successfully";
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        User user = userRepo.findByUsername(request.getUsername());

        if (user == null) {
            return "User not found";
        }


        // CHECK PASSWORD

        if (!encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return "Invalid password";
        }


        // CHECK SELECTED ROLE

        if (!user.getRole().equalsIgnoreCase(request.getRole())) {

            return "Wrong role selected. This account is registered as "
                    + user.getRole();
        }


        // CHECK APPROVAL

        if (!user.getStatus().equalsIgnoreCase("APPROVED")) {

            return "Account pending approval";
        }


        // CREATE JWT

        return JwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );
    }


    // =========================================================
    // CREATE NEW ADMIN
    // ONLY EXISTING ADMIN CAN DO THIS
    // =========================================================

    @PostMapping("/admin/create")
    public String createAdmin(
            @RequestBody AdminRequest request,
            @RequestHeader("Authorization") String token) {

        try {

            String currentUsername =
                    JwtUtil.getUsername(token);

            String currentRole =
                    JwtUtil.getRole(token);


            // ONLY ADMIN CAN CREATE ADMIN

            if (!"ADMIN".equalsIgnoreCase(currentRole)) {
                return "Unauthorized. Only admins can create another admin.";
            }


            // CHECK USERNAME

            if (userRepo.findByUsername(request.getUsername()) != null) {
                return "Username already exists";
            }


            // VALIDATION

            if (request.getUsername() == null ||
                    request.getUsername().trim().isEmpty()) {

                return "Username is required";
            }

            if (request.getPassword() == null ||
                    request.getPassword().length() < 6) {

                return "Password must be at least 6 characters";
            }


            // CREATE ADMIN

            User admin = new User();

            admin.setUsername(request.getUsername().trim());

            admin.setPassword(
                    encoder.encode(request.getPassword())
            );

            admin.setRole("ADMIN");

            admin.setStatus("APPROVED");

            userRepo.save(admin);

            return "New admin created successfully";

        } catch (Exception e) {

            return "Unauthorized or invalid token";
        }
    }


    // =========================================================
    // GET CURRENT USER PROFILE
    // =========================================================

    @GetMapping("/profile")
    public User getProfile(
            @RequestHeader("Authorization") String token) {

        String username = JwtUtil.getUsername(token);

        User user = userRepo.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Never send password to frontend
        user.setPassword(null);

        return user;
    }


    // =========================================================
    // CHANGE USERNAME / PASSWORD
    // =========================================================

    @PutMapping("/profile")
    public String updateProfile(
            @RequestBody ProfileUpdateRequest request,
            @RequestHeader("Authorization") String token) {

        try {

            String oldUsername =
                    JwtUtil.getUsername(token);

            String role =
                    JwtUtil.getRole(token);


            User user =
                    userRepo.findByUsername(oldUsername);


            if (user == null) {
                return "User not found";
            }


            // -------------------------------------------------
            // USERNAME CHANGE
            // -------------------------------------------------

            if (request.getNewUsername() != null &&
                    !request.getNewUsername().trim().isEmpty() &&
                    !request.getNewUsername()
                            .equals(oldUsername)) {

                User existing =
                        userRepo.findByUsername(
                                request.getNewUsername()
                        );

                if (existing != null) {
                    return "New username already exists";
                }


                // Update User table

                user.setUsername(
                        request.getNewUsername().trim()
                );


                // If STUDENT, also update Student table

                if ("STUDENT".equalsIgnoreCase(role)) {

                    Student student =
                            studentRepo.findByUsername(oldUsername);

                    if (student != null) {

                        student.setUsername(
                                request.getNewUsername().trim()
                        );

                        studentRepo.save(student);
                    }
                }
            }


            // -------------------------------------------------
            // PASSWORD CHANGE
            // -------------------------------------------------

            if (request.getNewPassword() != null &&
                    !request.getNewPassword().trim().isEmpty()) {

                if (request.getNewPassword().length() < 6) {

                    return "New password must be at least 6 characters";
                }

                user.setPassword(
                        encoder.encode(
                                request.getNewPassword()
                        )
                );
            }


            userRepo.save(user);


            // Return NEW JWT

            return JwtUtil.generateToken(
                    user.getUsername(),
                    user.getRole()
            );

        } catch (Exception e) {

            return "Unable to update profile";
        }
    }


    // =========================================================
    // PENDING STUDENTS
    // =========================================================

    @GetMapping("/pending")
    public List<User> getPendingUsers(
            @RequestHeader("Authorization") String token) {

        String role = JwtUtil.getRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Unauthorized");
        }

        return userRepo.findByStatus("PENDING");
    }


    // =========================================================
    // APPROVE STUDENT
    // =========================================================

    @PutMapping("/approve/{username}")
    public String approve(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        String role = JwtUtil.getRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return "Unauthorized";
        }


        User user = userRepo.findByUsername(username);

        if (user == null) {
            return "User not found";
        }

        user.setStatus("APPROVED");

        userRepo.save(user);

        return "Student approved successfully";
    }


    // =========================================================
    // REJECT STUDENT
    // =========================================================

    @DeleteMapping("/reject/{username}")
    public String reject(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        String role = JwtUtil.getRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return "Unauthorized";
        }


        User user = userRepo.findByUsername(username);

        if (user == null) {
            return "User not found";
        }


        userRepo.delete(user);


        Student student =
                studentRepo.findByUsername(username);

        if (student != null) {
            studentRepo.delete(student);
        }


        return "Student rejected";
    }


    // =========================================================
    // REQUEST CLASSES
    // =========================================================

    public static class LoginRequest {

        private String username;
        private String password;
        private String role;

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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }


    public static class AdminRequest {

        private String username;
        private String password;

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
    }


    public static class ProfileUpdateRequest {

        private String newUsername;
        private String newPassword;

        public String getNewUsername() {
            return newUsername;
        }

        public void setNewUsername(String newUsername) {
            this.newUsername = newUsername;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}