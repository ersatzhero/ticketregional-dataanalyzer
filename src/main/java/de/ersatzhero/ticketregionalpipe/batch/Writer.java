package de.ersatzhero.ticketregionalpipe.batch;

import de.ersatzhero.ticketregionalpipe.batch.model.ExtendedTicketRegionalData;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class Writer implements ItemWriter<ExtendedTicketRegionalData> {

    private final RestHighLevelClient client;

    public Writer(RestHighLevelClient client) {
        this.client = client;
    }

    @Override
    public void write(List<? extends ExtendedTicketRegionalData> list) throws Exception {
        BulkRequest bulkRequest = new BulkRequest("orders");
        list.stream()
                .map(data -> { IndexRequest request = new IndexRequest(); request.id(data.id()); request.source(
                        "row", data.row(),
                        "category", data.category(),
                        "stand", data.stand(),
                        "soldBy", data.soldBy(),
                        "soldAt", data.soldAt(),
                        "deliveryMethod", data.deliveryMethod(),
                        "geolocation", data.geolocation(),
                        "price", data.price(),
                        "discount", data.discount(),
                        "address", data.address(),
                        "event", data.event(),
                        "file", data.file()
                ); return request;})
                .forEach(bulkRequest::add);
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
}
