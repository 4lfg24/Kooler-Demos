package template

import de.fabmax.kool.KoolContext
import de.fabmax.kool.scene.scene
import de.fabmax.kool.util.debugOverlay

/**
 * Main application entry. This demo creates a small example scene, which you probably want to replace by your actual
 * game / application content.
 */
fun launchApp(ctx: KoolContext) {
    // add a hello-world demo scene
    ctx.scenes += scene {

    }

    // add the debugOverlay. provides an fps counter and some additional debug info
    ctx.scenes += debugOverlay()
}