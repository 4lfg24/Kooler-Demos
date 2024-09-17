package particles

import Demo
import de.fabmax.kool.KoolContext
import de.fabmax.kool.math.MutableVec3f
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.modules.ui2.docking.Dock
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.scene.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.ObjectPool
import de.fabmax.kool.util.Time
import de.fabmax.kool.util.debugOverlay
import template.MenuScreen
import kotlin.random.Random


var particleColors=Pair(Color.RED, Color.YELLOW)


class ParticleDemo(ctx:KoolContext):Demo(ctx) {

    lateinit var bloodParticleSpawner: BloodParticleSpawner

    //I removed the ui window for this demo, I'll add it when I'll make the particle system more complex
    //val dock= Dock()
    //lateinit var particlesWindow: ParticlesWindow

    var particlesVel=1f

    override fun launchApp() {

        val mainScene= scene {
            defaultOrbitCamera()

            bloodParticleSpawner=BloodParticleSpawner(this, 3f, 100, MutableVec3f(0f)){
                //passing it a function that creates a sphere Mesh
                Mesh(Attribute.POSITIONS, Attribute.NORMALS, Attribute.COLORS).apply {
                    generate {
                        icoSphere {
                            radius=1f
                        }
                    }
                    shader=KslPbrShader{
                        color {
                            uniformColor(particleColors.first)
                        }
                    }
                }
            }

            onUpdate+={
                bloodParticleSpawner.update()
            }
        }

        ctx.scenes+=mainScene

        ctx.scenes+= scene {
            setupUiScene()
            //creating the home button
            addPanelSurface(Colors.neon) {
                modifier.width= Dp(150f)
                modifier.height= Dp(100f)
                modifier.align(AlignmentX.Start, AlignmentY.Top)

                Button("Back to home"){
                    modifier.align(AlignmentX.Center, AlignmentY.Center)
                    modifier.onClick{
                        ctx.scenes-=mainScene
                        ctx.scenes-=this@scene
                        release()
                        val menu= MenuScreen(ctx)
                        menu.launchApp()
                    }
                }
            }
            //ui section, work in progress
            /*
            dock.dockingSurface.colors= Colors.darkColors()
            addNode(dock)

            particlesWindow= ParticlesWindow()
            dock.addDockableSurface(particlesWindow.windowDockable, particlesWindow.windowSurface)

            onUpdate+={
                //put the logic here
                particlesVel=particlesWindow.currentVel
                particleColors=particlesWindow.currentColors
            }

             */

        }
        ctx.scenes+= debugOverlay()
    }
}

//my implementation of a generic particle spawner class, it takes a type parameter of type Particle, arguments defining its spawn-rate and spawn-number, and a function that creates the mesh of the given
//particles
open class ParticleSpawner<P:Particle>(var scene: Scene, var spawnRate:Float, var spawnNumber:Int, var spawnPosition:MutableVec3f,private var factory: ()->Mesh){

    var activeParticles= mutableListOf<P>() //these are the particles that will be updated
    var dyingParticles= mutableListOf<P>() //these are the particles to be removed
    var maxSpawnRate=spawnRate
    //I'm using pooling to increase efficiency
    var meshesPool:ObjectPool<Mesh>

    init {
        meshesPool=ObjectPool {
            factory()
        }
    }

    open fun update(){

        for(particle in activeParticles){
            particle.update()
            particle.lifeSpan-=Time.deltaT //lifespan is calculated in seconds
            //if the lifespan of a particle expires, schedule its removal by adding it to the dying particles
            if(particle.lifeSpan<=0) dyingParticles.add(particle)
        }
        activeParticles.removeAll(dyingParticles)
        //remove dying particles from the scene
        for (deadParticle in dyingParticles){
            scene.removeNode(deadParticle.mesh)
            //DO NOT release the mesh associated to the particle
            meshesPool.recycle(deadParticle.mesh)
        }
        dyingParticles.clear()

    }

    open fun spawnParticles(){
        //to be implemented by its subclasses
    }
}

class BloodParticleSpawner(scene: Scene, spawnRate:Float, spawnNumber:Int,spawnPosition: MutableVec3f, mesh: () -> Mesh):ParticleSpawner<BloodParticle>(scene, spawnRate, spawnNumber, spawnPosition,mesh){


    init {

    }

    override fun update() {
        spawnRate-=Time.deltaT
        if (spawnRate<=0){
            spawnParticles()
            spawnRate=maxSpawnRate
        }
        super.update()

    }

    override fun spawnParticles() {

        for (i in 0 until spawnNumber){
            //create a blood particle and give it a random velocity
            val mesh=meshesPool.get()
            mesh.transform.setIdentity().translate(spawnPosition)
            //give each particle a random velocity
            val vel=MutableVec3f(Random.nextFloat()-1,Random.nextFloat()-1,Random.nextFloat()-1)
            val bloodParticle=BloodParticle(mesh,vel, 1.5f)
            activeParticles.add(bloodParticle)
            scene.addNode(mesh)
        }
    }

}

abstract class Particle(){

    abstract var mesh: Mesh
    abstract var velocity:Vec3f
    abstract var lifeSpan:Float

    open fun update(){

    }

}