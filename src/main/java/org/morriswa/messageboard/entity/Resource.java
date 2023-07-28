package org.morriswa.messageboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity @Table(name = "post_resource")
@NoArgsConstructor @Builder @Getter
@AllArgsConstructor
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID resourceId;
    private UUID resourceId1;
    private UUID resourceId2;
    private UUID resourceId3;
    private UUID resourceId4;
    private UUID resourceId5;
    private UUID resourceId6;
    private UUID resourceId7;
    private UUID resourceId8;
    private UUID resourceId9;
}
