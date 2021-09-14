package de.ersatzhero.ticketregionalpipe.api;

import de.ersatzhero.ticketregionalpipe.api.configuration.FeignConfig;
import de.ersatzhero.ticketregionalpipe.api.model.Place;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="osmClient", qualifiers="osmClient", url="https://nominatim.openstreetmap.org", configuration = FeignConfig.class)
public interface OSM {
    @RequestMapping(method = RequestMethod.GET, path = "search", consumes = "application/json", produces = "application/json")
    List<Place> query(@RequestParam(value = "q", required = true) String query, @RequestParam(value = "format", required = false) String format, @RequestParam(value = "countrycodes", required = false) String countrycodes, @RequestParam(value = "email", required = false) String email);
}
