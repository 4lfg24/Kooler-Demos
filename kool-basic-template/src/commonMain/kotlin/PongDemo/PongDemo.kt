package PongDemo

import Demo
import de.fabmax.kool.KoolContext
import de.fabmax.kool.input.KeyboardInput
import de.fabmax.kool.input.UniversalKeyCode
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.modules.ui2.docking.Dock
import de.fabmax.kool.physics.*
import de.fabmax.kool.physics.geometry.BoxGeometry
import de.fabmax.kool.physics.geometry.SphereGeometry
import de.fabmax.kool.scene.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MdColor
import de.fabmax.kool.util.MsdfFont
import template.MenuScreen
import kotlin.random.Random
import kotlin.random.nextInt

class PongDemo(ctx:KoolContext):Demo(ctx) {
    lateinit var world: PhysicsWorld
    var stepper = ConstantPhysicsStepperSync()

    lateinit var paddle1:PongPaddle
    lateinit var paddle2:PongPaddle
    lateinit var ball:Ball

    //class responsible to handle the game's logic (such as keeping the score increasing the ball's speed ecc.)
    lateinit var gameLogic:GameLogic

    //sizes for the walls delimiting the game area
    var wallDimension = 100f

    //variables to initialize the ui window
    val dock= Dock()
    lateinit var debugWindow: ColorsWindow

    override fun launchApp() {

        val gameScene= scene {
            camera.apply {
                position.set(0f, 0f, 150f)
                clipNear = 0.5f
                clipFar = 500f
                lookAt.set(Vec3f(0f, 0f, 0f))
            }

            loadPhysicsWorld()

            loadPaddles()
            loadWalls()
            loadBall()

            gameLogic= GameLogic(ball, paddle1, paddle2)

            onUpdate{
                gameLogic.update()
            }
        }
        ctx.scenes+= gameScene
        //adding ui
        ctx.scenes+= scene {
            setupUiScene()
            //creating the home button, responsible for releasing the scene
            addPanelSurface(Colors.neon) {
                modifier.width= Dp(150f)
                modifier.height=Dp(100f)
                modifier.align(AlignmentX.Start, AlignmentY.Top)

                Button("Back to home"){
                    modifier.align(AlignmentX.Center, AlignmentY.Center)
                    modifier.onClick{
                        ctx.scenes-=gameScene
                        gameScene.release()
                        ctx.scenes-=this@scene
                        release()
                        val menu=MenuScreen(ctx)
                        menu.launchApp()
                    }
                }
            }

            //creating the dock for the ui window
            dock.dockingSurface.colors= Colors.darkColors()
            addNode(dock)
            //this window will allow you to customize the game objects' color
            debugWindow= ColorsWindow()
            dock.addDockableSurface(debugWindow.windowDockable, debugWindow.windowSurface)

            onUpdate+={
                //this is one way to change the color of a Mesh if it's created with a KslPbrShader (there are probably different approaches to this, but for now this works fine)
                (paddle1.mesh.shader as KslPbrShader).color=debugWindow.currentPaddleColor
                (paddle2.mesh.shader as KslPbrShader).color=debugWindow.currentPaddleColor
                (ball.mesh.shader as KslPbrShader).color=debugWindow.currentBallColor

            }

            //ui for displaying the score
            addPanelSurface(colors = Colors.singleColorDark(MdColor.BLUE)) {

                this.modifier
                    .size(300.dp, 100.dp)
                    .align(AlignmentX.Center, AlignmentY.Top)
                    .margin(top = 40.dp) //how distant from the top of its alignment is
                    .padding(top = 40.dp) //how much everything inside it is distant from the top

                //we initialize a Msdf font, which allows for great personalized
                //text, we will use this to draw the score
                val font = MsdfFont(
                    sizePts = 45f,
                    glowColor = this.colors.secondary.withAlpha(0.5f)
                )

                var player1Text by remember(0)
                var player2Text by remember(0)

                Text("$player1Text - $player2Text") {
                    modifier.backgroundColor(MdColor.BLUE_GREY)
                    modifier.alignX(AlignmentX.Center)
                    modifier.textAlign(AlignmentX.Center, AlignmentY.Center)
                    modifier.font(font)
                    modifier.size(250.dp, 80.dp)
                    modifier.textColor(MdColor.LIGHT_GREEN)
                    onUpdate {
                        //we update the value of the player's score so that
                        // it gets reflected inside the ui
                        player1Text = gameLogic.player1Score
                        player2Text = gameLogic.player2Score
                    }
                }
            }
        }

    }

