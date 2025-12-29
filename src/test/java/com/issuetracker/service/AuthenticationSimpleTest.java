package com.issuetracker.service;

import com.issuetracker.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de autenticación simplificado que funciona con H2.
 * Este test demuestra que la lógica de autenticación funciona correctamente.
 * 
 * Para ejecutar: mvn test -Dtest="AuthenticationSimpleTest"
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationSimpleTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void authenticationWorkflow_ShouldWorkCorrectly() {
        // Datos de prueba
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";
        
        // 1. Registrar usuario
        User user = userService.registerUser(email, password, name);
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        
        // 2. Cargar detalles del usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        
        // 3. Generar token JWT
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // 4. Validar token
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        
        // 5. Extraer información del token
        assertThat(jwtService.extractUsername(token)).isEqualTo(email);
        
        // 6. Validar credenciales
        assertThat(userService.validateCredentials(email, password)).isTrue();
        assertThat(userService.validateCredentials(email, "wrongpassword")).isFalse();
        
        System.out.println("✅ Test de autenticación completado exitosamente");
        System.out.println("📧 Usuario: " + email);
        System.out.println("🔑 Token generado: " + token.substring(0, 20) + "...");
    }

    @Test
    void multipleUsers_ShouldWorkIndependently() {
        // Usuario 1
        String email1 = "user1@test.com";
        String password1 = "password123";
        User user1 = userService.registerUser(email1, password1, "User One");
        
        // Usuario 2
        String email2 = "user2@test.com";
        String password2 = "password456";
        User user2 = userService.registerUser(email2, password2, "User Two");
        
        // Verificar que son diferentes
        assertThat(user1.getId()).isNotEqualTo(user2.getId());
        assertThat(user1.getEmail()).isNotEqualTo(user2.getEmail());
        
        // Generar tokens independientes
        UserDetails userDetails1 = userDetailsService.loadUserByUsername(email1);
        UserDetails userDetails2 = userDetailsService.loadUserByUsername(email2);
        
        String token1 = jwtService.generateToken(userDetails1);
        String token2 = jwtService.generateToken(userDetails2);
        
        // Los tokens deben ser diferentes
        assertThat(token1).isNotEqualTo(token2);
        
        // Cada token debe ser válido solo para su usuario
        assertThat(jwtService.isTokenValid(token1, userDetails1)).isTrue();
        assertThat(jwtService.isTokenValid(token2, userDetails2)).isTrue();
        assertThat(jwtService.isTokenValid(token1, userDetails2)).isFalse();
        assertThat(jwtService.isTokenValid(token2, userDetails1)).isFalse();
        
        System.out.println("✅ Test de múltiples usuarios completado exitosamente");
    }

    @Test
    void passwordEncryption_ShouldWorkCorrectly() {
        String plainPassword = "mySecretPassword123";
        String email = "encryption@test.com";
        
        // Registrar usuario
        User user = userService.registerUser(email, plainPassword, "Encryption Test");
        
        // La contraseña almacenada debe estar encriptada
        assertThat(user.getPasswordHash()).isNotEqualTo(plainPassword);
        assertThat(passwordEncoder.matches(plainPassword, user.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", user.getPasswordHash())).isFalse();
        
        System.out.println("✅ Test de encriptación de contraseñas completado exitosamente");
    }
}