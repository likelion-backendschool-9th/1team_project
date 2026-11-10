package com.dipsource.search;

import java.nio.file.Path;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

/** 검색 통합 테스트가 같이 쓰는 컨테이너. 플러그인이 든 이미지를 한 번만 만들고 JVM 안에서 재사용한다. */
public final class SearchContainers {

    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");
    public static final ElasticsearchContainer ELASTICSEARCH = new ElasticsearchContainer(pluginImage())
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

    static {
        POSTGRES.start();
        ELASTICSEARCH.start();
    }

    private SearchContainers() {
    }

    private static DockerImageName pluginImage() {
        String name = new ImageFromDockerfile("dipsource-elasticsearch-test", false)
                .withFileFromPath(".", Path.of("../docker/elasticsearch"))
                .get();
        return DockerImageName.parse(name).asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch");
    }

    public static String elasticsearchUri() {
        return "http://" + ELASTICSEARCH.getHttpHostAddress();
    }
}
