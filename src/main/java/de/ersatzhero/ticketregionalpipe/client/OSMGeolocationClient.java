package de.ersatzhero.ticketregionalpipe.client;

import de.ersatzhero.ticketregionalpipe.api.OSM;
import de.ersatzhero.ticketregionalpipe.api.model.Place;
import org.springframework.cache.annotation.Cacheable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class OSMGeolocationClient implements GeolocationClient {

    private final OSM osmClient;
    private final Function<List<Place>, Point> parseFunction;
    private final String countries;
    private final String callerEmail;

    public OSMGeolocationClient(OSM osmClient, String countries, String callerEmail) {
        this(osmClient, new DEFAULT_PARSE_FUNCTION(), countries, callerEmail);
    }

    public OSMGeolocationClient(OSM osmClient, Function<List<Place>, Point> parseFunction, String countries, String callerEmail) {
        this.osmClient = osmClient;
        this.parseFunction = parseFunction;
        this.countries = countries;
        this.callerEmail = callerEmail;
    }

    @Override
    @Cacheable("geolocation")
    public Point getGeolocation(String address) {
        List<Place> places = osmClient.query(address, "jsonv2", countries, callerEmail);
        return parseFunction.apply(places);
    }

    public static class DEFAULT_PARSE_FUNCTION implements Function<List<Place>, Point> {

        @Override
        public Point apply(List<Place> places) {
            if (places.size() == 0) {
                return null;
            }

            places.sort(Comparator.comparingInt(Place::place_rank));
            Place place = places.get(0);
            return new Point(place.lat(), place.lon());
        }
    }
}
