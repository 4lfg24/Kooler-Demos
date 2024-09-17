package lights

import de.fabmax.kool.KoolContext
import de.fabmax.kool.input.KeyboardInput
import de.fabmax.kool.input.UniversalKeyCode
import de.fabmax.kool.math.*
import de.fabmax.kool.modules.ksl.blocks.ColorBlockConfig
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.physics.*
import de.fabmax.kool.physics.geometry.BoxGeometry
import de.fabmax.kool.physics.geometry.SphereGeometry
import de.fabmax.kool.pipeline.*
import de.fabmax.kool.pipeline.deferred.DeferredPipeline
import de.fabmax.kool.pipeline.deferred.DeferredPipelineConfig
import de.fabmax.kool.pipeline.deferred.DeferredPointLights
import de.fabmax.kool.pipeline.deferred.deferredKslPbrShader
import de.fabmax.kool.pipeline.ibl.EnvironmentHelper
import de.fabmax.kool.scene.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MdColor
import de.fabmax.kool.util.debugOverlay
import Demo
import template.MenuScreen

class LightsDemo(ctx:KoolContext): Demo(ctx) {

    //deferred pipeline, used to create dynamic and moving lights
    lateinit var deferredPipeline: DeferredPipeline
    lateinit var movingLight: DeferredPointLights.PointLight

    lateinit var world: PhysicsWorld
    var stepper = ConstantPhysicsStepperSync()
    lateinit var ball: RigidDynamic

    //for the minimap
    lateinit var off:OffscreenRenderPass2d


    override fun launchApp() {
        val mainScene = scene {
            lighting.clear()
            orbitCamera {
                setZoom(28.0, max = 50.0)
            }
            //LOAD DEFERRED PIPELINE AND PHYSICS
            loadScene(this@scene)
        }

        // add a hello-world demo scene
        ctx.scenes += mainScene

        ctx.scenes += scene {
            setupUiScene()
            //menu button
            addPanelSurface(Colors.neon) {
                modifier.width = Dp(150f)
                modifier.height = Dp(100f)
                modifier.align(AlignmentX.End, AlignmentY.Top)
                //creating the home button
                Button("Back to home") {
                    modifier.align(AlignmentX.Center, AlignmentY.Center)
                    modifier.onClick {
                        ctx.scenes -= mainScene
                        mainScene.release()
                        ctx.scenes -= this@scene
                        release()
                        val menu = MenuScreen(ctx)
                        menu.launchApp()
                    }
                }
            }
            //creating the "mini-map"
                addPanelSurface(colors = Colors.singleColorLight(MdColor.LIGHT_GREEN)) {
                    modifier
                        .size(400.dp, 300.dp)
                        .align(AlignmentX.Start, AlignmentY.Top)
                        .background(RoundRectBackground(colors.background, 16.dp))

                    Image() {
                        modifier.imageProvider(FlatImageProvider(off.colorTexture).mirrorY())
                    }
                }

        }
    }

