/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * RFXCOM data class for curtain1 message. See Harrison.
 *
 * @author Evert van Es - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComCurtain1Message extends RFXComBaseMessage {

    public enum SubType {
        HARRISON(0),

        UNKNOWN(255);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        SubType(byte subType) {
            this.subType = subType;
        }

        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum Commands {
        OPEN(0),
        CLOSE(1),
        STOP(2),
        PROGRAM(3),

        UNKNOWN(255);

        private final int command;

        Commands(int command) {
            this.command = command;
        }

        Commands(byte command) {
            this.command = command;
        }

        public byte toByte() {
            return (byte) command;
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays
            .asList(RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.COMMAND);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
            .asList(RFXComValueSelector.SHUTTER);

    public SubType subType = SubType.HARRISON;
    public char sensorId = 'A';
    public byte unitCode = 0;
    public Commands command = Commands.STOP;
    public byte signalLevel = 0;
    public byte batteryLevel = 0;

    public RFXComCurtain1Message() {
        packetType = PacketType.CURTAIN1;
    }

    public RFXComCurtain1Message(byte[] data) {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) {

        super.encodeMessage(data);

        try {
            subType = SubType.values()[super.subType];
        } catch (Exception e) {
            subType = SubType.UNKNOWN;
        }
        sensorId = (char) data[4];
        unitCode = data[5];

        try {
            command = Commands.values()[data[6]];
        } catch (Exception e) {
            command = Commands.UNKNOWN;
        }

        signalLevel = (byte) ((data[7] & 0xF0) >> 4);
        batteryLevel = (byte) ((data[7] & 0x0F));
    }

    @Override
    public byte[] decodeMessage() {
        // Example data 07 18 00 00 65 01 00 00
        // 07 18 00 00 65 02 00 00

        byte[] data = new byte[8];

        data[0] = 0x07;
        data[1] = 0x18;
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) sensorId;
        data[5] = unitCode;
        data[6] = command.toByte();
        data[7] = (byte) (((signalLevel & 0x0F) << 4) + batteryLevel);

        return data;
    }

    @Override
    public String getDeviceId() {
        return sensorId + ID_DELIMITER + unitCode;
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state = UnDefType.UNDEF;

        if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {

                state = new DecimalType(batteryLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }

        } else if (valueSelector.getItemClass() == RollershutterItem.class) {

            if (valueSelector == RFXComValueSelector.COMMAND) {

                switch (command) {
                    case CLOSE:
                        state = OpenClosedType.CLOSED;
                        break;

                    case OPEN:
                        state = OpenClosedType.OPEN;
                        break;

                    default:
                        break;
                }

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to SwitchItem");
            }

        } else {

            throw new RFXComException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());

        }

        return state;
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        this.subType = ((SubType) subType);
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        String[] ids = deviceId.split("\\" + ID_DELIMITER);
        if (ids.length != 2) {
            throw new RFXComException("Invalid device id '" + deviceId + "'");
        }

        sensorId = ids[0].charAt(0);
        unitCode = Byte.parseByte(ids[1]);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case SHUTTER:
                if (type instanceof OpenClosedType) {
                    command = (type == OpenClosedType.CLOSED ? Commands.CLOSE : Commands.OPEN);
                } else if (type instanceof UpDownType) {
                    command = (type == UpDownType.UP ? Commands.CLOSE : Commands.OPEN);
                } else if (type instanceof StopMoveType) {
                    command = Commands.STOP;

                } else {
                    throw new RFXComException("Can't convert " + type + " to Command");
                }
                break;

            default:
                throw new RFXComException("Can't convert " + type + " to " + valueSelector);
        }

    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {

        for (SubType s : SubType.values()) {
            if (s.toString().equals(subType)) {
                return s;
            }
        }

        // try to find sub type by number
        try {
            return SubType.values()[Integer.parseInt(subType)];
        } catch (Exception e) {
            throw new RFXComException("Unknown sub type " + subType);
        }
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return supportedInputValueSelectors;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return supportedOutputValueSelectors;
    }

}
