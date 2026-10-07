package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Student;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.StudentService;

@RestController
@RequestMapping("/students")
@CrossOrigin("*")
public class StudentController {

    @Autowired
    private StudentService service;


    // =========================================================
    // GET ALL STUDENTS
    // ADMIN ONLY
    // =========================================================

    @GetMapping
    public List<Student> getAll(
            @RequestHeader("Authorization") String token) {

        String role = JwtUtil.getRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Unauthorized");
        }

        return service.getAllStudents();
    }


    // =========================================================
    // GET LOGGED-IN STUDENT
    // =========================================================

    @GetMapping("/my")
    public Student getMyData(
            @RequestHeader("Authorization") String token) {

        String username = JwtUtil.getUsername(token);

        return service.getByUsername(username);
    }


    // =========================================================
    // ADD STUDENT PROFILE
    // =========================================================

    @PostMapping
    public Student addStudent(
            @RequestBody Student s,
            @RequestHeader("Authorization") String token) {

        String username = JwtUtil.getUsername(token);

        s.setUsername(username);

        return service.addStudent(s);
    }


    // =========================================================
    // SEARCH
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/search/{name}")
    public List<Student> search(
            @PathVariable String name,
            @RequestHeader("Authorization") String token) {

        String role = JwtUtil.getRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Unauthorized");
        }

        return service.searchByName(name);
    }


    // =========================================================
    // UPDATE STUDENT
    // =========================================================

    @PutMapping("/{id}")
    public Student update(
            @PathVariable long id,
            @RequestBody Student s,
            @RequestHeader("Authorization") String token) {

        String username = JwtUtil.getUsername(token);

        String role = JwtUtil.getRole(token);

        Student existing =
                service.getStudentById(id);


        if (existing == null) {
            throw new RuntimeException("Student not found");
        }


        // ADMIN CAN UPDATE ANY STUDENT

        if ("ADMIN".equalsIgnoreCase(role)) {

            return service.updateStudent(id, s);
        }


        // STUDENT CAN UPDATE ONLY OWN DATA

        if (!existing.getUsername().equals(username)) {

            throw new RuntimeException("Unauthorized");
        }


        return service.updateStudent(id, s);
    }


    // =========================================================
    // DELETE
    // ADMIN ONLY
    // =========================================================

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable long id,
            @RequestHeader("Authorization") String token) {

        String role = JwtUtil.getRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {

            throw new RuntimeException("Unauthorized");
        }

        service.deleteStudent(id);

        return "Deleted";
    }
}