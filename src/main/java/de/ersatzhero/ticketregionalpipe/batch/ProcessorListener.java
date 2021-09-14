package de.ersatzhero.ticketregionalpipe.batch;

import de.ersatzhero.ticketregionalpipe.batch.model.ExtendedTicketRegionalData;
import de.ersatzhero.ticketregionalpipe.batch.model.TicketRegionalData;
import org.springframework.batch.core.ItemProcessListener;

public class ProcessorListener implements ItemProcessListener<TicketRegionalData, ExtendedTicketRegionalData> {
    @Override
    public void beforeProcess(TicketRegionalData ticketRegionalData) {

    }

    @Override
    public void afterProcess(TicketRegionalData ticketRegionalData, ExtendedTicketRegionalData extendedTicketRegionalData) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Couldn't pause");
        }
    }

    @Override
    public void onProcessError(TicketRegionalData ticketRegionalData, Exception e) {

    }
}
