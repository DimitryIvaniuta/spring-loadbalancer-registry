package com.github.dimitryivaniuta.loadbalancer.service;

import com.github.dimitryivaniuta.loadbalancer.domain.LbCursor;
import com.github.dimitryivaniuta.loadbalancer.repo.LbCursorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbCursorService {

    private final LbCursorRepository repo;

    @Transactional
    public int nextSlot(int modulus) {
        if (modulus <= 0) {
            throw new IllegalArgumentException("modulus must be > 0");
        }

        LbCursor cursor = repo.lockSingleton()
                .orElseGet(() -> repo.save(LbCursor.builder().id((short) 1).nextIndex(0L).build()));

        long current = cursor.getNextIndex();
        cursor.setNextIndex(current + 1);
        repo.save(cursor);

        return Math.floorMod(current, modulus);
    }
}
