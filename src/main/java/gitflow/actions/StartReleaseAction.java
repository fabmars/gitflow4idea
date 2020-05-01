package gitflow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import git4idea.validators.GitNewBranchNameValidator;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.singleton;

public class StartReleaseAction extends AbstractStartAction {

    StartReleaseAction() {
        super("Start Release");
    }

    StartReleaseAction(GitRepository repo) {
        super(repo,"Start Release");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        final Project project = e.getProject();
        final String releaseName = Messages.showInputDialog(project, "Enter the name of new release:", "New Release", Messages.getQuestionIcon(), "",
                GitNewBranchNameValidator.newInstance(singleton(myRepo)));

        if (releaseName == null){
            // user clicked cancel
        }
        else if (releaseName.isEmpty()){
            Messages.showWarningDialog(project, "You must provide a name for the release", "Whoops");
        }
        else {
            this.runAction(project, null, releaseName, null);
        }

    }

    @Override
    public void runAction(Project project, String baseBranchName, String releaseName, @Nullable Runnable callInAwtLater) {

        new Task.Backgroundable(project,"Starting release "+releaseName,false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(project);
                GitCommandResult result =  myGitflow.startRelease(myRepo, releaseName, errorLineHandler);

                if (result.success()) {
                    String startedReleaseMessage = String.format("A new release '%s%s' was created, based on '%s'", branchUtil.getPrefixRelease(), releaseName, branchUtil.getBranchnameDevelop());
                    NotifyUtil.notifySuccess(myProject, releaseName, startedReleaseMessage);
                }
                else {
                    NotifyUtil.notifyError(myProject, "Error", "Please have a look at the Version Control console for more details");
                }

                myRepo.update();

            }
        }.queue();
    }
}