/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.tellstick;

import nu.nethome.coders.decoders.FineOffsetDecoder;
import nu.nethome.coders.decoders.NexaLDecoder;
import nu.nethome.util.ps.BitString;
import nu.nethome.util.ps.ProtocolDecoderSink;

public class FineOffsetEventReceiver extends TellstickEventReceiverAdaptor {

    FineOffsetDecoder decoder = new FineOffsetDecoder();

    public FineOffsetEventReceiver(ProtocolDecoderSink sink) {
        decoder.setTarget(sink);
    }

    @Override
    public void processActiveEvent(TellstickEvent event) {
        decoder.decodeMessage(new BitString(Long.parseLong(event.getDataString(), 16), 40));
    }

    @Override
    public String getEventType() {
        return "protocol:fineoffset;model:";
    }

    @Override
    public String getProtocolName() {
        return "FineOffset";
    }
}
