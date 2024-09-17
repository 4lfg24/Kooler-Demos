package instanced

import Demo
import de.fabmax.kool.KoolContext
import de.fabmax.kool.math.Mat4f
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.randomF
import de.fabmax.kool.modules.ksl.KslUnlitShader
import de.fabmax.kool.modules.ksl.blocks.ColorSpaceConversion
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.scene.*
import de.fabmax.kool.util.MdColor
import de.fabmax.kool.util.Time
import kotlin.random.Random

class ParticlesDemo(ctx:KoolContext):Demo(ctx) {

    val objects = mutableListOf<Mesh>()
    val particleInstances = mutableListOf<Particle>()

    private val instancedShader = KslUnlitShader {
        vertices {
            isInstanced = true
        }
        color { vertexColor() }
        colorSpaceConversion = ColorSpaceConversion.AS_IS
    }

    //timer to test the instancing logic
    var timer=2.0f


    override fun launchApp() {

        ctx.scenes+= scene {
            defaultOrbitCamera()
            populateScene(this)

            onUpdate{

                timer-=Time.deltaT
                if(timer<=0){
                    resetScene(this)
                    populateScene(this)
                    timer=2.0f
                }
            }
        }
    }

    fun resetScene(scene:Scene){
        for(i in objects.indices.reversed()){
            scene.removeNode(objects[i])
        }
        objects.forEach { it.release() }
        objects.clear()

    }

    fun populateScene(scene: Scene){
        var instances: MeshInstanceList?
        instances= MeshInstanceList(200, Attribute.INSTANCE_MODEL_MAT)
        particleInstances.clear()

        //you add the object to repeat once and pass it the MeshInstanceList
        objects+=scene.addColorMesh(instances=instances) {
            generate {
                color=MdColor.LIME
                cube { size.set(0.3f, 0.3f, 0.3f) }
            }
            shader=instancedShader
        }
        //solution given by Fabmax, for now updating each instance separately is impossible
        /*for(mesh in objects){
            mesh.onUpdate{
                instances.clear()

                instances.addInstances(particleInstances.size) { instanceBuffer ->
                    for (particle in particleInstances) {
                        particle.pose.translate(particle.randomVel, MutableMat4f())
                        particle.pose.putTo(instanceBuffer)
                    }
                }
            }
        }

         */

        for(i in 0 until 100){
            val x=Random.nextInt(-25, 25)
            val y=Random.nextInt(-25, 25)
            val z=Random.nextInt(-25, 25)



            particleInstances += Particle(instances, Mat4f.translation(x.toFloat(), y.toFloat(), z.toFloat())).apply { addInstance() }
        }

    }
}

class Particle(val instances: MeshInstanceList, val pose: Mat4f) {

    var randomVel= Vec3f(Random.randomF()*100, Random.randomF()*100,Random.randomF()*100)

    fun addInstance() {
        //think of it like this, the MeshInstanceList has a certain mesh that it uses as the "model"
        //everytime you want ot create a copy of that model you do .addInstance() and specify it's location
        instances.addInstance {
            pose.putTo(this)
        }
    }
}