package com.example.demo;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.RegisterUser;

@WebServlet("/RegisterUser")

public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        boolean success = RegisterUser.registerUser(username, password);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        if (success) {
            out.println("User registered successfully!");
        } else {
            out.println("Error: Could not register user.");
        }
    }
}
