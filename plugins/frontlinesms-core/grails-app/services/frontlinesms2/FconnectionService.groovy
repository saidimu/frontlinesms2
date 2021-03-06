package frontlinesms2

import org.apache.camel.*
import serial.SerialClassFactory
import serial.CommPortIdentifier
import net.frontlinesms.messaging.*

class FconnectionService {
	def camelContext
	def deviceDetectionService
	def i18nUtilService
	
	def createRoutes(Fconnection c) {
		println "FconnectionService.createRoutes() :: ENTRY :: $c"
		if(c instanceof SmslibFconnection) {
			deviceDetectionService.stopFor(c.port)
			// work-around for CORE-736 - NoSuchPortException can be thrown
			// for RXTX when a port has not previously been listed with
			// getPortIdentifiers()
			if(SerialClassFactory.instance?.serialPackageName == SerialClassFactory.PACKAGE_RXTX) {
				CommPortIdentifier.getPortIdentifiers()
			}
		}
		println "creating route for fconnection $c"
		try {
			def routes = c.routeDefinitions
			camelContext.addRouteDefinitions(routes)
			createSystemNotification('connection.route.successNotification', [c?.name?: c?.id])
			LogEntry.log("Created routes: ${routes*.id}")
		} catch(FailedToCreateProducerException ex) {
			logFail(c, ex.cause)
		} catch(Exception ex) {
			logFail(c, ex)
			destroyRoutes(c.id as long)
		}
		println "FconnectionService.createRoutes() :: EXIT :: $c"
	}
	
	def destroyRoutes(Fconnection c) {
		destroyRoutes(c.id as long)
		createSystemNotification('connection.route.destroyNotification', [c?.name?: c?.id])
	}
	
	def destroyRoutes(long id) {
		println "fconnectionService.destroyRoutes : ENTRY"
		println "fconnectionService.destroyRoutes : id=$id"
		camelContext.routes.findAll { it.id ==~ /.*-$id$/ }.each {
			try {
				println "fconnectionService.destroyRoutes : route-id=$it.id"
				println "fconnectionService.destroyRoutes : stopping route $it.id..."
				camelContext.stopRoute(it.id)
				println "fconnectionService.destroyRoutes : $it.id stopped.  removing..."
				camelContext.removeRoute(it.id)
				println "fconnectionService.destroyRoutes : $it.id removed."
			} catch(Exception ex) {
				println "fconnectionService.destroyRoutes : Exception thrown while destroying $it.id: $ex"
				ex.printStackTrace()
			}
		}
		println "fconnectionService.destroyRoutes : EXIT"
	}
	
	def getConnectionStatus(Fconnection c) {
		if (c instanceof SmslibFconnection)
			return camelContext.routes.any { it.id ==~ /.*-$c.id$/ } ? ConnectionStatus.CONNECTED : deviceDetectionService.isConnecting((c as SmslibFconnection).port) ? ConnectionStatus.CONNECTING : ConnectionStatus.NOT_CONNECTED
		else
			return camelContext.routes.any { it.id ==~ /.*-$c.id$/ } ? ConnectionStatus.CONNECTED : ConnectionStatus.NOT_CONNECTED
	}
	
	// TODO rename 'handleNotConnectedException'
	def handleDisconnection(Exchange ex) {
		try {
			println "fconnectionService.handleDisconnection() : ENTRY"
			def caughtException = ex.getProperty(Exchange.EXCEPTION_CAUGHT)
			println "FconnectionService.handleDisconnection() : ex.fromRouteId: $ex.fromRouteId"
			println "FconnectionService.handleDisconnection() : EXCEPTION_CAUGHT: $caughtException"

			log.warn("Caught exception for route: $ex.fromRouteId", caughtException)
			def routeId = (ex.fromRouteId =~ /(?:(?:in)|(?:out))-(?:[a-z]+-)?(\d+)/)[0][1]
			println "FconnectionService.handleDisconnection() : Looking to stop route: $routeId"
			RouteDestroyJob.triggerNow([routeId:routeId as long])
		} catch(Exception e) {
			e.printStackTrace()
		}
	}

	private def logFail(c, ex) {
		ex.printStackTrace()
		log.warn("Error creating routes to fconnection with id $c?.id", ex)
		LogEntry.log("Error creating routes to fconnection with name ${c?.name?: c?.id}")
		createSystemNotification('connection.route.failNotification', [c?.id, c?.name?:c?.id], ex)
	}

	private def createSystemNotification(code, args, exception=null) {
		if(exception) args += [i18nUtilService.getMessage(code:'connection.error.'+exception.class.name.toLowerCase(), args:[exception.message])]
		def text = i18nUtilService.getMessage(code:code, args:args)
		def notification = SystemNotification.findByText(text) ?: new SystemNotification(text:text)
		notification.read = false
		notification.save(failOnError:true, flush:true)
	}
}

