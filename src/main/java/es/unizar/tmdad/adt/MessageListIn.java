package es.unizar.tmdad.adt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageListIn {

    private String requestId;
    private String recipient;
    private RecipientType recipientType;
    private List<MessageIn> messages;

}
