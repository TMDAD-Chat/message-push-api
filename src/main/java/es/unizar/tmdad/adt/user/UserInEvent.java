package es.unizar.tmdad.adt.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInEvent {

    private String subject; //Either the user id or the room id.
    private EventType event;
    private String argument; //Argument (i.e. user added to room or room added to user)

}
