package statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;

class DemoStatusBarWidgetFactory : StatusBarEditorBasedWidgetFactory() {

  override fun getId(): String {
    return "dev.statusbar.widget";
  }

  override fun getDisplayName(): String {
    return "开发助手"
  }

  override fun createWidget(project: Project): StatusBarWidget {
    return  DemoStatusBarWidget(project);
  }

  override fun disposeWidget(widget: StatusBarWidget) {
    Disposer.dispose(widget);
  }
}
