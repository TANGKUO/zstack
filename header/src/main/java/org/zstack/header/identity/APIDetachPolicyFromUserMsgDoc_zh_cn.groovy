package org.zstack.header.identity

org.zstack.header.identity.APIDetachPolicyFromUserEvent

doc {
    title "DetachPolicyFromUser"

    category "identity"

    desc "在这里填写API描述"

    rest {
        request {
			url "DELETE /v1/accounts/users/{userUuid}/policies/{policyUuid}"


            header (OAuth: 'the-session-uuid')

            clz APIDetachPolicyFromUserMsg.class

            desc ""
            
			params {

				column {
					name "policyUuid"
					enclosedIn ""
					desc "权限策略UUID"
					location "url"
					type "String"
					optional false
					since "0.6"
					
				}
				column {
					name "userUuid"
					enclosedIn ""
					desc "用户UUID"
					location "url"
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
            clz APIDetachPolicyFromUserEvent.class
        }
    }
}