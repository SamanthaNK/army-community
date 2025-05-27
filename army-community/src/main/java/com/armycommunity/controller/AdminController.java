package com.armycommunity.controller;

import ch.qos.logback.core.model.Model;
import com.armycommunity.model.user.UserRole;
import com.armycommunity.security.RequireRole;
import com.armycommunity.service.event.EventService;
import com.armycommunity.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@Data
public class AdminController {

    private final UserService userService;
    private final EventService eventService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Admin dashboard logic
        return "admin/dashboard";
    }

    @RequireRole(UserRole.ADMIN)
    @PostMapping("/users/{id}/promote")
    public String promoteUser(@PathVariable Long id, @RequestParam UserRole role, HttpSession session) {
        Long currentUserId = (Long) session.getAttribute("userId");
        userService.promoteUser(id, role, currentUserId);
        return "redirect:/admin/users";
    }

    @RequireRole(UserRole.MODERATOR)
    @PostMapping("/events/{id}/verify")
    public String verifyEvent(@PathVariable Long id, @RequestParam String notes, HttpSession session) {
        Long currentUserId = (Long) session.getAttribute("userId");
        eventService.verifyEvent(id, currentUserId);
        return "redirect:/admin/events";
    }
}
