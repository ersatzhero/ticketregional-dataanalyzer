package de.ersatzhero.ticketregionalpipe.batch;

import de.ersatzhero.ticketregionalpipe.batch.model.ExtendedTicketRegionalData;
import de.ersatzhero.ticketregionalpipe.batch.model.TicketRegionalData;
import de.ersatzhero.ticketregionalpipe.client.GeolocationClient;
import de.ersatzhero.ticketregionalpipe.client.Point;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Processor implements ItemProcessor<TicketRegionalData, ExtendedTicketRegionalData> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
    private final GeolocationClient osmClient;
    private final String file;
    private final String eventName;

    public Processor(GeolocationClient osmClient, String file, String eventName) {
        this.osmClient = osmClient;
        this.file = file;
        this.eventName = eventName;
    }

    @Override
    public ExtendedTicketRegionalData process(TicketRegionalData ticketRegionalData) throws Exception {
        if (ticketRegionalData == null || ticketRegionalData.getBarcode() == null || ticketRegionalData.getBarcode().isBlank()) {
            return null;
        }

        String addressData = getAddressData(ticketRegionalData.getOrderData(), ticketRegionalData.getAddress());
        String geolocation = getGeolocation(addressData);

        LocalDateTime parsedDate;
        try {
            parsedDate = LocalDateTime.parse(ticketRegionalData.getSoldAt(), formatter);
        } catch (Exception ex) {
            parsedDate = null;
        }

        return new ExtendedTicketRegionalData(
            ticketRegionalData.getBarcode().replaceAll("'", ""),
            ticketRegionalData.getRow(),
            ticketRegionalData.getCategory(),
            ticketRegionalData.getStand(),
            ticketRegionalData.getSoldBy(),
            ticketRegionalData.getPrice(),
            ticketRegionalData.getDiscount(),
            parsedDate,
            ticketRegionalData.getDeliveryMethod(),
            geolocation,
            addressData,
            eventName,
            file
        );
    }

    private String getGeolocation(String addressData) {
        if (addressData == null) {
            return null;
        }

        Point geolocation = osmClient.getGeolocation(addressData);
        if (geolocation == null) {
            return null;
        }

        return geolocation.lat() + "," + geolocation.lon();
    }

    private String getAddressData(String orderData, String address) {
        if (orderData.isBlank() && address.isBlank()) {
            return null;
        }

        if (!orderData.strip().isBlank()) {
            System.out.println(orderData);
            String[] orderDataWithAddress = orderData.split(",");
            if (orderDataWithAddress.length <= 2) {
                return returnAddressDataOrNull(address);
            }
            return orderDataWithAddress[1] + ", " + orderDataWithAddress[2];
        } else {
            return returnAddressDataOrNull(address);
        }
    }

    private String returnAddressDataOrNull(String address) {
        return address.isBlank() ? null : address;
    }
}
