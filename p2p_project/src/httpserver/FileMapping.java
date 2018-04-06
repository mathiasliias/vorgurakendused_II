package httpserver;

import javax.json.JsonObject;
import javax.websocket.server.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import com.sun.faces.util.Json;

import javax.ws.rs.Path;

@Path("files")
public class FileMapping {

    @GET
    @Path("all")
    @Produces("application/json")
    public JsonObject getAllFilesData() {
    	return null;
    }
    
    @GET
    @Path("/{id}")
    public JsonObject getFileData(@PathParam("id") String id) {
    	return null;//fdgsd
    }
    
}
