package particles

import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.util.Color
import template.DemoWindow

class ParticlesWindow():DemoWindow("Particles Window") {

    var currentVel=1f
    var colorsStrings= mutableListOf("Red/Yellow", "Dark Red/Orange")
    var selectableColors= mutableListOf(Pair(Color.RED, Color.YELLOW), Pair(Color.DARK_RED, Color.ORANGE))
    var currentColors=Pair(Color.RED, Color.YELLOW)

    init {
        windowDockable.setFloatingBounds(width = Dp(300f), height = Dp(400f))
        windowDockable.floatingAlignmentX.set(AlignmentX.End)
        windowDockable.floatingAlignmentY.set(AlignmentY.Center)
    }


    override fun UiScope.windowContent() =Column(Grow.Std, Grow.Std) {
        modifier
            .padding(horizontal = sizes.gap, vertical = sizes.largeGap)

        Row {
            modifier.padding(vertical = Dp(30f))
            Text("Particles velocity") {
                modifier.padding(horizontal = Dp(10f))
            }

            Slider(currentVel, 0.3f, 2f) {
                modifier.margin(sizes.gap)
                modifier.onChange { currentVel = it }
            }
        }

        Row{
            modifier.padding(vertical = Dp(30f))
            Text("Particles colors"){
                modifier.padding(horizontal = Dp(10f))
            }
            ComboBox {
                modifier
                    .width(Grow.Std)
                    .items(colorsStrings)
                    .onItemSelected {
                        currentColors = selectableColors[it]
                    }
                    .selectedIndex(selectableColors.indexOf(currentColors))
            }
        }

    }


}