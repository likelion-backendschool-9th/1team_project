package com.dipsource.service.demo.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DemoBackendAddressPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "0.0.0.0", "0.1.2.3", "10.0.0.1", "10.255.255.255", "100.64.0.1", "100.127.255.255", "127.0.0.1", "127.255.0.1",
            "169.254.169.254", "172.16.0.1", "172.31.255.255", "192.0.0.1", "192.0.2.1", "192.88.99.1", "192.168.1.1",
            "198.18.0.1", "198.19.255.255", "198.51.100.1", "203.0.113.1", "224.0.0.1", "239.255.255.255",
            "240.0.0.1", "255.255.255.255",
            "::", "::1", "::ffff:127.0.0.1", "::ffff:10.0.0.1", "::ffff:169.254.169.254", "::127.0.0.1",
            "fe80::1", "fc00::1", "fd12:3456::1", "ff02::1", "64:ff9b::7f00:1",
            "2001:db8::1", "2001::1", "2002:7f00:1::1", "3fff::1",
    })
    void 내부_예약_주소를_막는다(String address) throws Exception {
        assertThat(DemoBackendAddressPolicy.isPublic(InetAddress.getByName(address))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "8.8.8.8", "1.1.1.1", "9.255.255.255", "11.0.0.1", "100.63.255.255", "100.128.0.1", "126.255.255.255",
            "128.0.0.1", "169.253.0.1", "172.15.255.255", "172.32.0.1", "192.167.255.255", "192.169.0.1", "198.17.255.255",
            "198.20.0.1", "223.255.255.255",
            "2606:4700:4700::1111", "2001:4860:4860::8888", "2a00:1450:4001::1",
    })
    void 공인_주소는_허용한다(String address) throws Exception {
        assertThat(DemoBackendAddressPolicy.isPublic(InetAddress.getByName(address))).isTrue();
    }
}
