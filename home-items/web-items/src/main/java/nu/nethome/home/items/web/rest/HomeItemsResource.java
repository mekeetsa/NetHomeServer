package nu.nethome.home.items.web.rest;

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.items.web.rest.exceptions.RestException;
import nu.nethome.home.system.HomeService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static nu.nethome.home.items.web.rest.ItemDirectoryEntryDto.toDtos;

@Path("/rest2")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class HomeItemsResource {

    public static final int ITEM_NOT_FOUND = 100;
    private HomeService server;
    private LogReader logReader;

    public HomeItemsResource(HomeService server) {
        this.server = server;
        logReader = new LogReader(server);
    }

    /**
     * List all HomeItem instances
     *
     * @return a directory listing of all HomeItems running in the server
     */
    @GET
    @Path("/items")
    public List<ItemDirectoryEntryDto> listItems() {
        return toDtos(server.listInstances(""));
    }

    /**
     * Get the content of the specified HomeItem
     *
     * @param itemId Identity of the Item to get
     * @return HomeItem description
     */
    @GET
    @Path("/items/{itemId}")
    public ItemDto getItem(@PathParam("itemId") String itemId) {
        return new ItemDto(validateNotNull(server.openInstance(itemId)));
    }

    /**
     * Update attributes and instance name of a HomeItem
     *
     * @param itemId Identity of the Item to update
     * @param itemDto Name and attribute values to update
     * @return An updated HomeItem description
     * @throws IllegalValueException
     */
    @PUT
    @Path("/items/{itemId}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ItemDto setAttributes(@PathParam("itemId") String itemId, ItemDto itemDto) throws IllegalValueException {
        return updateAttributesAndName(validateNotNull(server.openInstance(itemId)), itemDto);
    }

    private ItemDto updateAttributesAndName(HomeItemProxy item, ItemDto itemDto) throws IllegalValueException {
        if (itemDto.getAttributes() != null) {
            updateItemAttributes(item, itemDto);
        }
        if (itemDto.getName() != null && !item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE).equals(itemDto.getName())) {
            updateItemName(item, itemDto);
        }
        return new ItemDto(item);
    }

    private void updateItemName(HomeItemProxy item, ItemDto itemDto) {
        if (!server.renameInstance(item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE), itemDto.getName())) {
            throw new RestException("Illegal instance name", 400, HttpURLConnection.HTTP_NOT_MODIFIED);
        }
    }

    private void updateItemAttributes(HomeItemProxy item, ItemDto itemDto) throws IllegalValueException {
        for (AttributeDto attribute : itemDto.getAttributes()) {
            item.setAttributeValue(attribute.getName(), attribute.getValue());
        }
    }

    /**
     * Get log values for the specified time period
     *
     * @param itemId
     * @return log values
     * @throws IOException
     */
    @GET
    @Path("/items/{itemId}/log")
    public List<Object[]> log(@PathParam("itemId") String itemId) throws IOException {
        // TODO: get date parameters
        return logReader.getLog(new Date(0), null, validateNotNull(server.openInstance(itemId)));
    }

    /**
     * Create a new HomeItem instance. If the instance name starts with "#", the new instance is not activated
     * and this has to be made later with a call to the "activate"-action.
     *
     * @param itemDto Name, Class and attribute values of the new instance
     * @return  HomeItem description
     * @throws IllegalValueException
     * @throws ExecutionFailure
     */
    @POST
    @Path("/items")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ItemDto createItem(ItemDto itemDto) throws IllegalValueException, ExecutionFailure {
        HomeItemProxy item = validateCreation(server.createInstance(validateString(itemDto.getClassName()), validateString(itemDto.getName())));
        if (itemDto.getAttributes() != null) {
            updateItemAttributes(item, itemDto);
        }
        if (!itemDto.getName().startsWith("#")) {
            item.callAction("activate");
        }
        return new ItemDto(item);
    }

    private HomeItemProxy validateCreation(HomeItemProxy itemProxy) {
        if (itemProxy == null) throw new RestException("Could not create HomeItem",
                800, HttpURLConnection.HTTP_NOT_ACCEPTABLE);
        return itemProxy;
    }

    private String validateString(String string) {
        if (string == null || string.isEmpty()) throw new RestException("Required argument missing",
                700, HttpURLConnection.HTTP_BAD_REQUEST);
        return string;
    }


    /**
     * Call an action on the specified HomeItem
     *
     * @param itemId Identity of the Item to invoke the action on
     * @param action The action to invoke
     * @return HomeItem description of the state of the HomeItem after the action is performed
     * @throws ExecutionFailure
     */
    @PUT
    @Path("/items/{itemId}/action")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ItemDto performAction(@PathParam("itemId") String itemId, String action) throws ExecutionFailure {
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
