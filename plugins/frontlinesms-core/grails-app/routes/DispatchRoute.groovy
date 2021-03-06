import org.apache.camel.builder.RouteBuilder

class DispatchRoute extends RouteBuilder {
	void configure() {
		onCompletion().onCompleteOnly()
				.beanRef('dispatchRouterService', 'handleCompleted')
		onCompletion().onFailureOnly()
				.beanRef('dispatchRouterService', 'handleFailed')
				
		from('seda:dispatches')
				.dynamicRouter(bean('dispatchRouterService', 'slip'))
				.routeId('dispatch-route')
	}
}
