import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Test the hash
        boolean matches = encoder.matches(password, hash);
        System.out.println("Hash matches: " + matches);
        
        // Test against the existing hash from the database
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jS9.k6ssxY6S";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing hash matches: " + existingMatches);
    }
}