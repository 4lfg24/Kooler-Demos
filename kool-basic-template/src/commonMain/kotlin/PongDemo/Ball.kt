package PongDemo

import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.physics.RigidDynamic
import de.fabmax.kool.scene.Mesh
import kotlin.random.Random
import kotlin.random.nextInt

class Ball(var body: RigidDynamic, var mesh: Mesh) {

    var maxVelocity1=10f
    var maxVelocity2=maxVelocity1*2
    var maxVelocity3=maxVelocity1*3



    fun resetPosition(){
        body.position= Vec3f(0f)
        body.linearVelocity= Vec3f.ZERO
        body.maxLinearVelocity=maxVelocity1
    }

    fun shootBall(){
        var impulseY= Random.nextInt(-12..12)
        while(impulseY==0){
            impulseY= Random.nextInt(-12..12)
        }
        val impulse = Vec3f(maxVelocity1, impulseY.toFloat(), 0f) //for testing
        body.addImpulseAtPos(impulse, Vec3f.ZERO)
    }
}