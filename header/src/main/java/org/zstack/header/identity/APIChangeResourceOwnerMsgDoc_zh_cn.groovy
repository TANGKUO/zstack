package org.zstack.header.identity

org.zstack.header.identity.APIChangeResourceOwnerEvent

doc {
    title "ChangeResourceOwner"

    category "identity"

    desc "在这里填写API描述"

    rest {
        request {
			url "POST /v1/account/{accountUuid}/resources"


            header (OAuth: 'the-session-uuid')

            clz APIChangeResourceOwnerMsg.class

            desc ""
            
			params {

				column {
					name "accountUuid"
					enclosedIn "params"
					desc "账户UUID"
					location "url"
					type "String"
					optional false
					since "0.6"
					
				}
				column {
					name "resourceUuid"
					enclosedIn "params"
					desc ""
					location "body"
					type "String"
					optional false
					since "0.6"
					
				}
				column {
					name "systemTags"
					enclosedIn ""
					desc ""
					location "body"
					type "List"
					optional true
					since "0.6"
					
				}
				column {
					name "userTags"
					enclosedIn ""
					desc ""
					location "body"
					type "List"
					optional true
					since "0.6"
					
				}
			}
        }

        response {
            clz APIChangeResourceOwnerEvent.class
        }
    }
}