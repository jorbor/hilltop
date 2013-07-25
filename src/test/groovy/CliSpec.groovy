
package hilltop

import spock.lang.*

class CliSpec extends Specification {
  def writer
	def config = {
    description 'test description'
    options {
      v longOpt: 'version', 'test version'
      e longOpt: 'echo', args: 1, argName: 'message', 'test echo'
    }
    execute { params ->
      if (params.v)
        writer << 'version 1'
      if (params.e)
        writer << params.message
    }
  }

  def 'root command with simple option is executed'() {
    def cli = new Cli('test', config)
    writer = new StringWriter()

    when:
      cli.run('-v')
    then:
      writer.toString() == 'version 1'
  }
}