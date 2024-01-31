package com.muedsa.snapshot.parser

data class TrackPos(
    var pos: Int = UNSET,
    var line: Int = UNSET,
    var column: Int = UNSET,
) {
    fun hasSetting(): Boolean = pos != UNSET || line != UNSET || column != UNSET

    fun update(pos: Int, line: Int, column: Int) {
        this.pos = pos
        this.line = line
        this.column = column
    }

    fun updateWhenUnset(pos: Int, line: Int, column: Int) {
        if (!hasSetting()) {
            this.pos = pos
            this.line = line
            this.column = column
        }
    }

    fun reset() {
        pos = UNSET
        line = UNSET
        column = UNSET
    }

    override fun toString(): String {
        val p = if (pos == UNSET) "UNSET" else "$pos"
        val l = if (line == UNSET) "UNSET" else "$line"
        val c = if (column == UNSET) "UNSET" else "$column"
        return "Pos[$l:$c]~$p"
    }

    companion object {
        const val UNSET: Int = -1
    }
}
