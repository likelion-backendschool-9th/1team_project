# 통합 검색용 Elasticsearch. 버전은 Spring Boot가 관리하는 elasticsearch-java 클라이언트(8.15.5)와 맞춘다.
FROM docker.elastic.co/elasticsearch/elasticsearch:8.15.5
RUN bin/elasticsearch-plugin install --batch analysis-nori analysis-icu analysis-kuromoji analysis-smartcn
