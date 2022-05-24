package es.unizar.tmdad.repository.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "rooms")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RoomEntity {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "room_users",
            joinColumns = { @JoinColumn(name = "room_id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id") }
    )
    @ToString.Exclude
    Set<UserEntity> users = new HashSet<>();


}
