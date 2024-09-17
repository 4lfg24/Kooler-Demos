package template

import Demo
import PongDemo.PongDemo
import de.fabmax.kool.KoolContext
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.scene.PointMesh
import de.fabmax.kool.scene.scene
import de.fabmax.kool.util.Color
import lights.LightsDemo
import particles.ParticleDemo
import kotlin.coroutines.coroutineContext

class MenuScreen(ctx:KoolContext):Demo(ctx) {

    var lightsDemo=LightsDemo(ctx)
    var pongDemo=PongDemo(ctx)
    var particleDemo=ParticleDemo(ctx)

    override fun launchApp() {

        ctx.scenes+= scene {
            setupUiScene(Color.CYAN)

            addPanelSurface(Colors.singleColorLight(Color.BLUE)){
                modifier.width= Dp(600f)
                modifier.height=Dp(450f)
                modifier.align(AlignmentX.Center, AlignmentY.Center)

                Text("Select a demo") {
                    modifier.align(AlignmentX.Center, AlignmentY.Top)
                    modifier.marginTop=Dp(20f)
                }

                Row {
                    modifier.padding(Dp(20f))

                    Button("Lights demo") {
                        modifier.padding(Dp(100f))
                        modifier.size(Dp(100f), Dp(30f))
                        modifier.align(AlignmentX.Start, AlignmentY.Bottom)
                        modifier.onClick{
                            lightsDemo.launchApp()
                            ctx.scenes-=this@scene
                            //releasing the scene, since we're going to re-initialize it anyway, by doing so we also release every InputHandler associated to it
                            release()
                        }

                    }
                    Button("Pong demo") {
                        modifier.padding(Dp(100f))
                        modifier.size(Dp(100f), Dp(30f))
                        modifier.align(AlignmentX.Center, AlignmentY.Bottom)
                        modifier.onClick{
                            pongDemo.launchApp()
                            ctx.scenes-=this@scene
                            release()
                        }

                    }
                    Button("Particles demo") {
                        modifier.padding(Dp(100f))
                        modifier.size(Dp(100f), Dp(30f))
                        modifier.align(AlignmentX.End, AlignmentY.Bottom)
                        modifier.onClick{
                            particleDemo.launchApp()
                            ctx.scenes-=this@scene
                            release()
                        }

                    }
                }
            }
        }
    }
}