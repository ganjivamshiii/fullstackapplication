package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

// Use Jakarta EE imports for newer Spring versions
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class RegisterController {
    @RequestMapping(value = "/register", method = {RequestMethod.POST, RequestMethod.GET})
    public void registerUser(@RequestParam String username, 
                             @RequestParam String password, 
                             HttpServletResponse response) throws IOException {
        RegisterUser.registerUser(username, password);
        response.sendRedirect("/travelpage/index.html");
    }
}
