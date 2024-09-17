package particles

import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.scene.Mesh
import de.fabmax.kool.util.Time

class BloodParticle(override var mesh: Mesh, override var velocity: Vec3f, override var lifeSpan: Float) :Particle() {

    var currentOpacity=1f

    override fun update() {
        mesh.transform.translate(velocity)
        currentOpacity-=Time.deltaT //NOTE: in the current state of the demo you can see that the particles fade in rather quickly on the first spawn, that is because
        //the scene lags the first time it has to create a large number of meshes, I will fix this in the next release

        (mesh.shader as KslPbrShader).color= particleColors.first.withAlpha(currentOpacity)
    }

}