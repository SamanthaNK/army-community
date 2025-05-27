package com.armycommunity.security;

import com.armycommunity.model.user.User;
import com.armycommunity.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

            if (requireRole != null) {
                HttpSession session = request.getSession(false);
                if (session == null) {
                    response.sendRedirect("/login");
                    return false;
                }

                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                    response.sendRedirect("/login");
                    return false;
                }

                User user = userService.findById(userId);
                if (user == null || !user.hasMinimumRole(requireRole.value())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
                    return false;
                }
            }
        }
        return true;
    }
}
