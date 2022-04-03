package es.unizar.tmdad.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "message_timestamps")
@IdClass(MessageTimestampEntity.MessageTimestampCompositeKey.class)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageTimestampEntity {

    @Id
    private String topic;
    @Id
    private String username;

    @Column
    private Date messageTimestamp;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class MessageTimestampCompositeKey implements Serializable {
        private String topic;
        private String username;
    }
}
