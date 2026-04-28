package com.sentinel.bootstrap;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;

/**
 * 最小化 Observation 与 MeterRegistry 适配层。
 */
public class MeterRegistryObservationHandlerAdapter implements ObservationHandler<Observation.Context> {

    private final MeterRegistry meterRegistry;

    public MeterRegistryObservationHandlerAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return meterRegistry != null;
    }
}
