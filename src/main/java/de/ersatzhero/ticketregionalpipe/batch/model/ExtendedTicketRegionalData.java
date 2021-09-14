package de.ersatzhero.ticketregionalpipe.batch.model;

import java.time.LocalDateTime;

public record ExtendedTicketRegionalData(
        String id,
        String row,
        String category,
        String stand,
        String soldBy,
        String price,
        String discount,
        LocalDateTime soldAt,
        String deliveryMethod,
        String geolocation,
        String address,
        String event,
        String file
) {
    @Override
    public String id() {
        return id;
    }

    @Override
    public String row() {
        return row;
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public String stand() {
        return stand;
    }

    @Override
    public String soldBy() {
        return soldBy;
    }

    @Override
    public String price() {
        return price;
    }

    @Override
    public String discount() {
        return discount;
    }

    @Override
    public LocalDateTime soldAt() {
        return soldAt;
    }

    @Override
    public String deliveryMethod() {
        return deliveryMethod;
    }

    @Override
    public String geolocation() {
        return geolocation;
    }

    public String address() {
        return address;
    }

    @Override
    public String file() {
        return file;
    }
}
