/*
 * Copyright 2014 Martin W. Kirst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nitram509.jmacaroons;

import com.github.nitram509.jmacaroons.util.Base64;

import java.util.ArrayList;
import java.util.List;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;
import static com.github.nitram509.jmacaroons.util.StringUtil.getBytes;

class MacaroonsSerializer {

  private static final byte[] HEX = new byte[]{
          '0', '1', '2', '3',
          '4', '5', '6', '7',
          '8', '9', 'a', 'b',
          'c', 'd', 'e', 'f'};

  public static String serialize(Macaroon macaroon) {
    List<byte[]> packets = new ArrayList<byte[]>(3 + macaroon.caveatPackets.length);
    packets.add(serialize_packet(Type.location, macaroon.location));
    packets.add(serialize_packet(Type.identifier, macaroon.identifier));
    for (CaveatPacket caveatPacket : macaroon.caveatPackets) {
      packets.add(serialize_packet(caveatPacket.type, caveatPacket.value));
    }
    packets.add(serialize_packet(Type.signature, macaroon.signatureBytes));
    return Base64.encodeUrlSafeToString(flattenByteArray(packets));
  }

  private static byte[] serialize_packet(Type type, String data) {
    return serialize_packet(type, getBytes(data));
  }

  private static byte[] serialize_packet(Type type, byte[] data) {
    String typname = type.name();
    int packet_len = PACKET_PREFIX_LENGTH + typname.length() + KEY_VALUE_SEPARATOR.length() + data.length + LINE_SEPARATOR.length();
    byte[] packet = new byte[packet_len];
    int offset = 0;

    System.arraycopy(packet_header(packet_len), 0, packet, offset, PACKET_PREFIX_LENGTH);
    offset += PACKET_PREFIX_LENGTH;

    System.arraycopy(typname.getBytes(), 0, packet, offset, typname.length());
    offset += typname.length();

    System.arraycopy(KEY_VALUE_SEPARATOR.getBytes(), 0, packet, offset, KEY_VALUE_SEPARATOR.length());
    offset += KEY_VALUE_SEPARATOR.length();

    System.arraycopy(data, 0, packet, offset, data.length);
    offset += data.length;

    System.arraycopy(LINE_SEPARATOR.getBytes(), 0, packet, offset, LINE_SEPARATOR.length());
    offset += LINE_SEPARATOR.length();
    return packet;
  }

  private static byte[] packet_header(int size) {
    assert (size < 65536);
    size = (size & 0xffff);
    byte[] packet = new byte[PACKET_PREFIX_LENGTH];
    packet[0] = HEX[(size >> 12) & 15];
    packet[1] = HEX[(size >> 8) & 15];
    packet[2] = HEX[(size >> 4) & 15];
    packet[3] = HEX[(size) & 15];
    return packet;
  }

  private static byte[] flattenByteArray(List<byte[]> packets) {
    int size = 0;
    for (byte[] packet : packets) {
      size += packet.length;
    }
    byte[] alldata = new byte[size];
    size = 0;
    for (byte[] packet : packets) {
      System.arraycopy(packet, 0, alldata, size, packet.length);
      size += packet.length;
    }
    return alldata;
  }

}
