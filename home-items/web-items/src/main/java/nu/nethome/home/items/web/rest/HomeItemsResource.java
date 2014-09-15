package nu.nethome.home.items.web.rest;

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.items.web.rest.exceptions.RestException;
import nu.nethome.home.system.HomeService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;

import static nu.nethome.home.items.web.rest.ItemDirectoryEntryDto.toDtos;

@Path("/rest2")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class HomeItemsResource {

    public static final int ITEM_NOT_FOUND = 100;
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
    public List<ItemDirectoryEntryDto> listItems() {
        return toDtos(server.listInstances(""));
    }

    @GET
    @Path("/items/{itemId}")
    public ItemDto getCylinder(@PathParam("itemId") String itemId) {
        return new ItemDto(validateNotNull(server.openInstance(itemId)));
    }

    @PUT
    @Path("/items/{itemId}/action")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ItemDto getCylinder(@PathParam("itemId") String itemId, String action) throws ExecutionFailure {
        HomeItemProxy item = validateNotNull(server.openInstance(itemId));
        item.callAction(action);
        return new ItemDto(item);
    }

    private HomeItemProxy validateNotNull(HomeItemProxy homeItemProxy) {
        if (homeItemProxy == null) throw new RestException("No such HomeItem",
                ITEM_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND);
        return homeItemProxy;
    }
}
