package hilltop.commands

import com.urbancode.anthill3.main.client.AnthillClient;
import com.urbancode.anthill3.persistence.UnitOfWork;
import com.urbancode.anthill3.domain.project.*;
import com.urbancode.anthill3.domain.workflow.*;

@Mixin(ConsoleCommands)
class AnthillCommands {
  protected get_project_or_complain(name) {
    if (name == '.') name = infer_project_name(name)

    def projects = ProjectFactory.getInstance()
      .restoreAllForName(name)

    if (projects.size() > 1)
      println "There are ${projects.size()} named <$name>; taking the first one"
    if (projects.size() == 0)
      quit "No such project <$name>"

    projects[0]
  }

  protected get_workflow_or_complain(projectName, workflowName) {
    def project = get_project_or_complain(projectName)
    def workflow = WorkflowFactory.getInstance()
      .restoreForProjectAndWorkflowName(project, workflowName)

    if (!workflow)
      quit "No such workflow <$workflowName> for project <$projectName>"
    [project, workflow]
  }

  protected String infer_project_name(name) {
    if (name != '.') return name
    System.getProperty("user.dir")
      .tokenize(File.separator).last()
  }

  protected work(Closure task) {
    def settings = config.get('anthill')
    if (settings == null || settings.api_token.isEmpty() || settings.api_server.isEmpty()) {
      quit 'Your Anthill configuration requires anthill.api_server and anthill.api_token values.'
    }

    def result
    def client = AnthillClient.connect(settings.api_server, 4567, settings.api_token)
    def uow = client.createUnitOfWork()
    try {
      result = task(uow)
    }
    catch (Exception e) {
      if (!uow.isClosed())
        uow.cancel()
      throw e
    }

    uow.commitAndClose();
    result
  }
}
