package com.kaiwa.controller;

import com.kaiwa.model.ChatRoom;
import com.kaiwa.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ViewController {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @GetMapping("/view")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/rooms")
    public String rooms() {
        return "rooms";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/chat/dm/{username}")
    public String dmChat(@PathVariable String username, Model model) {
        model.addAttribute("dmMode", true);
        model.addAttribute("dmUser", username);
        return "chat";
    }

    @GetMapping("/chat/{roomName}")
    public String chat(@PathVariable String roomName, Model model) {

        Optional<ChatRoom> room = chatRoomRepository.findByName(roomName);
        if (room.isPresent()) {
            model.addAttribute("roomId", room.get().getId());
            model.addAttribute("roomName", roomName);
        }
        return "chat";
    }


}
