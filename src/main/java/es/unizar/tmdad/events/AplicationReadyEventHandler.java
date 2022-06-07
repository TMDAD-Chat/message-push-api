package es.unizar.tmdad.events;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import es.unizar.tmdad.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
public class AplicationReadyEventHandler {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Value("${firebase.service-account}")
    private String serviceFile;

    @EventListener
    public void init(ApplicationReadyEvent ignoredEvent) {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream(serviceFile);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(bucketName)
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new StorageException("Failed to open storage.", e);
        }
    }

}
