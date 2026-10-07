package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Student;
import com.example.demo.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repo;

    // 🔥 ADD STUDENT
    public Student addStudent(Student s) {

        Student student = new Student();

        student.setName(s.getName());
        student.setAge(s.getAge());
        student.setCourse(s.getCourse());
        student.setStream(s.getStream());
        student.setDob(s.getDob());
        student.setEmail(s.getEmail());
        student.setMobile(s.getMobile());
        student.setUsername(s.getUsername());

        return repo.save(student);
    }

    // 🔥 GET ALL
    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    // 🔥 GET BY ID
    public Student getStudentById(long id) {
        return repo.findById(id).orElse(null);
    }

    // 🔥 SEARCH
    public List<Student> searchByName(String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }

    // 🔥 UPDATE
    public Student updateStudent(long id, Student s) {

        Student existing = repo.findById(id).orElse(null);

        if(existing != null) {

            if(s.getName() != null)
                existing.setName(s.getName());

            if(s.getAge() != 0)
                existing.setAge(s.getAge());

            if(s.getCourse() != null)
                existing.setCourse(s.getCourse());

            if(s.getStream() != null)
                existing.setStream(s.getStream());

            if(s.getDob() != null)
                existing.setDob(s.getDob());

            if(s.getEmail() != null)
                existing.setEmail(s.getEmail());

            if(s.getMobile() != null)
                existing.setMobile(s.getMobile());
            
            if(s.getProfilePhoto() != null)
                existing.setProfilePhoto(s.getProfilePhoto());

            return repo.save(existing);
        }

        return null;
    }

    // 🔥 DELETE
    public void deleteStudent(long id) {
        repo.deleteById(id);
    }

    // 🔥 GET BY USERNAME
    public Student getByUsername(String username) {
        return repo.findByUsername(username);
    }
}