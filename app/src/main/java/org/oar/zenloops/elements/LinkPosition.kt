package org.oar.zenloops.elements

enum class LinkPosition {
    TOP,
    TOP_RIGHT,
    BOT_RIGHT,
    BOT,
    BOT_LEFT,
    TOP_LEFT;

    lateinit var next: LinkPosition
        private set

    lateinit var oposite: LinkPosition
        private set

    companion object {
        init {
            TOP.next = BOT_RIGHT
            BOT_RIGHT.next = BOT_LEFT
            BOT_LEFT.next = TOP

            BOT.next = TOP_LEFT
            TOP_LEFT.next = TOP_RIGHT
            TOP_RIGHT.next = BOT


            TOP.oposite = BOT
            BOT.oposite = TOP

            TOP_RIGHT.oposite = BOT_LEFT
            BOT_LEFT.oposite = TOP_RIGHT

            BOT_RIGHT.oposite = TOP_LEFT
            TOP_LEFT.oposite = BOT_RIGHT
        }
    }
}

