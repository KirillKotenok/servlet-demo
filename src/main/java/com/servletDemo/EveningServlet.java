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
    private static final String CUSTOM_SESSION = "CUSTOM_NAME_SESSION_ID";
    private static final String NAME_PARAMETER = "name";
    private static final String DEFAULT = "welcome to the club Buddy";
    private static final String PRINT_MESSAGE_TEMPLATE = "Good evening: %s!%n";
    private static final ConcurrentMap<UUID, String> session = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Arrays.stream(Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> cookie.getName().equals(CUSTOM_SESSION))
                .findFirst()
                .ifPresentOrElse(cookie -> processRequestWithCookie(cookie, req, resp),
                        () -> processRequestWithoutCookie(req, resp));
    }

    @SneakyThrows
    private void processRequestWithoutCookie(HttpServletRequest req, HttpServletResponse resp) {
        try (var writer = resp.getWriter()) {
            var generatedSessionId = generateUUID();
            var name = req.getParameter(NAME_PARAMETER);
            saveNameIntoSessionMap(name, generatedSessionId);

            var nameFromMap = Optional.ofNullable(session.get(generatedSessionId))
                    .orElseGet(() -> DEFAULT);
            writer.printf(PRINT_MESSAGE_TEMPLATE, nameFromMap);
            resp.addCookie(new Cookie(CUSTOM_SESSION, generatedSessionId.toString()));
        }
    }

    @SneakyThrows
    private void processRequestWithCookie(Cookie cookie, HttpServletRequest req, HttpServletResponse resp) {
        try (var writer = resp.getWriter()) {
            var customSessionId = UUID.fromString(cookie.getValue());
            var name = req.getParameter(NAME_PARAMETER);
            saveNameIntoSessionMap(name, customSessionId);

            var nameFromMap = Optional.ofNullable(session.get(customSessionId))
                    .orElseGet(() -> DEFAULT);
            writer.printf(PRINT_MESSAGE_TEMPLATE, nameFromMap);
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

