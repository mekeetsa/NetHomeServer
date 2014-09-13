package nu.nethome.home.items.web.rest;

import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.HomeService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static nu.nethome.home.items.web.rest.ItemDirectoryEntryDto.toDtos;

@Path("/rest2")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class HomeItemsResource {

    private HomeService server;

    public HomeItemsResource(HomeService server) {
        this.server = server;
    }

    /**
     * Just a test of the service
     * @return a static string
     */
    @GET
    @Path("/hello")
    public Response  helloGet() {
        return Response.status(200).entity("Hello world!").build();
    }

    /**
     * @return a directory listing of all HomeItems running in the server
     */
    @GET
    @Path("/items")
    public Response listItems() {
        return Response.ok(toDtos(server.listInstances(""))).build();
    }

    @GET
    @Path("/items/{itemId}")
    public Response getCylinder(@PathParam("itemId") String itemId) {
        HomeItemProxy item = server.openInstance(itemId);
        // TODO: Just test implementation, should list an HomeItemDto
        return Response.status(200).entity(item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE)).build();
    }
}
