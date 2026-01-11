package com.github.dimitryivaniuta.loadbalancer.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lb_cursor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LbCursor {

    @Id
    private Short id;          // always 1

    @Column(name = "next_index", nullable = false)
    private long nextIndex;
}
