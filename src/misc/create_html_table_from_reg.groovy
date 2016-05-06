import groovy.json.JsonSlurper

def slurper = new JsonSlurper()
def data = slurper.parse(new File("data.json"))
def out = new File("data.html")
out.delete()
out << "<html><body ><table border=\"1\" style=\"font-size:2em;\"><tbody><meta charset=\"utf-8\">"
data.each { 
	 out << "<tr><td>${it.firstName} ${it.name}</td></tr>"
}
out << "</tbody></body></html>"