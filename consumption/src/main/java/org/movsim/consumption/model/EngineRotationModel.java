package org.movsim.consumption.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.movsim.consumption.autogen.GearRatio;
import org.movsim.consumption.autogen.RotationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class EngineRotationModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(EngineRotationModel.class);

    private static final double INVMIN_TO_INVSEC = 1. / 60;
    /** idle rotation rate, minimum frequency(1/s) */
    public final double minFrequency;

    /** maximum rotation rate of engine (1/s) */
    public final double maxFrequency;

    /** dynamic tire radius (m) */
    private final double dynamicRadius;

    private final List<Double> gears;

    EngineRotationModel(RotationModel rotationModel) {
        minFrequency = rotationModel.getIdleRotationRateInvmin() * INVMIN_TO_INVSEC;
        maxFrequency = rotationModel.getMaxRotationRateInvmin() * INVMIN_TO_INVSEC;
        dynamicRadius = rotationModel.getDynamicTyreRadius();
        gears = getSortedGearBox(rotationModel.getGearRatio());
    }

    private List<Double> getSortedGearBox(List<GearRatio> gearRatio) {
        if (gearRatio.isEmpty()) {
            return createDefaultGears();
        }
        return getSortedTransmissions(gearRatio);
    }

    private List<Double> getSortedTransmissions(List<GearRatio> gearRatio) {
        List<Double> gearTransmissions = Lists.newArrayList();
        for (GearRatio ratio : gearRatio) {
            gearTransmissions.add(ratio.getPhi());
        }
        Collections.sort(gearTransmissions, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                final Double pos1 = new Double((o1).doubleValue());
                final Double pos2 = new Double((o2).doubleValue());
                return pos2.compareTo(pos1); // sort with DECREASING transmission ratios (gear 1 has highest ratio)
            }
        });

        return gearTransmissions;
    }

    /**
     * Sets default gear box with 5 gears
     */
    private List<Double> createDefaultGears() {
        List<Double> defaultGears = Lists.newArrayList(13.9, 7.8, 5.26, 3.79, 3.09);
        return defaultGears;
    }

    public double dynamicRadius() {
        return dynamicRadius;
    }

    public double dynamicWheelCircumfence() {
        return 2 * Math.PI * dynamicRadius;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    private double getGearRatio(int gearIndex) {
        return gears.get(gearIndex);
    }

    public int getNumberOfGears() {
        return gears.size();
    }

    public int getMaxGearIndex() {
        return gears.size() - 1;
    }

    public double getIdleFrequency() {
        return minFrequency;
    }

    public double getEngineFrequency(double v, int gearIndex) {
        if (gearIndex < 0 || gearIndex > getMaxGearIndex()) {
            logger.error("gear out of range! g={}", gearIndex);
        }
        final double freq = getGearRatio(gearIndex) * v / dynamicWheelCircumfence();
        return Math.max(minFrequency, Math.min(freq, maxFrequency));
    }

    public boolean isFrequencyPossible(double v, int gearIndex) {
        if (gearIndex < 0 || gearIndex > getMaxGearIndex()) {
            logger.error("gear out of range !  g={}", gearIndex);
        }
        final double frequencyTest = getGearRatio(gearIndex) * v / dynamicWheelCircumfence();
        if (frequencyTest > maxFrequency || frequencyTest < minFrequency) {
            return false;
        }
        return true;
    }

}
