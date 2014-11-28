package rest;

import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by jaideepray on 11/26/14.
 */

@Path("/find-corner")
@Produces(MediaType.APPLICATION_JSON)
public class CornerFinderResource {

    @GET
    @Timed
    public String hello(@QueryParam("name") String name){
        return "Hello World";
    }
}
