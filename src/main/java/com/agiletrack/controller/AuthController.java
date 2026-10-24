package com.agiletrack.controller;
import com.agiletrack.model.UserAccount;
import com.agiletrack.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserRepository users; private final PasswordEncoder encoder;
 public AuthController(UserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
 @PostMapping("/login") public Map<String,Object> login(@RequestBody Map<String,String> body,HttpServletRequest request){
  String email=body.getOrDefault("email","").trim();
  UserAccount u=users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));
  if(!u.active || !encoder.matches(body.getOrDefault("password",""),u.passwordHash))
   throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
  HttpSession session=request.getSession(true); session.setAttribute("userEmail",u.email);session.setAttribute("userRole",u.role);
  return Map.of("id",u.id,"name",u.fullName,"email",u.email,"role",u.role);
 }
 @GetMapping("/me") public Map<String,Object> me(HttpSession session){
  if(session.getAttribute("userEmail")==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  return Map.of("email",session.getAttribute("userEmail"),"role",session.getAttribute("userRole"));
 }
 @PostMapping("/logout") public Map<String,String> logout(HttpServletRequest request){
  HttpSession s=request.getSession(false);if(s!=null)s.invalidate();return Map.of("message","Signed out");
 }
}