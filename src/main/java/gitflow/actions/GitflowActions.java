package gitflow.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

/**
 * All actions associated with Gitflow
 *
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 */
public class GitflowActions {

    public static void runMergeTool(){
        git4idea.actions.GitResolveConflictsAction resolveAction = new git4idea.actions.GitResolveConflictsAction();
        AnActionEvent e = new AnActionEvent(null, DataManager.getInstance().getDataContext(), ActionPlaces.UNKNOWN, new Presentation(""), ActionManager.getInstance(), 0);
        resolveAction.actionPerformed(e);
    }
}
