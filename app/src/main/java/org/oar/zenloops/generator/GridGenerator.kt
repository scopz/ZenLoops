package org.oar.zenloops.generator

import org.oar.zenloops.elements.Grid

interface GridGenerator {
    fun generateNewGrid(grid: Grid)
}