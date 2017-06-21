/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yw.ldap;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author Y.W
 */
@Path("/rest")
public class RestService {

    LdapService ldapService;

    public LdapService getLdapService() {
        return ldapService;
    }

    public void setLdapService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @GET
    @Path("/check")
    public Response checkACL(
            @QueryParam("user") String user,
            @QueryParam("acl") String acl) throws Exception {

        String result = "FALSE";

        List<String> aclList = new ArrayList<String>();
        aclList.add(acl);

        ldapService.containACLs(user, aclList) ;
        // ldapService.checkACL(user, acl)
        System.out.println("using Camel LDAP");
        if(ldapService.containACLs(user, aclList) ){
            result="TRUE";
        }
        
        return Response
                .status(200)
                .entity(result).build();

    }

}