    fun Scene.loadPhysicsWorld() {
        //we initialize the physic world, it will be responsible for simulating
        //and updating the physics actors in it
        world = PhysicsWorld(this).apply{
            registerHandlers(this@loadPhysicsWorld)
            simStepper = stepper
            //for this game we don't need any gravity
            gravity = Vec3f.ZERO
        }

        world.registerContactListener(object : ContactListener {
            override fun onTouchFound(actorA: RigidActor, actorB: RigidActor, contactPoints: List<ContactPoint>?) {
                //simple check between the two colliding actors to see if they're the ball and the left/right wall
                if (actorA.tags.hasTag("left wall") && actorB.tags.hasTag("ball")){
                    gameLogic.player2Score+=1
                    gameLogic.respawn=true
                }
                if (actorB.tags.hasTag("left wall") && actorA.tags.hasTag("ball")){
                    gameLogic.player2Score+=1
                    gameLogic.respawn=true
                }
                if (actorA.tags.hasTag("right wall") && actorB.tags.hasTag("ball")){
                    gameLogic.player1Score+=1
                    gameLogic.respawn=true
                }
                if (actorB.tags.hasTag("right wall") && actorA.tags.hasTag("ball")){
                    gameLogic.player1Score+=1
                    gameLogic.respawn=true
                }

                if (actorA.tags.hasTag("ball") && actorB.tags.hasTag("left paddle") || actorB.tags.hasTag("right paddle")){
                    gameLogic.numberOfBounces+=1
                }
                if (actorB.tags.hasTag("ball") && actorA.tags.hasTag("left paddle") || actorA.tags.hasTag("right paddle")){
                    gameLogic.numberOfBounces+=1
                }
            }

            override fun onTouchLost(actorA: RigidActor, actorB: RigidActor) {
                super.onTouchLost(actorA, actorB)
            }
        })
    }

    fun Scene.loadPaddles(){
        val material = Material(0.5f, 0.5f, 1f)
        val paddleGeom = BoxGeometry(Vec3f(3f, 10f, 5f))

        val paddle1Body = RigidDynamic(isKinematic = true).apply {
            attachShape(Shape(paddleGeom, material))
            position = Vec3f(-40f, 0f, 0f)
            tags.put("paddle left", this)
        }
        //we create the mesh for the paddle
        val paddleMesh = createMeshFromBody(paddle1Body, MdColor.BLUE, this)

        paddle1 = PongPaddle(paddle1Body, paddleMesh, UniversalKeyCode('w'), UniversalKeyCode('s'))

        //same thing for the second paddle
        val paddle2Body= RigidDynamic(isKinematic = true).apply {
            attachShape(Shape(paddleGeom, material))
            position = Vec3f(40f, 0f, 0f)
            tags.put("paddle right", this)
        }
        val paddle2Mesh = createMeshFromBody(paddle2Body, MdColor.BLUE, this)
        paddle2= PongPaddle(paddle2Body, paddle2Mesh, KeyboardInput.KEY_CURSOR_UP, KeyboardInput.KEY_CURSOR_DOWN)
        //we add the two paddle's physic actors to the world, so that they will
        //be updated every frame
        world.addActor(paddle1Body)
        world.addActor(paddle2Body)
    }

