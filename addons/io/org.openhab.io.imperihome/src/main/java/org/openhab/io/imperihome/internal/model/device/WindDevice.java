/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.imperihome.internal.model.param.NumericValueParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;
import org.openhab.io.imperihome.internal.processor.ItemProcessor;

/**
 * Wind sensor device.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class WindDevice extends AbstractNumericValueDevice {

    private static final String LINK_DIRECTION = "direction";

    public WindDevice(Item item) {
        super(DeviceType.WIND, item, "km/h");
    }

    @Override
    public void updateParams() {
        super.updateParams();

        if (getLinks().containsKey(LINK_DIRECTION)) {
            String deviceName = getLinks().get(LINK_DIRECTION);
            String deviceId = ItemProcessor.getDeviceId(deviceName);
            AbstractDevice dirDevice = getDeviceRegistry().getDevice(deviceId);
            if (dirDevice == null) {
                logger.error("Couldn't resolve linked wind direction device '{}', make sure the Item has iss tags",
                        deviceName);
                return;
            }

            NumericValueParam valueParam = (NumericValueParam) dirDevice.getParams().get(ParamType.GENERIC_VALUE);
            if (valueParam == null) {
                logger.warn("Linked Wind direction device has no Value parameter: {}", dirDevice);
                return;
            }

            NumericValueParam dirParam = new NumericValueParam(ParamType.DIRECTION, valueParam.getUnit(), null);
            if (StringUtils.isEmpty(dirParam.getUnit())) {
                dirParam.setUnit("Degrees");
            }

            dirParam.setValue(valueParam.getValue());
            addParam(dirParam);
        }
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        DecimalType value = (DecimalType) item.getStateAs(DecimalType.class);
        addParam(new NumericValueParam(ParamType.SPEED, getUnit(), value));
    }

}