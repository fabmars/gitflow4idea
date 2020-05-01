package gitflow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractStartAction extends GitflowAction {
    AbstractStartAction(String actionName) {
        super(actionName);
    }

    AbstractStartAction(GitRepository repo, String actionName) {
        super(repo, actionName);
    }

    public abstract void runAction(Project project, final String baseBranchName, final String branchName, @Nullable final Runnable callInAwtLater);

    @Override
    public void update(@NotNull AnActionEvent e) {
        GitflowBranchUtil branchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);
        if (branchUtil != null) {
            //Disable and hide when gitflow has not been setup
            if (!branchUtil.hasGitflow()) {
                e.getPresentation().setEnabledAndVisible(false);
            } else {
                e.getPresentation().setEnabledAndVisible(true);
            }
        }
    }
}
