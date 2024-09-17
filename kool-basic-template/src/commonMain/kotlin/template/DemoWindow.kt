package template

import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.modules.ui2.docking.UiDockable

//Reusable Ui window class, shamelessly copied by Fabmax's demo 😅
abstract class DemoWindow(name: String, isClosable: Boolean = false) {

    //the dockable and window surface component are necessary
    val windowDockable = UiDockable(name)

    val selectedColors = mutableStateOf(Colors.darkColors())
    val selectedUiSize = mutableStateOf(Sizes.medium)

    val windowSurface = WindowSurface(windowDockable) {
        surface.sizes = selectedUiSize.use()
        surface.colors = selectedColors.use()
        modifyWindow()

        var isMinimizedToTitle by remember(false)
        val isDocked = windowDockable.isDocked.use()

        Column(Grow.Std, Grow.Std) {
            TitleBar(
                windowDockable,
                isMinimizedToTitle = isMinimizedToTitle,
                onMinimizeAction = if (!isDocked && !isMinimizedToTitle) {
                    {
                        isMinimizedToTitle = true
                        windowDockable.setFloatingBounds(height = FitContent)
                    }
                } else null,
                onMaximizeAction = if (!isDocked && isMinimizedToTitle) {
                    { isMinimizedToTitle = false }
                } else null,
                onCloseAction = if (isClosable) {
                    {
                        //uiDemo.closeWindow(this@DemoWindow)
                    }
                } else null
            )
            if (!isMinimizedToTitle) {
                windowContent()
            }
        }
    }

    protected open fun UiScope.modifyWindow() { }

    protected abstract fun UiScope.windowContent(): Any

    open fun onClose() { }

    protected fun UiScope.applyThemeBackgroundColor() {
        val borderColor = colors.secondaryVariantAlpha(0.3f)
        if (windowDockable.isDocked.use()) {
            modifier
                .background(RectBackground(colors.background))
                .border(RectBorder(borderColor, sizes.borderWidth))
        } else {
            modifier
                .background(RoundRectBackground(colors.background, sizes.gap))
                .border(RoundRectBorder(borderColor, sizes.gap, sizes.borderWidth))
        }
    }
}