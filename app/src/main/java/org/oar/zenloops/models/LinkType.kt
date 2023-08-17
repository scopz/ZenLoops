package org.oar.zenloops.models

import java.util.Random

enum class LinkType(
    val connections: Int
) {
    SINGLE(1),
    DOUBLE(2),
    TRIPLE(3),
    TRIPLE_B(3);

    companion object {
        fun findByConnections(connections: Int): LinkType {
            return when(connections) {
                1 -> SINGLE
                2 -> DOUBLE
                3 -> if (Random().nextInt(4) > 0) TRIPLE else TRIPLE_B
                else -> throw RuntimeException("There's no link with $connections connections")
            }
        }
    }
}