package com.servletDemo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@WebServlet("/evening")
public class EveningServlet extends HttpServlet {
    private static final String CUSTOM_SESSION_MAP_KEY = "CUSTOM_NAME_SESSION_ID";
    private static final ConcurrentMap<UUID, String> session = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Arrays.stream(Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> cookie.getName().equals(CUSTOM_SESSION_MAP_KEY))
                .findFirst()
                .ifPresentOrElse(cookie -> processRequestWithCookie(cookie, req, resp),
                        () -> processRequestWithoutCookie(req, resp));
    }

    @SneakyThrows
    private void processRequestWithoutCookie(HttpServletRequest req, HttpServletResponse resp) {
        try (var writer = resp.getWriter()) {
            var generatedSessionId = generateUUID();
            var name = req.getParameter("name");
            saveNameIntoSessionMap(name, generatedSessionId);

            var nameFromMap = Optional.ofNullable(session.get(generatedSessionId))
                    .orElseGet(() -> "welcome to the club Buddy");
            writer.printf("Good evening: %s!%n", nameFromMap);
            resp.addCookie(new Cookie(CUSTOM_SESSION_MAP_KEY, generatedSessionId.toString()));
        }
    }

    @SneakyThrows
    private void processRequestWithCookie(Cookie cookie, HttpServletRequest req, HttpServletResponse resp) {
        try (var writer = resp.getWriter()) {
            var customSessionId = UUID.fromString(cookie.getValue());
            var name = req.getParameter("name");
            saveNameIntoSessionMap(name, customSessionId);

            var nameFromMap = Optional.ofNullable(session.get(customSessionId))
                    .orElseGet(() -> "welcome to the club Buddy");
            writer.printf("Good evening: %s!%n", nameFromMap);
        }
    }

    private void saveNameIntoSessionMap(String name, UUID customSessionId) {
        if (name != null && !name.equals("")) {
            session.put(customSessionId, name);
        }
    }

    private UUID generateUUID() {
        while (true) {
            var generatedUUID = UUID.randomUUID();
            if (!session.containsKey(generatedUUID)) {
                return generatedUUID;
            }
        }
    }
}

