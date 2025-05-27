package com.armycommunity.controller;

import com.armycommunity.dto.request.post.EventRequest;
import com.armycommunity.model.user.UserRole;
import com.armycommunity.security.RequireRole;
import com.armycommunity.service.event.EventService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/events")
@Data
public class EventController {

    private final EventService eventService;

    @RequireRole(UserRole.VERIFIED)
    @PostMapping("/create-verified")
    public String createVerifiedEvent(@ModelAttribute EventRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        eventService.createEvent(userId, request);
        return "redirect:/events";
    }
}
