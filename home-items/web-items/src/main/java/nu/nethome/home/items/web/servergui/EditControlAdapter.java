package nu.nethome.home.items.web.servergui;

import nu.nethome.home.system.HomeService;

public class EditControlAdapter implements EditControl {
    private String control;

    public EditControlAdapter(String control) {
        this.control = control;
    }

    @Override
    public String print(HomeGUIArguments arguments, HomeService server) {
        return control;
    }
}
