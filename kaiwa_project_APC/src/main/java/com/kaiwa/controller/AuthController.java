package com.kaiwa.controller;

import com.kaiwa.model.User;
import com.kaiwa.repository.UserRepository;
import com.kaiwa.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    // ------------------------------------------------------------------
    // Original username/password registration (kept intact)
    // ------------------------------------------------------------------
    @PostMapping("/register")
    public String register(@Valid User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "redirect:/register?error";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of("ROLE_USER"));
        userRepository.save(user);
        return "redirect:/login";
    }

    // ------------------------------------------------------------------
    // Email OTP registration flow
    // ------------------------------------------------------------------
    @GetMapping("/otp/register")
    public String otpRegister() {
        return "otp-register";
    }

    @PostMapping("/otp/send")
    public String otpSend(@RequestParam String email, Model model) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "otp-register";
        }
        if (userRepository.existsByUsername(email)) {
            // Reusing the email as a username is the simplest mapping here;
            // avoid leaking "user exists" in a real app.
            model.addAttribute("error", "An account with that email already exists.");
            return "otp-register";
        }
        String code = otpService.sendCode(email);
        otpService.printCode(email, code);
        model.addAttribute("email", email);
        return "otp-verify";
    }

    @PostMapping("/otp/verify")
    public String otpVerify(@RequestParam String email,
                            @RequestParam String code,
                            @RequestParam String username,
                            @RequestParam String password,
                            Model model) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            model.addAttribute("error", "Invalid email.");
            model.addAttribute("email", email);
            return "otp-verify";
        }
        String normalizedCode = code.replaceAll("\\s+", "");
        otpService.verify(email, normalizedCode).orElseGet(() -> {
            model.addAttribute("error", "Invalid or expired verification code. Try again or resend.");
            model.addAttribute("email", email);
            return null;
        });

        // Verification succeeded — create the account.
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of("ROLE_USER"));
        user.setVerified(true);
        userRepository.save(user);

        // Log the user in immediately (OTP is the proof).
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, Set.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return "redirect:/home";
    }

    @PostMapping("/otp/resend")
    public String otpResend(@RequestParam String email, Model model) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            model.addAttribute("error", "Invalid email.");
            return "otp-verify";
        }
        otpService.clear(email);
        String code = otpService.sendCode(email);
        otpService.printCode(email, code);
        model.addAttribute("email", email);
        return "otp-verify";
    }

    // ------------------------------------------------------------------
    // Email OTP login flow (email -> send -> verify -> login)
    // ------------------------------------------------------------------
    @GetMapping("/otp/login")
    public String otpLogin() {
        return "otp-login";
    }

    @PostMapping("/otp/login")
    public String otpLoginAction(@RequestParam String email, Model model) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "otp-login";
        }
        if (!userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "No account found with that email.");
            return "otp-login";
        }
        String code = otpService.sendCode(email);
        otpService.printCode(email, code);
        model.addAttribute("email", email);
        return "otp-verify-login";
    }

    @PostMapping("/otp/confirm-login")
    public String otpConfirmLogin(@RequestParam String email,
                                  @RequestParam String code,
                                  Model model) {
        String normalizedCode = code.replaceAll("\\s+", "");
        otpService.verify(email, normalizedCode).orElseGet(() -> {
            model.addAttribute("error", "Invalid or expired code. Try again or resend.");
            model.addAttribute("email", email);
            return null;
        });
        User user = userRepository.findByEmail(email).orElseThrow();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, user.getRoles().stream().map(AuthController::toAuthority).toList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return "redirect:/home";
    }

    @PostMapping("/otp/resend-login")
    public String otpResendLogin(@RequestParam String email, Model model) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            model.addAttribute("error", "Invalid email.");
            return "otp-verify-login";
        }
        otpService.clear(email);
        String code = otpService.sendCode(email);
        otpService.printCode(email, code);
        model.addAttribute("email", email);
        return "otp-verify-login";
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private static SimpleGrantedAuthority toAuthority(String role) {
        return new SimpleGrantedAuthority(role);
    }
}
