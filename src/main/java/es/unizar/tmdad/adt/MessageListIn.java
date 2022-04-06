package es.unizar.tmdad.adt;

import lombok.Data;

import java.util.List;

@Data
public class MessageListIn {

    private String requestId;
    private String recipient;
    private RecipientType recipientType;
    private List<MessageIn> messages;

}
