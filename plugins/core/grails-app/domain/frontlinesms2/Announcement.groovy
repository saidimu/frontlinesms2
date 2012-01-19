package frontlinesms2

import java.util.Date;

class Announcement extends MessageOwner {
	static transients = ['liveMessageCount']
	String name
	static hasMany = [sentMessages: Fmessage]
	
	static constraints = {
		name(nullable:false)
		sentMessages(nullable:false)
	}
	
	def getAnnouncementMessages(getOnlyStarred = false) {
		Fmessage.owned(getOnlyStarred, this)
	}
	
	def archive() {
		this.archived = true
		def messagesToArchive = Fmessage?.owned(this, true)?.list()
		messagesToArchive.each { it?.archived = true }
	}
	
	def unarchive() {
		this.archived = false
		def messagesToArchive = Fmessage?.owned(this, true)?.list()
		messagesToArchive.each { it?.archived = false }
	}
	
	def getLiveMessageCount() {
		def m = Fmessage.findAllByMessageOwnerAndDeleted(this, false)
		m ? m.size() : 0
	}
}