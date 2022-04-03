package es.unizar.tmdad.adt;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class MessageRequest {

    private String requestId;
    private String recipient;
    private RecipientType recipientType;
    private Date since;

}
