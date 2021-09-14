package de.ersatzhero.ticketregionalpipe.api.model;

public record Place(
        String placeId,
        String license,
        String osmType,
        String osmId,
        String[] boundingbox,
        String lat,
        String lon,
        String displayName,
        String clazz,
        String type,
        String importance,
        String icon,
        int place_rank
) {
    @Override
    public String placeId() {
        return placeId;
    }

    @Override
    public String license() {
        return license;
    }

    @Override
    public String osmType() {
        return osmType;
    }

    @Override
    public String osmId() {
        return osmId;
    }

    @Override
    public String[] boundingbox() {
        return boundingbox;
    }

    @Override
    public String lat() {
        return lat;
    }

    @Override
    public String lon() {
        return lon;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public String clazz() {
        return clazz;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String importance() {
        return importance;
    }

    @Override
    public String icon() {
        return icon;
    }

    @Override
    public int place_rank() {
        return place_rank;
    }
}
