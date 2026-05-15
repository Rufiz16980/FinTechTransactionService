package az.et.fintechtransactionservice.application.port.out;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}

