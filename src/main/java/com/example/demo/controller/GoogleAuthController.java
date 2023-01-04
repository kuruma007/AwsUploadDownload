package com.example.demo.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GoogleAuthController {
    @RequestMapping("/")
    public String user(OAuth2AuthenticationToken oAuth2AuthenticationToken){
       oAuth2AuthenticationToken.getPrincipal().getAttributes();
       return "upload";
    }
}
