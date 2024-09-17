package PongDemo

import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.util.MdColor
import template.DemoWindow

class ColorsWindow() : DemoWindow("Demo Window") {

    var paddleColorStrings= listOf("Blue", "Orange", "Amber")
    var paddleColorEntries= listOf(MdColor.BLUE, MdColor.ORANGE, MdColor.AMBER)
    var currentPaddleColor=MdColor.BLUE

    var wallsColorStrings= listOf("Indigo", "Lime", "Brown")
    var wallsColorEntries= listOf(MdColor.INDIGO, MdColor.LIME, MdColor.BROWN)
    var currentWallsColor=MdColor.BROWN

    var ballColorStrings= listOf("Red", "Purple", "Green")
    var ballColorEntries= listOf(MdColor.RED, MdColor.PURPLE, MdColor.LIGHT_GREEN)
    var currentBallColor=MdColor.RED

    init {
        windowDockable.setFloatingBounds(width = Dp(300f), height = Dp(400f))
        windowDockable.floatingAlignmentX.set(AlignmentX.End)
    }

    override fun UiScope.windowContent() = Column(Grow.Std, Grow.Std) {

        modifier
            .padding(horizontal = sizes.gap, vertical = sizes.largeGap)
        Row {
            modifier.padding(vertical = Dp(30f))
            Text("Paddles colors"){
                modifier.padding(horizontal = Dp(10f))
            }
            ComboBox {
                modifier
                    .width(Grow.Std)
                    .items(paddleColorStrings)
                    .onItemSelected {
                        currentPaddleColor = paddleColorEntries[it]
                    }
                    .selectedIndex(paddleColorEntries.indexOf(currentPaddleColor))
            }
        }
        Row {
            modifier.padding(vertical = Dp(30f))
            Text("Walls colors"){
                modifier.padding(horizontal = Dp(10f))
            }
            ComboBox {
                modifier
                    .width(Grow.Std)
                    .items(wallsColorStrings)
                    .onItemSelected {
                        currentWallsColor = wallsColorEntries[it]
                    }
                    .selectedIndex(wallsColorEntries.indexOf(currentWallsColor))
            }
        }

        Row {
            modifier.padding(vertical = Dp(30f))
            Text("Ball colors"){
                modifier.padding(horizontal = Dp(10f))
            }
            ComboBox {
                modifier
                    .width(Grow.Std)
                    .items(ballColorStrings)
                    .onItemSelected {
                        currentBallColor = ballColorEntries[it]
                    }
                    .selectedIndex(ballColorEntries.indexOf(currentBallColor))
            }
        }

    }

}