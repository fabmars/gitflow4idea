package gitflow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.GitflowInitOptions;
import org.jetbrains.annotations.NotNull;

public class ReInitRepoAction extends InitRepoAction {
    ReInitRepoAction() {
        super("Re-init Repo");
    }

    ReInitRepoAction(GitRepository repo) {
        super(repo, "Re-init Repo");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        GitflowBranchUtil branchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);

        // Only show when gitflow is setup
        if (branchUtil.hasGitflow()) {
            e.getPresentation().setEnabledAndVisible(true);
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    protected String getSuccessMessage() {
        return "Re-initialized gitflow in repo " + myRepo.getRoot().getPresentableName();
    }

    @Override
    protected GitCommandResult executeCommand(GitflowInitOptions initOptions,
            GitflowErrorsListener errorLineHandler,
            GitflowLineHandler localLineHandler) {
        return myGitflow.reInitRepo(myRepo, initOptions, errorLineHandler, localLineHandler);
    }

    @Override
    protected String getTitle() {
        return "Re-initializing Repo";
    }

    @Override
    protected GitflowLineHandler getLineHandler(Project project) {
        return new GitflowErrorsListener(project);
    }
}
