package gitflow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.ui.GitflowStartFeatureDialog;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StartFeatureAction extends AbstractStartAction {

    public StartFeatureAction() {
        super("Start Feature");
    }
    public StartFeatureAction(GitRepository repo) {
        super(repo, "Start Feature");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        Project project = e.getProject();
        GitflowStartFeatureDialog dialog = new GitflowStartFeatureDialog(project, myRepo);
        dialog.show();

        if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

        final String featureName = dialog.getNewBranchName();
        final String baseBranchName = dialog.getBaseBranchName();

        this.runAction(e.getProject(), baseBranchName, featureName, null);
    }

    public void runAction(final Project project, final String baseBranchName, final String featureName, @Nullable final Runnable callInAwtLater){

        new Task.Backgroundable(project, "Starting feature " + featureName, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                final GitCommandResult commandResult = createFeatureBranch(project, baseBranchName, featureName);
                if (callInAwtLater != null && commandResult.success()) {
                    callInAwtLater.run();
                }
            }
        }.queue();
    }

    private GitCommandResult createFeatureBranch(Project project, String baseBranchName, String featureName) {
        GitflowErrorsListener errorListener = new GitflowErrorsListener(project);
        GitCommandResult result = myGitflow.startFeature(myRepo, featureName, baseBranchName, errorListener);

        if (result.success()) {
            String startedFeatureMessage = String.format("A new branch '%s%s' was created, based on '%s'", branchUtil.getPrefixFeature(), featureName, baseBranchName);
            NotifyUtil.notifySuccess(project, featureName, startedFeatureMessage);
        } else {
            NotifyUtil.notifyError(project, "Error", "Please have a look at the Version Control console for more details");
        }

        myRepo.update();
        virtualFileMananger.asyncRefresh(null); //update editors
        return result;
    }
}