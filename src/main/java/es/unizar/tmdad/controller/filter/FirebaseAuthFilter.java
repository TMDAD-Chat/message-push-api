package es.unizar.tmdad.controller.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@Order(1)
@Slf4j
public class FirebaseAuthFilter implements Filter {

    private final List<String> ignoreForAuthRegex;

    public FirebaseAuthFilter(@Value("${chat.auth.ignore-uri}") String ignoreForAuthRegex, @Value("${chat.auth.separator:;}") String ignoreForAuthRegexSeparator) {
        this.ignoreForAuthRegex = List.of(ignoreForAuthRegex.split(ignoreForAuthRegexSeparator));
        log.info("Ignoring auth for uris: {}", this.ignoreForAuthRegex);
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        if(!shouldIgnoreAuthIn(req)) {
            log.info(
                    "Starting a transaction for req : {}",
                    req.getRequestURI());
            String idToken = req.getHeader("X-Auth-Firebase");
            String user = req.getHeader("X-Auth-User");
            FirebaseToken decodedToken;
            try {
                if (!Objects.nonNull(idToken)) {
                    ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    log.error("Unauthorised token, as it is null");
                    return;
                }

                //Second parameter in verifyIdToken states that revocation must be checked
                decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken, true);
            } catch (FirebaseAuthException e) {
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                log.error("Unauthorised token {}", idToken);
                return;
            }
            String uid = decodedToken.getUid();
            if(!Objects.equals(decodedToken.getEmail(), user)){
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                log.error("Unauthorised token. Header user ({}) does not match token email ({})", user, decodedToken.getEmail());
                return;
            }
            log.info("Logged user {} (email: {})", uid, decodedToken.getEmail());

            chain.doFilter(request, response);
            log.info(
                    "Committing a transaction for req : {}",
                    req.getRequestURI());
        }else{
            chain.doFilter(request, response);
        }

    }

    private boolean shouldIgnoreAuthIn(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        return ignoreForAuthRegex.stream().anyMatch(requestURI::matches);
    }

    // other methods
}
