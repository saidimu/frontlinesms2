package frontlinesms2

import frontlinesms2.*

class SubscriptionService {

    def saveInstance(Subscription subscriptionInstance, params) {
		subscriptionInstance.group = Group.get(params.subscriptionGroup)
		if(subscriptionInstance.keywords)
			subscriptionInstance.keywords.clear()

		subscriptionInstance.defaultAction = Subscription.Action."${params.defaultAction.toUpperCase()}"
		subscriptionInstance.joinAutoreplyText = params.joinAutoreplyText
		subscriptionInstance.leaveAutoreplyText = params.leaveAutoreplyText
		subscriptionInstance.name = params.name

		subscriptionInstance.save(failOnError:true, flush:true)
		
		if(params.topLevelKeywords) {
			params.topLevelKeywords.toUpperCase().replaceAll(/\s/, "").split(",").each {
				subscriptionInstance.addToKeywords(new Keyword(value: it, isTopLevel:true))
			}
		}
		if(params.joinKeywords.trim()){
			params.joinKeywords.toUpperCase().replaceAll(/\s/, "").split(",").each {
				subscriptionInstance.addToKeywords(new Keyword(value: it, isTopLevel:!params.topLevelKeywords, ownerDetail: Subscription.Action.JOIN.toString()))
			}
		}
		if(params.leaveKeywords.trim()){
			params.leaveKeywords.toUpperCase().replaceAll(/\s/, "").split(",").each {
				subscriptionInstance.addToKeywords(new Keyword(value: it, isTopLevel:!params.topLevelKeywords, ownerDetail: Subscription.Action.LEAVE.toString()))
			}
		}
		subscriptionInstance.save(flush:true, failOnError: true)
		return subscriptionInstance
	}
}