    fun Scene.loadWalls() {
        //the material is the same for all the walls
        val wallMaterial = Material(0f, 0f, 1f)
        //creating the bottom wall
        val wallBottomGeom = BoxGeometry(Vec3f(wallDimension, 10f, 10f))
        val wallBottom = RigidStatic().apply {
            attachShape(Shape(wallBottomGeom, wallMaterial))
            position = Vec3f(0f, -50f, 0f)
            world.addActor(this)
            tags.put("bottom wall", this)
        }


        //creating the left wall
        val wallLeftGeom = BoxGeometry(Vec3f(10f, wallDimension, 10f))
        val wallLeft = RigidStatic().apply {
            attachShape(Shape(wallLeftGeom, wallMaterial))
            position = Vec3f(-50f, 0f, 0f)
            world.addActor(this)
            tags.put("left wall",this)
        }
        //creating the top wall
        val wallTopGeom = BoxGeometry(Vec3f(wallDimension, 10f, 10f))
        val wallTop = RigidStatic().apply {
            attachShape(Shape(wallTopGeom, wallMaterial))
            position = Vec3f(0f, 50f, 0f)
            world.addActor(this)
            tags.put("top wall", this)
        }
        //creating the right wall
        val wallRightGeom = BoxGeometry(Vec3f(10f, wallDimension, 10f))
        val wallRight = RigidStatic().apply {
            attachShape(Shape(wallRightGeom, wallMaterial))
            position = Vec3f(50f, 0f, 0f)
            world.addActor(this)
            tags.put("right wall",this)
        }

        //generating the meshes for the walls, I'm going to include the color-changing logic directly here because i don't want to have a reference to each wall
        //bottom wall
        createMeshFromBody(wallBottom, MdColor.GREEN, this).apply {
            onUpdate{
                (shader as KslPbrShader).color=debugWindow.currentWallsColor
            }
        }
        //left wall
        createMeshFromBody(wallLeft, MdColor.GREEN, this).apply {
            onUpdate{
                (shader as KslPbrShader).color=debugWindow.currentWallsColor
            }
        }
        //top wall
        createMeshFromBody(wallTop, MdColor.GREEN, this).apply {
            onUpdate{
                (shader as KslPbrShader).color=debugWindow.currentWallsColor
            }
        }
        //right wall
        createMeshFromBody(wallRight, MdColor.GREEN, this).apply {
            onUpdate{
                (shader as KslPbrShader).color=debugWindow.currentWallsColor
            }
        }
    }

    fun Scene.loadBall() {
        //creating the ball physic actor
        val sphereMaterial = Material(0f, 0f, 1.5f)
        val sphereShape = SphereGeometry(1f)
        val ballBody = RigidDynamic().apply {
            attachShape(Shape(sphereShape, sphereMaterial))
            position= Vec3f.ZERO
            tags.put("ball",this)
            simulationFilterData= FilterData {
                setCollisionGroup(0) //aka "what am I?"
                setCollidesWithEverything() //aka "what do I collide with"

                word2=Physics.NOTIFY_TOUCH_FOUND or Physics.NOTIFY_TOUCH_LOST //IMPORTANT: this flag has to be set, otherwise the contact listener won't react to this actor
            }
        }
        world.addActor(ballBody)
        //after it's been created, we give the ball a random velocity to start
        //the game
        var impulseY= Random.nextInt(-12..12)
        while(impulseY==0){
            impulseY= Random.nextInt(-12..12)
        }
        val impulse = Vec3f(12f, impulseY.toFloat(), 0f)
        ballBody.addImpulseAtPos(impulse, Vec3f.ZERO)

        //creating the mesh for the ball
        val mesh=createMeshFromBody(ballBody, MdColor.RED, this)

        ball=Ball(ballBody, mesh)
    }

    //helper function to create meshes out of rigid actors
    fun createMeshFromBody(body: RigidActor, color: Color, scene: Scene): Mesh {
        return scene.addColorMesh {
            generate {
                body.shapes.forEach { shape ->
                    withTransform {
                        transform.mul(shape.localPose)
                        shape.geometry.generateMesh(this)
                    }
                }
            }
            shader = KslPbrShader {
                color {
                    uniformColor(color)
                }
            }

            onUpdate {
                transform.set(body.transform)
            }
        }
    }
}

