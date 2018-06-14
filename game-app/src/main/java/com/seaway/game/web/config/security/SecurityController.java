package com.seaway.game.web.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;

@Slf4j
@Controller
public class SecurityController {

    @RequestMapping(value = "/signin", method = RequestMethod.GET)
    public String signIn(HttpServletRequest request, HttpServletResponse response) {
        String referer = request.getHeader("referer");
        log.info("Returning signin page, referer: {}", referer);

        if (referer != null) {
            response.setHeader("AUTH-REQUIRED", "true");
        }

        return "game.signin";
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(HttpServletRequest request, Authentication authentication, Principal principal) {
        log.info("SecurityController ----------> index");

        if (!authentication.isAuthenticated()) {
            log.warn("No authentication");
            return "signin?no-auth";
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("No session");
            return "signin?no-session";
        }

        return "game.index";
    }

    @RequestMapping(value = "/lockscreen", method = RequestMethod.GET)
    public String lockScreen(HttpServletRequest request, Authentication authentication) {
        log.info("SecurityController ----------> lockscreen");

        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("No session");
            return signOut();
        }

        authentication.setAuthenticated(false);

        return "game.lockscreen";
    }

    @RequestMapping(value = "/signout", method = RequestMethod.POST)
    public String signOut() {
        log.info("SecurityController ----------> signout");

        return "signin?signout";
    }

    @RequestMapping(value = "/access-denied", method = RequestMethod.GET)
    public String accessDenied() {
        log.info("SecurityController ----------> Access Denied");

        return "signin?access-denied";
    }

    @RequestMapping(value = "/invalid-session", method = RequestMethod.GET)
    public String invalidSession() {
        log.info("SecurityController ----------> Invalid Session");

        return "signin?invalid-session";
    }
}
