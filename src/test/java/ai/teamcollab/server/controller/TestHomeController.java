package ai.teamcollab.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestHomeController {
    
    @GetMapping("/")
    public String home() {
        return "layout/base";
    }
}