    fun loadScene(scene: Scene) {
        //loading the physics simulation
        world = PhysicsWorld(scene, true).apply {
            simStepper = stepper
            registerHandlers(scene)
            gravity= Vec3f.ZERO
        }

        //this isn't needed, as it says on Fabmax's demo
        scene.mainRenderPass.clearColor = null

        //initializing the deferred pipeline as shown in Fabmax's demo
        val ibl = EnvironmentHelper.singleColorEnvironment(Color(0.15f, 0.15f, 0.15f)) //this will be the actual clear color for the background
        val defCfg = DeferredPipelineConfig().apply {
            //maxGlobalLights = 1
            isWithAmbientOcclusion = true
            isWithScreenSpaceReflections = false
            isWithImageBasedLighting = false
            isWithBloom = true
            isWithVignette = true
            isWithChromaticAberration = true

            // set output depth compare op to ALWAYS, so that the skybox with maximum depth value is drawn
            outputDepthTest = DepthCompareOp.ALWAYS
        }
        deferredPipeline = DeferredPipeline(scene, defCfg)
        deferredPipeline.apply {
            bloomScale = 1f //I haven't played too much with these values yet, but they should determine the intensity and radius of the bloom effect
            bloomStrength = 3f
            setBloomBrightnessThresholds(0.01f, 0.2f)
            //this.scene.mainRenderPass.clearColor=Color.WHITE
            lightingPassContent += Skybox.cube(ibl.reflectionMap, 1f, hdrOutput = true)
        }

        //creating the light attached to the ball, this light will be reflected on surfaces, not to be confused with the glowing of the orb
        movingLight = deferredPipeline.dynamicPointLights.addPointLight {
            this.position.set(4f, 0f, 0f)
            this.color.set(Color.RED)
            this.radius = 20f
            this.intensity = 100f
        }
        //LOAD BALL AND WALLS
        val sphereMaterial = Material(0f, 0f, 0.5f)
        val sphereShape = SphereGeometry(1f)

        ball = RigidDynamic().apply {
            attachShape(Shape(sphereShape, sphereMaterial))
            position= Vec3f(3f, 5f, 0f)
            createMeshFromBody(this, Color.WHITE)
            scene.onUpdate += {
                //setting the light's position to match the ball's one
                movingLight.position.set(position.mul(1f, MutableVec3f()))
            }
        }
        //giving the ball some simple movement controls
        KeyboardInput.addKeyListener(UniversalKeyCode('w'), "move forward", filter = { it.isPressed }) {
            ball.linearVelocity = Vec3f(0f, 0f, -3f)
        }
        KeyboardInput.addKeyListener(UniversalKeyCode('s'), "move down", filter = { it.isPressed }) {
            ball.linearVelocity = Vec3f(0f, 0f, 3f)
        }
        KeyboardInput.addKeyListener(UniversalKeyCode('a'), "move left", filter = { it.isPressed }) {
            ball.linearVelocity = Vec3f(-3f, 0f, 0f)
        }
        KeyboardInput.addKeyListener(UniversalKeyCode('d'), "move right", filter = { it.isPressed }) {
            ball.linearVelocity = Vec3f(3f, 0f, 0f)
        }
        world.addActor(ball)

        //creating the floor and giving it physics and mesh
        val wallMaterial = Material(0f, 0f, 1f)
        //creating the bottom wall
        val wallBottomGeom = BoxGeometry(Vec3f(50f, 1f, 50f))
        RigidStatic().apply {
            attachShape(Shape(wallBottomGeom, wallMaterial))
            position = Vec3f(0f, -10f, 0f)
            createMeshFromBody(this, Color.BLACK)
            world.addActor(this)
        }

        deferredPipeline.scene.addNode(deferredPipeline.createDefaultOutputQuad())
        //deferredPipeline.scene.addNode(boxModel)

        //MINIMAP INITIALIZATION
        off= OffscreenRenderPass2d(deferredPipeline.scene,
            OffscreenRenderPass.colorAttachmentDefaultDepth(TexFormat.RGBA),
            Vec2i(512, 512),
            name = "render-to-texture").apply {
                //test later
                //camera.position.set(Vec3f(0f, 8f, 0f))
                //camera.lookAt.set(Vec3f.ZERO)
                //camera.up.set(Vec3f.Y_AXIS)

        }
        scene.addOffscreenPass(off)

    }

    //helper function to create meshes from bodies
    fun createMeshFromBody(body: RigidActor, color: Color) {

        deferredPipeline.sceneContent.addColorMesh {
            generate {
                this.color = color
                body.shapes.forEach { shape ->
                    withTransform {
                        transform.mul(shape.localPose)
                        shape.geometry.generateMesh(this)
                    }
                }
            }
            shader = deferredKslPbrShader {

                color { vertexColor() }
                roughness(0.15f)
                emission {
                    instanceColor(Attribute.COLORS)
                    constColor(color, blendMode = ColorBlockConfig.BlendMode.Multiply)
                }
            }
            onUpdate {
                transform.set(body.transform)
            }
        }
    }
}
