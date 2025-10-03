package exam.portal.security_configurarions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);

        // ✅ Production domains only
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:51150",
                "http://localhost:4200"
//                "https://*.exam-portal.com"
        ));

        // ✅ Only needed headers
        corsConfiguration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept"
        ));

        // ✅ Only expose what client needs
        corsConfiguration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));

        // ✅ Allowed methods
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}




