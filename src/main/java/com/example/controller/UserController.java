package com.example.controller;

import com.example.model.User;
import com.example.service.interfaces.IUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Mono<User> createUser(@Valid @RequestBody User user) {
        return userService.save(user);
    }

    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable String id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}/balance")
    public Mono<User> updateUser(@PathVariable String id, @RequestBody User user) {
        return userService.findById(id)
                .flatMap(existingUser -> {
                    //existingProducto.setName(user.getName());
                    existingUser.setBalance(user.getBalance());
                    return userService.save(existingUser);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteUser(@PathVariable String id) {
        return userService.deleteById(id);
    }

}
