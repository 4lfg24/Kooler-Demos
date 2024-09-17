import de.fabmax.kool.*
import instanced.ParticlesDemo
import particles.ParticleDemo
import template.MenuScreen

/**
 * JVM main function / app entry point: Creates a new KoolContext (with optional platform-specific configuration) and
 * forwards it to the common-code launcher.
 */
fun main(): Unit = KoolApplication(
    config = KoolConfigJvm(
        windowTitle = "kool Template App",

    )
) {
    val lightsDemo= MenuScreen(ctx)
    lightsDemo.launchApp()

}