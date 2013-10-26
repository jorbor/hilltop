
package hilltop.commands

import hilltop.Config
import hilltop.anthill.BuildFinder
import com.urbancode.anthill3.services.build.*
import com.urbancode.anthill3.domain.buildrequest.*
import com.urbancode.anthill3.domain.buildlife.*

@Mixin(ConsoleHelper)
@Mixin(AnthillHelper)
class RequestCommands {
  def config = new Config()
  def buildFinder = new BuildFinder({
    error { m -> quit m }
  })

  def open(id) {
    def settings = config.get('anthill')
    def request = work {
      buildFinder.request(id as int)
    }

    browse link_to(request)
  }

  def show(id) {
    work {
      def request = buildFinder.request(id as int)
      def project = request.project
      def workflow = request.workflow

      echo "$project.name $workflow.name"
      echo link_to(request)

      echo 'Status', request.status.toString()
      echo 'Requester', "$request.requesterName ($request.requestSource.name)"
      if (request.status == BuildRequestStatusEnum.BUILD_LIFE_CREATED)
        echo 'Buildlife', request.buildLife.id.toString()
      if (request.status == BuildRequestStatusEnum.STARTED_WORKFLOW) 
        echo 'WorkflowCase', request.workflowCase.id.toString()

      if (request.propertyNames) {
        echo "Properties", { line ->
          request.propertyNames.each { n -> line.echo "$n=${request.getPropertyValue(n)}" }
        }      
      }
    }
  }
}