package es.unizar.tmdad.adt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageIn {

    private Long id;
    private MessageType messageType;
    private String content;
    private String sender;
    private String creationTimestamp;

}
