package httpserver;

import jdk.nashorn.internal.objects.annotations.Getter;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

@Path("/files")
public class FileMapping {

    @GET
    @Produces("application/json")
    public
}
