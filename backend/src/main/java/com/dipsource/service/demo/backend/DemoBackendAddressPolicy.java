package com.dipsource.service.demo.backend;

import java.net.InetAddress;

/** 서버가 연결해도 되는 IP인지 판단한다. IPv6는 전역 유니캐스트만 허용하고, 그 안의 특수 대역을 뺀다. */
final class DemoBackendAddressPolicy {

    /** {시작 주소, 프리픽스 길이}. RFC 6890 등의 사설·예약 대역 */
    private static final int[][] BLOCKED_V4 = {
            {ipv4(0, 0, 0, 0), 8},
            {ipv4(10, 0, 0, 0), 8},
            {ipv4(100, 64, 0, 0), 10},
            {ipv4(127, 0, 0, 0), 8},
            {ipv4(169, 254, 0, 0), 16},
            {ipv4(172, 16, 0, 0), 12},
            {ipv4(192, 0, 0, 0), 24},
            {ipv4(192, 0, 2, 0), 24},
            {ipv4(192, 31, 196, 0), 24},
            {ipv4(192, 52, 193, 0), 24},
            {ipv4(192, 88, 99, 0), 24},
            {ipv4(192, 168, 0, 0), 16},
            {ipv4(192, 175, 48, 0), 24},
            {ipv4(198, 18, 0, 0), 15},
            {ipv4(198, 51, 100, 0), 24},
            {ipv4(203, 0, 113, 0), 24},
            {ipv4(224, 0, 0, 0), 4},
            {ipv4(240, 0, 0, 0), 4},
    };

    private DemoBackendAddressPolicy() {
    }

    static boolean isPublic(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 4 ? isPublicV4(bytes) : isPublicV6(bytes);
    }

    private static boolean isPublicV4(byte[] b) {
        int value = ipv4(b[0] & 0xff, b[1] & 0xff, b[2] & 0xff, b[3] & 0xff);
        for (int[] range : BLOCKED_V4) {
            int mask = -1 << (32 - range[1]);
            if ((value & mask) == range[0]) {
                return false;
            }
        }
        return true;
    }

    /** ::ffff:0:0/96(IPv4 매핑), 64:ff9b::/96(NAT64), fc00::/7, fe80::/10은 2000::/3 밖이라 여기서 함께 막힌다. */
    private static boolean isPublicV6(byte[] b) {
        int first = b[0] & 0xff;
        int second = b[1] & 0xff;
        int third = b[2] & 0xff;
        int fourth = b[3] & 0xff;
        if ((first & 0xe0) != 0x20) {
            return false;
        }
        boolean ietfSpecial = first == 0x20 && second == 0x01 && (third & 0xfe) == 0;
        boolean documentation = first == 0x20 && second == 0x01 && third == 0x0d && fourth == 0xb8;
        boolean sixToFour = first == 0x20 && second == 0x02;
        boolean documentation2 = first == 0x3f && second == 0xff && (third & 0xf0) == 0;
        return !ietfSpecial && !documentation && !sixToFour && !documentation2;
    }

    private static int ipv4(int a, int b, int c, int d) {
        return a << 24 | b << 16 | c << 8 | d;
    }
}
