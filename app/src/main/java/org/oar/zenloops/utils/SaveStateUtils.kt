package org.oar.zenloops.utils

import android.content.Context
import android.content.ContextWrapper
import org.json.JSONException
import org.json.JSONObject
import org.oar.zenloops.core.Matrix
import org.oar.zenloops.elements.Grid
import org.oar.zenloops.elements.Link
import org.oar.zenloops.elements.LinkGraphics
import org.oar.zenloops.elements.LinkPosition
import org.oar.zenloops.grid.GridPosition
import org.oar.zenloops.models.LinkType
import org.oar.zenloops.models.NeighborLink
import org.oar.zenloops.models.Point
import org.oar.zenloops.models.Position
import org.oar.zenloops.ui.views.GameView
import org.oar.zenloops.utils.GridUtils.linksFromPack
import org.oar.zenloops.utils.GridUtils.toPack
import java.io.*
import kotlin.math.roundToInt

object SaveStateUtils {

    private val Context.statePath: String
        get() = ContextWrapper(this).filesDir.path + "/state.save"

    fun Context.saveState(pack: String) {

        try {
            PrintWriter(statePath).use {
                it.print(pack)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun Context.deleteState() {
        File(statePath).delete()
    }

    fun Context.hasState(): Boolean {
        return File(statePath).exists()
    }

    fun Context.loadState(): String? {
        val statePath = statePath

        if (File(statePath).exists()) {
            try {
                val line = BufferedReader(FileReader(statePath)).use {
                    it.readLine()
                }

                return line
            } catch (e: JSONException) {
                System.err.println("Couldn't load \"$statePath\"")
            } catch (e: IOException) {
                System.err.println("Couldn't load \"$statePath\"")
            }
        }
        return null
    }
}